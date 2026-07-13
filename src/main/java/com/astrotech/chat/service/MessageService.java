package com.astrotech.chat.service;

import com.astrotech.chat.core.GetPageRequest;
import com.astrotech.chat.customCache.CustomCacheable;
import com.astrotech.chat.dto.response.*;
import com.astrotech.chat.entites.*;
import com.astrotech.chat.enums.ConversationType;
import com.astrotech.chat.enums.MessageEventType;
import com.astrotech.chat.enums.MessageStatus;
import com.astrotech.chat.enums.SendMessageType;
import com.astrotech.chat.events.MessageEvent;
import com.astrotech.chat.events.UpdateMessageResponseAndLastMessageEvent;
import com.astrotech.chat.exceptions.BadRequestException;
import com.astrotech.chat.exceptions.ResourceNotFoundException;
import com.astrotech.chat.exceptions.UnauthorizedException;
import com.astrotech.chat.mappers.MessageMapper;
import com.astrotech.chat.repositories.MessageRepository;
import com.astrotech.chat.repositories.MessageUpdateRepository;
import com.astrotech.chat.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

import static com.astrotech.chat.mappers.MessageMapper.mapToResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
        private final MessageUpdateRepository messageUpdateRepository;
        private final BlockedUserService blockedUserService;
        private final MessageRepository messageRepo;
        private final ConversationService conversationService;
        private final EncryptionUtil encryptionUtil;
        private final SimpMessagingTemplate simpMessagingTemplate;
        private final ApplicationEventPublisher eventPublisher;
        private final UserService userService;

        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "all-messages", allEntries = true),
                        @CacheEvict(value = "single-message", key = "#conversationId")
        })
        public MessageResponse sendMessage(String conversationId, String userId, SendMessageType type, String content) {
                log.info("Processing standard message send request");

                var user = userService.getUserOrNull(userId);
                var convedRef = conversationService.assertMember(conversationId, userId);

                if (isChatBlocked(convedRef, userId)) {
                        return MessageResponse.builder().content("Message blocked due to privacy settings.")
                                        .status(MessageStatus.FAILED).build();
                }

                String encryptedContent = null;
                String iv = null;
                if (content != null && !content.isBlank()) {
                        var enc = encryptionUtil.encrypt(content);
                        encryptedContent = enc.cipherText();
                        iv = enc.iv();
                }

                var message = Message.builder()
                                .conversationId(convedRef.getId())
                                .senderId(userId)
                                .type(type)
                                .content(encryptedContent)
                                .contentIv(iv)
                                .status(MessageStatus.SENT)
                                .sentAt(Instant.now())
                                .createdAt(Instant.now())
                                .build();

                var savedMessage = messageRepo.save(message);
                var decryptedContent = encryptionUtil.decryptContent(savedMessage.getContent(),
                                savedMessage.getContentIv());

                dispatchMessageEvents(conversationId, savedMessage, decryptedContent, user);
                return mapToResponse(savedMessage, decryptedContent, user);
        }

        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "all-messages", allEntries = true),
                        @CacheEvict(value = "single-message", key = "#conversationId")
        })
        public MessageResponse replyToMessage(String conversationId, String userId, String replyToId,
                        SendMessageType type, String content) {
                log.info("Processing reply message thread link request");

                var user = userService.getUserOrNull(userId);
                var convedRef = conversationService.assertMember(conversationId, userId);

                if (isChatBlocked(convedRef, userId)) {
                        return MessageResponse.builder().content("Reply blocked due to privacy settings.")
                                        .status(MessageStatus.FAILED).build();
                }

                var parentMessage = messageRepo.findByIdAndDeletedFalse(replyToId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "The message you are replying to has been deleted."));

                String encryptedContent = null;
                String iv = null;
                if (content != null && !content.isBlank()) {
                        var enc = encryptionUtil.encrypt(content);
                        encryptedContent = enc.cipherText();
                        iv = enc.iv();
                }

                var message = Message.builder()
                                .conversationId(convedRef.getId())
                                .senderId(userId)
                                .type(type)
                                .content(encryptedContent)
                                .contentIv(iv)
                                .replyTo(parentMessage)
                                .status(MessageStatus.SENT)
                                .sentAt(Instant.now())
                                .createdAt(Instant.now())
                                .build();

                var savedMessage = messageRepo.save(message);
                var decryptedContent = encryptionUtil.decryptContent(savedMessage.getContent(),
                                savedMessage.getContentIv());

                dispatchMessageEvents(conversationId, savedMessage, decryptedContent, user);
                return mapToResponse(savedMessage, decryptedContent, user);
        }

        @Transactional
        public List<MessageResponse> forwardMessages(
                        List<String> conversationIds,
                        String userId,
                        String messageId) {

                if (conversationIds == null || conversationIds.isEmpty()) {
                        return Collections.emptyList();
                }

                var sourceMessage = messageRepo.findByIdAndDeletedFalse(messageId)
                                .orElse(null);

                if (sourceMessage == null) {
                        log.warn("Forward cancelled. Source message {} was not found.", messageId);
                        return Collections.emptyList();
                }

                var sender = userService.getUserOrNull(userId);

                if (sender == null) {
                        log.warn("Forward cancelled. User {} was not found.", userId);
                        return Collections.emptyList();
                }

                Set<String> uniqueConversationIds = new LinkedHashSet<>(conversationIds);

                List<ForwardTarget> pendingForwards = new ArrayList<>();

                for (String conversationId : uniqueConversationIds) {

                        var conversation = conversationService.assertMember(conversationId, sender.getId());

                        if (conversation == null) {
                                log.debug("Skipping conversation {} because it does not exist or user is not a member.",
                                                conversationId);
                                continue;
                        }

                        if (isChatBlocked(conversation, sender.getId())) {
                                log.debug("Skipping blocked conversation {}.", conversationId);
                                continue;
                        }

                        var forwardedMessage = MessageMapper.forwardMessageBuilder(
                                        conversation.getId(),
                                        sender.getId(),
                                        sourceMessage);

                        pendingForwards.add(new ForwardTarget(conversation, forwardedMessage));
                }

                if (pendingForwards.isEmpty()) {
                        return Collections.emptyList();
                }

                var messages = pendingForwards.stream()
                                .map(ForwardTarget::message)
                                .toList();

                var savedMessages = messageRepo.saveAll(messages);

                List<MessageResponse> responses = new ArrayList<>(savedMessages.size());

                for (int i = 0; i < savedMessages.size(); i++) {

                        Message savedMessage = savedMessages.get(i);
                        Conversation conversation = pendingForwards.get(i).conversation();

                        String decryptedContent = encryptionUtil.decryptContent(
                                        savedMessage.getContent(),
                                        savedMessage.getContentIv());

                        dispatchMessageEvents(
                                        conversation.getId(),
                                        savedMessage,
                                        decryptedContent,
                                        sender);

                        responses.add(
                                        mapToResponse(
                                                        savedMessage,
                                                        decryptedContent,
                                                        sender));
                }

                return responses;
        }

        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "all-messages", allEntries = true),
                        @CacheEvict(value = "single-message", key = "#messageId")
        })
        public MessageResponse editMessage(String messageId, String newContent, String userId) {
                var user = userService.getAuthorizedUser(userId);
                var message = getMessageOrThrow(messageId);
                if (!message.getSenderId().equals(user.getId()))
                        throw new UnauthorizedException("Cannot edit another user's message");
                if (message.isDeleted())
                        throw new BadRequestException("Cannot edit a deleted message");
                encryptionUtil.decryptContent(message.getContent(), message.getContentIv());
                var enc = encryptionUtil.encrypt(newContent);

                message.setContent(enc.cipherText());
                message.setContentIv(enc.iv());
                message.setEdited(true);
                message.setEditedAt(Instant.now());
                var savedMessage = messageRepo.save(message);
                var decryptedContent = encryptionUtil.decryptContent(savedMessage.getContent(),
                                savedMessage.getContentIv());

                var response = mapToResponse(message, decryptedContent, user);
                MessageEvent event = new MessageEvent(MessageEventType.EDIT, response, message.getConversationId(),
                                Instant.now());
                simpMessagingTemplate.convertAndSend("/topic/conversation." + message.getConversationId(), event);
                return response;
        }

        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "all-messages", allEntries = true),
                        @CacheEvict(value = "single-message", key = "#messageId")
        })
        public Map<String, MessageResponse> deleteMessage(String messageId, boolean forAll, String currentUserId) {
                var message = getMessageOrThrow(messageId);
                var user = userService.getAuthorizedUser(currentUserId);
                var userId = user.getId();
                var isSender = message.getSenderId().equals(userId);

                if (!isSender && forAll) {
                        var content = encryptionUtil.decryptContent(message.getContent(), message.getContentIv());

                        return Map.of("status", mapToResponse(message, content != null ? content : "", user));
                }

                if (!isSender) {
                        message.setDeleted(true);
                        message.setDeletedForAll(false);
                        var savedMessage = messageRepo.save(message);
                        return Map.of("status", mapToResponse(savedMessage, "", user));
                }

                if (forAll) {
                        var hasReplies = messageRepo.existsByReplyToIdAndDeletedFalse(messageId);
                        if (hasReplies) {
                                throw new BadRequestException(
                                                "This message cannot be deleted because it has active replies.");
                        }
                        message.setDeletedForAll(true);
                        message.setType(SendMessageType.DELETED);
                }

                message.setDeleted(true);
                var messageSaved = messageRepo.save(message);

                var stub = MessageResponse.builder()
                                .id(messageId)
                                .deleted(true)
                                .conversationId(message.getConversationId())
                                .build();

                var event = new MessageEvent(MessageEventType.DELETE, stub, message.getConversationId(), Instant.now());
                simpMessagingTemplate.convertAndSend("/topic/conversation." + message.getConversationId(), event);

                return Map.of("status", mapToResponse(messageSaved, "", user));
        }

        @Transactional
        @CacheEvict(value = "single-message", key = "#messageId")
        public void toggleReaction(String messageId, String emoji, String currentUserId) {
                log.info("Toggle Reaction Message Service reached");
                var user = userService.getAuthorizedUser(currentUserId);
                var userId = user.getId();

                var projectionOpt = messageRepo.findUserReaction(messageId, userId);

                if (projectionOpt.isPresent() && !projectionOpt.get().getReactions().isEmpty()) {

                        var oldReaction = projectionOpt.get().getReactions().getFirst();

                        messageUpdateRepository.removeReaction(messageId, userId, oldReaction.getEmoji());

                        if (!oldReaction.getEmoji().equals(emoji)) {
                                var newReaction = MessageReaction.builder()
                                                .userId(userId)
                                                .emoji(emoji)
                                                .createdAt(Instant.now())
                                                .build();

                                messageUpdateRepository.addReaction(messageId, newReaction);
                        }
                } else {

                        var newReaction = MessageReaction.builder()
                                        .userId(userId)
                                        .emoji(emoji)
                                        .createdAt(Instant.now())
                                        .build();

                        messageUpdateRepository.addReaction(messageId, newReaction);
                }

                var updatedMessage = getMessageOrThrow(messageId);
                var decryptedContent = encryptionUtil.decryptContent(updatedMessage.getContent(),
                                updatedMessage.getContentIv());
                var response = mapToResponse(updatedMessage, decryptedContent, user);

                simpMessagingTemplate.convertAndSend(
                                "/topic/conversation." + updatedMessage.getConversationId(),
                                new MessageEvent(MessageEventType.REACTION, response,
                                                updatedMessage.getConversationId(), Instant.now()));
        }

        @Transactional
        public void markMessageDelivered(String messageId, String currentUserId) {
                var message = messageRepo.findByIdAndDeletedFalse(messageId).orElse(null);
                if (message != null) {
                        if (!currentUserId.equals(message.getSenderId())) {
                                conversationService.assertMember(message.getConversationId(), currentUserId);
                                message.setDeliveredAt(Instant.now());
                                message.setDelivered(true);
                                messageRepo.save(message);
                        }
                }

        }

        @Transactional
        public void markConversationRead(String conversationId, String userId) {

                conversationService.assertMember(conversationId, userId);

                var latestIncomingMessage = messageRepo
                                .findByConversationIdAndDeletedFalseOrderBySentAtDesc(conversationId,
                                                Pageable.ofSize(50))
                                .stream()
                                .filter(m -> !m.getSenderId().equals(userId))
                                .findFirst();

                if (latestIncomingMessage.isPresent()) {
                        String lastMessageId = latestIncomingMessage.get().getId();

                        boolean updated = conversationService.markConversationAsRead(conversationId, userId,
                                        lastMessageId);

                        if (updated) {

                                var readEvent = new ReadReceiptWebSocketPayload(conversationId, userId, lastMessageId);

                                simpMessagingTemplate.convertAndSend(
                                                "/topic/conversation." + conversationId,
                                                readEvent);
                        }
                }
        }

        @Transactional(readOnly = true)
        @CustomCacheable(value = "all-messages", key = "#conversationId + '-' + #page + '-' + #size")
        public SliceResponse<ConversationMessageResponse> getChatHistory(String conversationId, int page, int size,
                        String currentUserId) {
                log.info("Fetching chat history for conversation: {} page: {}", conversationId, page);

                var authorizedUser = userService.getAuthorizedUser(currentUserId);
                var conversation = conversationService.assertMember(conversationId, authorizedUser.getId());

                var pageable = GetPageRequest.getPageableWithSorting(page, size, "sentAt", false);
                var messageSlice = messageRepo
                                .findByConversationIdAndDeletedFalseOrderBySentAtDesc(conversation.getId(), pageable);

                var messages = messageSlice.getContent();

                var undeliveredIds = messages.stream()
                                .filter(m -> !m.isDelivered())
                                .filter(m -> !m.getSenderId().equals(authorizedUser.getId()))
                                .map(Message::getId)
                                .toList();

                if (!undeliveredIds.isEmpty()) {
                        messageRepo.bulkMarkAsDelivered(undeliveredIds);
                        // Also notify via WebSockets/Events here if needed using the undeliveredIds
                        // list

                        messages.forEach(m -> {
                                if (undeliveredIds.contains(m.getId())) {
                                        markMessageDelivered(m.getId(), authorizedUser.getId());
                                        m.setDelivered(true);
                                }
                        });
                }

                var content = messages.stream()
                                .map(this::convertToResponse)
                                .toList();

                return new SliceResponse<>(
                                content,
                                page,
                                size,
                                messageSlice.hasNext(),
                                messageSlice.hasPrevious());
        }


        @CustomCacheable(value = "single-message", key = "#messageId")
        public Optional<ConversationMessageResponse> getMessageById(String messageId, String currentUserId) {
                log.info("Fetching single message with ID: {}", messageId);

                var authorizedUser = userService.getAuthorizedUser(currentUserId);

                return messageRepo.findByIdAndDeletedFalse(messageId)
                                .map(message -> {
                                        conversationService.assertMember(message.getConversationId(),
                                                        authorizedUser.getId());

                                        if (!message.isDelivered()) {
                                                markMessageDelivered(message.getId(), authorizedUser.getId());
                                                message.setDelivered(true);
                                        }

                                        return convertToResponse(message);
                                });
        }

        private boolean isChatBlocked(Conversation conversation, String currentUserId) {

                if (conversation.getConversationType() == ConversationType.DIRECT) {
                        String targetUserId = conversation.getMembers().stream()
                                        .map(ConversationMember::getUserId)
                                        .filter(id -> !id.equals(currentUserId))
                                        .findFirst()
                                        .orElse(null);

                        return targetUserId != null && blockedUserService.isBlockActive(currentUserId, targetUserId);
                }
                return false;
        }

        private void dispatchMessageEvents(String convedId, Message message, String rawContent, User user) {
                eventPublisher.publishEvent(new UpdateMessageResponseAndLastMessageEvent(
                                convedId,
                                rawContent,
                                message.getSenderId()));

                var response = mapToResponse(message, rawContent, user);
                response.setConversationId(convedId);

                var event = new MessageEvent(MessageEventType.NEW, response, convedId, Instant.now());
                simpMessagingTemplate.convertAndSend("/topic/conversation." + convedId, event);

                deliverToOnlineMembers(convedId, message.getSenderId());
        }

        private ConversationMessageResponse convertToResponse(Message message) {
                if (message == null)
                        return null;

                var sender = userService.getUserOrNull(message.getSenderId());
                UserResponse senderResponse = sender != null ? mapToUserResponse(sender) : null;

                String finalDisplayContent = null;
                List<String> extractedUrls = Collections.emptyList();

                if (message.getType() == SendMessageType.TEXT) {
                        finalDisplayContent = encryptionUtil.decryptContent(message.getContent(),
                                        message.getContentIv());
                } else if (message.getType() == SendMessageType.DELETED) {
                        finalDisplayContent = "This message was deleted";
                } else {

                        if (message.getMediaAttachments() != null) {
                                extractedUrls = message.getMediaAttachments().stream()
                                                .map(MessageMedia::getStorageUrl)
                                                .toList();
                        }
                }

                ConversationMessageResponse nestedReply = null;
                if (message.getReplyTo() != null) {
                        nestedReply = convertToResponse(message.getReplyTo());
                }

                return ConversationMessageResponse.builder()
                                .id(message.getId())
                                .conversationId(message.getConversationId())
                                .sender(senderResponse)
                                .type(message.getType())
                                .content(finalDisplayContent)
                                .mediaUrls(extractedUrls)
                                .replyTo(Optional.ofNullable(nestedReply)
                                                .map(ConversationMessageResponse::getReplyTo)
                                                .orElse(null))
                                .forwardedFromId(message.getForwardedFromConversationId())
                                .edited(message.isEdited())
                                .editedAt(message.getEditedAt())
                                .deleted(message.isDeleted())
                                .deletedForAll(message.isDeletedForAll())
                                .reactionsCount(message.getReactionsCount())
                                .status(message.getStatus())
                                .sentAt(message.getSentAt())
                                .deliveredAt(message.getDeliveredAt())
                                .reactions(message.getReactions())
                                .readReceipts(message.getReadReceipts())
                                .build();
        }

        private UserResponse mapToUserResponse(User user) {

                return new UserResponse(user.getId(), user.getDisplayName(), user.getFullName(), user.getEmail(),
                                user.getStatus(), user.getPhoneNumber(), user.getRole());
        }

        @Transactional(readOnly = true)
        public PageResponse<MessageResponse> getMesssages(String conversationId, int page, int size,
                        String currentUserId) {
                var user = userService.getAuthorizedUser(currentUserId);
                var userId = user.getId();
                var conversation = conversationService.assertMember(conversationId, userId);
                var pageable = GetPageRequest.getPageableWithSorting(page, size, "sentAt", false);
                var messages = messageRepo
                                .findByConversationIdAndDeletedFalseOrderBySentAtDesc(conversation.getId(), pageable);

                var content = messages.getContent().stream()
                                .map(m -> mapToResponse(m,
                                                encryptionUtil.decryptContent(m.getContent(), m.getContentIv()), user))
                                .toList();

                return new PageResponse<>(
                                content,
                                page,
                                size,
                                messages.getNumberOfElements(),
                                messages.getSize(),
                                messages.hasNext(),
                                messages.hasPrevious());
        }

        @Transactional(readOnly = true)
        public PageResponse<MessageResponse> searchMessages(String conversationId, String query, int page, int size,
                        String currentUserId) {
                var user = userService.getAuthorizedUser(currentUserId);
                var userId = user.getId();
                conversationService.assertMember(conversationId, userId);
                Pageable pageable = GetPageRequest.getPageableWithoutSorting(page, size);

                Page<Message> results = messageRepo.searchMessages(conversationId, query, pageable);
                List<MessageResponse> content = results.getContent().stream()
                                .map(m -> mapToResponse(m,
                                                encryptionUtil.decryptContent(m.getContent(), m.getContentIv()), user))
                                .toList();
                return new PageResponse<>(
                                content,
                                page,
                                size,
                                results.getTotalElements(),
                                results.getTotalPages(),
                                results.hasNext(),
                                results.hasPrevious());
        }

        private Message getMessageOrThrow(String id) {
                return messageRepo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        }

        @Async("taskExecutor")
        public void deliverToOnlineMembers(String conversationId, String senderId) {
                getDelivered(conversationId, senderId);
        }

        public void getDelivered(String conversationId, String senderId) {
                messageRepo.markDelivered(conversationId, senderId, Instant.now());
                simpMessagingTemplate.convertAndSend("/topic/conversation." + conversationId,
                                new MessageEvent(MessageEventType.DELIVERED, null, conversationId, Instant.now()));
        }

}
