package com.astrotech.chat.service;


import com.astrotech.chat.cloudinary.CloudinaryBackendUploadResponse;
import com.astrotech.chat.cloudinary.CloudinaryService;
import com.astrotech.chat.core.FileHash;
import com.astrotech.chat.customCache.CustomCacheEvict;
import com.astrotech.chat.entites.Message;
import com.astrotech.chat.entites.MessageMedia;
import com.astrotech.chat.enums.MediaType;
import com.astrotech.chat.enums.MessageEventType;
import com.astrotech.chat.enums.MessageStatus;
import com.astrotech.chat.enums.SendMessageType;
import com.astrotech.chat.events.MediaUploadEvent;
import com.astrotech.chat.events.MessageEvent;
import com.astrotech.chat.exceptions.ResourceNotFoundException;

import com.astrotech.chat.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaService {
    private final FileHash fileHash;

    private final ConversationService conversationService;
    private final CloudinaryService cloudinaryService;

    private final MessageRepository messageRepository;
    private final ApplicationEventPublisher publisher;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageService messageService;


    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversation", keys = "#conversationId")
    public void createMedia(String secureUrl, String mediaType, String originalName, String mimeType,
                            long fileSize, String thumbnailUrl, Integer width, Integer height, Integer duration,
                            String checksum, String publicId, String fileHash, String messageId) {


        var media = MessageMedia.builder()
                .storageUrl(secureUrl)
                .mediaType(MediaType.valueOf(mediaType))
                .duration(duration)
                .createdAt(Instant.now())
                .checksum(checksum)
                .height(height)
                .fileHash(fileHash)
                .fileSize(fileSize)
                .mimeType(mimeType)
                .thumbnailUrl(thumbnailUrl)
                .fileName(originalName)
                .width(width)
                .publicId(publicId)
                .build();


        var message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Placeholder message not found for ID: " + messageId));


        if (message.getMediaAttachments() == null) {
            message.setMediaAttachments(new ArrayList<>());
        }
        message.getMediaAttachments().add(media);
        message.setStatus(MessageStatus.SENT);
        message.setType(SendMessageType.valueOf(mediaType));

        messageRepository.save(message);


        var event = new MessageEvent.MediaMessageEvent(
                MessageEventType.NEW, message.getConversationId(), Instant.now());
        simpMessagingTemplate.convertAndSend("/topic/conversation." + message.getConversationId(), event);
        messageService.getDelivered(message.getConversationId(), message.getSenderId());

        log.info("Background processing complete. Media attached to message ID: {}", messageId);
    }

    public Map<String, Object> sendMediaMessage(MultipartFile file, String conversationId, String userId) {


        conversationService.assertMember(conversationId, userId);

        var cloudinary = cloudinaryService.uploadMedia(file, conversationId, userId);
        if (cloudinary != null){
            var placeholderMessage = Message.builder()
                    .conversationId(conversationId)
                    .senderId(userId)
                    .content("")
                    .status(MessageStatus.PENDING)
                    .mediaAttachments(new ArrayList<>())
                    .sentAt(Instant.now())
                    .build();

            var savedMessage = messageRepository.save(placeholderMessage);


            if (messageRepository.existsByMediaAttachmentsPublicId(cloudinary.getPublicId())) {

                messageRepository.deleteById(savedMessage.getId());
                return Map.of("success", false, "message", "File already exists");
            }

            var hashedFile = fileHash.fileHashAsync(cloudinary.getSecureUrl());


            publisher.publishEvent(
                    new MediaUploadEvent(
                            cloudinary.getSecureUrl(),
                            String.valueOf(cloudinary.getMediaType()),
                            cloudinary.getOriginalName(),
                            cloudinary.getMimeType(),
                            cloudinary.getFileSize(),
                            cloudinary.getThumbnailUrl(),
                            cloudinary.getWidth(),
                            cloudinary.getHeight(),
                            cloudinary.getDuration(),
                            cloudinary.getChecksum(),
                            cloudinary.getPublicId(),
                            String.valueOf(hashedFile),
                            savedMessage.getId()

                    ));


            return Map.of(
                    "message", "Upload in progress",
                    "messageId", savedMessage.getId()
            );
        } else {
            return Map.of(
                    "message", "Upload Failed"
            );
        }

    }

    public Map<String, Object> sendMultipleMediaMessage(MultipartFile[] files, String conversationId, String userId) {

        conversationService.assertMember(conversationId, userId);

        var cloudinaryResponses = cloudinaryService.processBatchUpload(files, conversationId, userId);


        var validResponses = cloudinaryResponses.stream()
                .filter(Objects::nonNull)
                .toList();

        if (!validResponses.isEmpty()) {

            var placeholderMessage = Message.builder()
                    .conversationId(conversationId)
                    .senderId(userId)
                    .content("")
                    .status(MessageStatus.PENDING)
                    .mediaAttachments(new ArrayList<>())
                    .sentAt(Instant.now())
                    .build();

            var savedMessage = messageRepository.save(placeholderMessage);
            var messageId = savedMessage.getId();

            List<Map<String, String>> statusResults = new ArrayList<>();


            for (CloudinaryBackendUploadResponse cloudinary : validResponses) {


                if (messageRepository.existsByMediaAttachmentsPublicId(cloudinary.getPublicId())) {
                    statusResults.add(Map.of(
                            "publicId", cloudinary.getPublicId(),
                            "status", "File already exists"
                    ));
                    continue;
                }


                var hashedFile = fileHash.fileHashAsync(cloudinary.getSecureUrl());


                publisher.publishEvent(new MediaUploadEvent(
                        cloudinary.getSecureUrl(),
                        String.valueOf(cloudinary.getMediaType()),
                        cloudinary.getOriginalName(),
                        cloudinary.getMimeType(),
                        cloudinary.getFileSize(),
                        cloudinary.getThumbnailUrl(),
                        cloudinary.getWidth(),
                        cloudinary.getHeight(),
                        cloudinary.getDuration(),
                        cloudinary.getChecksum(),
                        cloudinary.getPublicId(),
                        String.valueOf(hashedFile),
                        messageId

                ));

                statusResults.add(Map.of(
                        "publicId", cloudinary.getPublicId(),
                        "status", "Upload in progress"
                ));
            }


            if (statusResults.stream().allMatch(r -> "File already exists".equals(r.get("status")))) {
                List<String> publicIds = validResponses.stream()
                        .map(CloudinaryBackendUploadResponse::getPublicId)
                        .toList();

                cloudinaryService.deleteResources(publicIds);
                messageRepository.deleteById(messageId);

                return Map.of("success", false, "message", "All uploaded files already exist");
            }

            return Map.of(
                    "message", "Batch file processing finalized",
                    "messageId", messageId,
                    "uploads", statusResults
            );
        } else {

            return Map.of(
                    "message", "Upload Failed"
            );
        }
    }}
