package com.astrotech.chat.service;

import com.astrotech.chat.core.GetPageRequest;
import com.astrotech.chat.customCache.CustomCacheEvict;
import com.astrotech.chat.customCache.CustomCacheable;
import com.astrotech.chat.dto.request.GroupChatRequest;
import com.astrotech.chat.dto.request.UpdateGroupRequest;
import com.astrotech.chat.dto.response.ConversationResponse;
import com.astrotech.chat.dto.response.DirectConversationResponse;
import com.astrotech.chat.dto.response.SliceResponse;
import com.astrotech.chat.entites.Conversation;
import com.astrotech.chat.entites.ConversationMember;
import com.astrotech.chat.entites.User;
import com.astrotech.chat.enums.ConversationType;
import com.astrotech.chat.enums.MemberRole;
import com.astrotech.chat.exceptions.BadRequestException;
import com.astrotech.chat.exceptions.ResourceNotFoundException;
import com.astrotech.chat.exceptions.UnauthorizedException;
import com.astrotech.chat.mappers.ConversationMapper;
import com.astrotech.chat.mappers.GroupInfoMapper;
import com.astrotech.chat.mappers.MessageMapper;
import com.astrotech.chat.repositories.ConversationRepository;
import com.astrotech.chat.repositories.ConversationUpdateRepository;
import com.astrotech.chat.repositories.MessageRepository;
import com.astrotech.chat.util.EncryptionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {
    private final UserService userService;
    private final ConversationRepository conversationRepository;
    private final BlockedUserService blockedUserService;
    private final ConversationUpdateRepository conversationUpdateRepository;
    private final MessageRepository messageRepository;
    private final EncryptionUtil encryptionUtil;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConversationMapper conversationMapper;

    @Transactional

    @CustomCacheEvict(cacheNames = {"my-conversation"}, keys = {"#convoId"})
    public void updateLastMessageSenderId(String convoId, String content, String senderId){
        var convo = findConversationId(convoId).orElse(null);
        if (convo != null){
            convo.setLastMessageSenderId(senderId);
            convo.setLastMessageAt(Instant.now());
            convo.setLastMessageText(content);
            
            conversationRepository.save(convo);
        }
    }


    @Transactional
    @CustomCacheEvict(cacheNames=  {"my-conversations"}, keys = {"#currentUserId"})
    public DirectConversationResponse getOrCreateDirect(String targetUserId, String currentUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new BadRequestException("Cannot start a conversation with yourself");
        }

        var target = userService.getAuthorizedUser(targetUserId);
        var blocked = blockedUserService.isBlockActive(currentUserId, target.getId());


        if (blocked) {
            log.warn("Direct conversation blocked between user={} and target={}", currentUserId, target.getId());
            throw new BadRequestException("Action blocked by privacy settings.");
        }

        var existing = conversationRepository.findDirectConversation(currentUserId, target.getId());
        if (existing.isPresent()) {
            return mapToDirectResponse(existing.get().getId());
        }


        var primaryMembers = List.of(
                ConversationMember.builder()
                        .userId(currentUserId)
                        .role(MemberRole.MEMBER)
                        .joinedAt(Instant.now())
                        .build(),
                ConversationMember.builder()
                        .userId(target.getId())
                        .role(MemberRole.MEMBER)
                        .joinedAt(Instant.now())
                        .build()
        );

        var convo = Conversation.builder()
                .conversationType(ConversationType.DIRECT)
                .createdById(currentUserId)
                .encrypted(true)
                .members(primaryMembers)
                .createdAt(Instant.now())
                .build();

        conversationRepository.save(convo);

        log.info("Direct conversation created: id={} between {} and {}", convo.getId(), currentUserId, target.getId());
        return mapToDirectResponse(convo.getId());
    }

    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversations", keys = "#creatorId")
    public ConversationResponse createGroup(GroupChatRequest request, String creatorId) {
        var inviteCode = encryptionUtil.generateSecureToken(6).toUpperCase();
        var groupInfo = GroupInfoMapper.create(creatorId, request.description(), request.name(), request.avatarUrl(), inviteCode, 300);
        List<ConversationMember> initialMembers = new ArrayList<>();


        initialMembers.add(ConversationMember.builder()
                .userId(creatorId)
                .role(MemberRole.OWNER)
                .joinedAt(Instant.now())
                .build());

        for (String memberId : request.membersId()) {
            if (!memberId.equals(creatorId)) {
                var isActive = blockedUserService.isBlockActive(creatorId, memberId);


                if (isActive) {
                    log.warn("Skipping member {} during group creation due to active block status.", memberId);
                    continue;
                }

                userService.findUserId(memberId).ifPresent(u -> initialMembers.add(ConversationMember.builder()
                        .userId(u.getId())
                        .role(MemberRole.MEMBER)
                        .joinedAt(Instant.now())
                        .build()));
            }
        }

        var convoGroup = Conversation.builder()
                .conversationType(ConversationType.GROUP)
                .encrypted(true)
                .createdById(creatorId)
                .groupInfo(groupInfo)
                .members(initialMembers)
                .createdAt(Instant.now())
                .build();

        conversationRepository.save(convoGroup);
        log.info("Group created successfully with {} members: id={} name={}",
                initialMembers.size(), convoGroup.getGroupInfo().getName(), creatorId);

        return mapToResponse(convoGroup.getId(), creatorId);
    }
    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversations", keys = "#convoId")
    public ConversationResponse updateGroup(String convId, UpdateGroupRequest req, String userId) {


       var group = assertAdminOrOwner(convId, userId);
        if (req.name() != null) group.getGroupInfo().setName(req.name());
        if (req.description() != null) group.getGroupInfo().setDescription(req.description());
        if (req.avatarUrl() != null) group.getGroupInfo().setAvatarUrl(req.avatarUrl());
        conversationRepository.save(group);
        broadcastSystemMessage(convId, "Group info was updated");
        return mapToResponse(group.getId(), userId);
    }
    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversations", keys = "#convoId")
    public void deleteGroup(String convId, String userId) {

        var group = assertOwner(convId, userId);
        group.setDeletedAt(Instant.now());
        conversationRepository.save(group);
        broadcastSystemMessage(convId, "This group has been dissolved by the owner");
    }

    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversations")
    public ConversationResponse joinByInviteCode(String code) {
        var user = userService.getUser();
        var userId = user.getId();
        var displayName = user.getDisplayName();


        var group = conversationRepository.findByGroupInfoInviteCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("No group found with code: " + code));


        if (group.getConversationType() != ConversationType.GROUP) {
            throw new BadRequestException("Invalid invite code transaction.");
        }


        var existing = group.getMembers()
                .stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst();


        if (existing.isPresent() && existing.get().isActive()) {
            log.info("User {} is already an active member of group {}. Returning details.", userId, group.getId());
            return mapToResponse(group.getId(), userId);
        }


        var memberCount = group.getMembers()
                .stream()
                .filter(ConversationMember::isActive)
                .count();

        if (memberCount >= group.getGroupInfo().getMaxMembers()) {
            throw new BadRequestException("This group channel has reached its maximum member capacity.");
        }


        if (existing.isPresent()) {

            conversationUpdateRepository.joinByInviteCodeIfExisting(group.getId(), userId, group.getGroupInfo().getInviteCode());
        } else {

            addMember(group.getId(), userId);
        }

        log.info("User {} successfully joined group {} via invite link", userId, group.getId());
        broadcastSystemMessage(group.getId(), displayName + " joined via invite link");

        return mapToResponse(group.getId(), userId);
    }
    public boolean markConversationAsRead(String conversationId, String userId, String lastMessageId){
        return conversationUpdateRepository.markConversationAsRead(conversationId, userId, lastMessageId);
    }
    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversation", keys = "#conversationId")
    public void leaveConversation(String conversationId, String newOwnerId) {
        var user = userService.getUser();
        var userId = user.getId();
        var displayName = user.getDisplayName();


        var conversation = findConversationId(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("No group found with id: " + conversationId));

        if (conversation.getConversationType() == ConversationType.DIRECT) {
            throw new BadRequestException("Cannot leave a direct conversation");
        }


        var member = conversation.getMembers()
                .stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Not a member of this conversation"));

        if (!member.isActive()) {
            throw new BadRequestException("You have already left this conversation");
        }


        if (member.getRole() == MemberRole.OWNER) {


           var otherActiveMembersCount = conversation.getMembers()
                    .stream()
                    .filter(m -> m.isActive() && !m.getUserId().equals(userId))
                    .count();

            if (otherActiveMembersCount > 0) {

                if (newOwnerId == null || newOwnerId.isBlank()) {
                    throw new BadRequestException("A new owner must be selected before leaving the conversation.");
                }


                var isNewOwnerAnActiveMember = conversation.getMembers()
                        .stream()
                        .anyMatch(m -> m.getUserId().equals(newOwnerId) && m.isActive());

                if (!isNewOwnerAnActiveMember) {
                    throw new BadRequestException("The nominated user must be an active member of this group.");
                }


                var newOwner = userService.getAuthorizedUser(newOwnerId);
                conversationUpdateRepository.transferOwnership(conversation.getId(), userId, newOwner.getId());


                broadcastSystemMessage(conversationId, "Ownership transferred to " + newOwner.getDisplayName());
            }

        }


        conversationUpdateRepository.leaveConversation(conversation.getId(), userId);
        broadcastSystemMessage(conversationId, displayName + " left the group");

        log.info("User {} has successfully exited group conversation {}", userId, conversationId);
    }

    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversation", keys = "#conversationId")
    public String addMemberToGroup(String conversationId, String targetUserId, String requestingUserId) {
        var blocked = blockedUserService.isBlockActive(requestingUserId, targetUserId);
        if (blocked) {
            return "BLOCKED_USER";
        }

        var convo = assertAdminOrOwner(conversationId, requestingUserId);

        var memberCount = convo.getMembers().stream()
                .filter(ConversationMember::isActive)
                .count();

        if (memberCount >= convo.getGroupInfo().getMaxMembers()) {
            return "GROUP_FULL";
        }


        var isAlreadyMember = convo.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(targetUserId) && m.isActive());

        if (isAlreadyMember) {
            return "ALREADY_MEMBER";
        }

        var finalTargetUserId = userService.getAuthorizedUser(targetUserId).getId();
        addMember(convo.getId(), finalTargetUserId);

        return "SUCCESS";
    }
    @Transactional
    public void addMembersToGroup(String conversationId, List<String> memberIds, String requestingUserId) {
        List<ConversationMember> membersToAppend = new ArrayList<>();

        for (String memberId : memberIds) {
            var blocked = blockedUserService.isBlockActive(requestingUserId, memberId);
            if (blocked) {
                log.warn("Bulk processing skipped memberId={} due to block relationship.", memberId);
                continue;
            }

            userService.findUserId(memberId).ifPresent(u -> membersToAppend.add(ConversationMember.builder()
                    .userId(u.getId())
                    .role(MemberRole.MEMBER)
                    .joinedAt(Instant.now())
                    .build()));
        }

        if (!membersToAppend.isEmpty()) {
            var success = conversationUpdateRepository.addMembers(conversationId, membersToAppend);
            if (!success) {
                log.error("Failed to commit appends for conversationId={}", conversationId);
            }
        }
    }
    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversation", keys = "#conversationId")
    public void removeMemberFromGroup(String conversationId, String targetUserId) {
        var userId = userService.getUser().getId();
        var convo = assertOwner(conversationId, userId);
        conversationUpdateRepository.removeMember(convo.getId(), targetUserId);

    }


    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversation", keys = "#conversationId")
    public String regenerateInviteCode(String convId, String userId) {

        var conv = assertAdminOrOwner(convId, userId);
        var inviteCode = encryptionUtil.generateSecureToken(6).toUpperCase();
        conv.getGroupInfo().setInviteCode(inviteCode);
        conversationRepository.save(conv);
        return conv.getGroupInfo().getInviteCode();
    }

    @CustomCacheable(
            value = "my-conversations",
            key = "#userId",
            ttl = 60,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<ConversationResponse> getMyConversations(String userId, int page, int size) {

        var pageable = GetPageRequest.getPageableWithSorting(page, size, "createdAt", true);
        var result = conversationRepository.findByMembersUserIdAndMembersLeftAtNullAndDeletedAtNullOrderByLastMessageAtDesc(userId, pageable);

               var content = result.getContent().stream().map(c -> mapToResponse(c.getId(), userId)).toList();
               return new SliceResponse<>(
                       content,
                       page,
                       size,
                       result.hasNext(),
                       result.hasPrevious()
               );
    }


    @CustomCacheable(value = "my-conversation", key = " #userId", ttl = 30,
            timeUnit = TimeUnit.MINUTES)
    public ConversationResponse getById(String convId, String userId) {

        var convo = assertMember(convId, userId);
        return mapToResponse(convo.getId(), userId);
    }
    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversation", keys = "#conversationId")
    public Map<String, String> muteConversation(String conversationId, String userId){

        var conversation = assertMember(conversationId, userId);
        conversationUpdateRepository.muteConversation(conversation.getId(), userId, true);
        return Map.of("message", "Muted successfully");
    }

    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversation", keys = "#conversationId")
    public Map<String, String> unMuteConversation(String conversationId, String userId){

        var conversation = assertMember(conversationId, userId);
        conversationUpdateRepository.muteConversation(conversation.getId(), userId, false);
        return Map.of("message", "Unmuted successfully");
    }
    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversation", keys = "#conversationId")
    public Map<String, String> pinConversation(String conversationId, String userId){

        var conversation = assertMember(conversationId, userId);
        conversationUpdateRepository.pinConversation(conversation.getId(), userId, true);
        return Map.of("message", "Pinned successfully");
    }
    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversation", keys = "#conversationId")
    public Map<String, String> unpinConversation(String conversationId, String userId){

        var conversation = assertMember(conversationId, userId);
        conversationUpdateRepository.pinConversation(conversation.getId(), userId, false);
        return Map.of("message", "Unpinned successfully");
    }
    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversation", keys = "#conversationId")
    public void changeMemberRole(String conversationId, String targetUserId, MemberRole newRole, String currentUserId) {



        var conversation = findConversationId(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("No group found with id: " + conversationId));

        if (conversation.getConversationType() != ConversationType.GROUP) {
            throw new BadRequestException("Cannot modify roles in a direct conversation");
        }


        var operator = conversation.getMembers().stream()
                .filter(m -> m.getUserId().equals(currentUserId) && m.isActive())
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this group"));

        if (operator.getRole() != MemberRole.OWNER && operator.getRole() != MemberRole.ADMIN) {
            throw new AccessDeniedException("You do not have permission to modify member roles");
        }


        var targetMember = conversation.getMembers().stream()
                .filter(m -> m.getUserId().equals(targetUserId) && m.isActive())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not an active member of this group"));


        if (newRole == MemberRole.OWNER) {
            throw new BadRequestException(
                    "Cannot explicitly assign an OWNER role. To switch owners, use the leave/transfer ownership protocol."
            );
        }

        if (operator.getRole() == MemberRole.ADMIN && targetMember.getRole() == MemberRole.OWNER) {
            throw new AccessDeniedException("Administrators cannot change the role of the group Owner");
        }


        var updated = conversationUpdateRepository.updateMemberRole(conversationId, targetUserId, newRole);

        if (!updated) {
            throw new RuntimeException("Failed to update user role. No changes committed.");
        }

        log.info("User {} updated role of member {} to {} in group {}", currentUserId, targetUserId, newRole, conversationId);


        var targetUserObj = userService.getAuthorizedUser(targetUserId);
        broadcastSystemMessage(conversationId, targetUserObj.getDisplayName() + " has been assigned the role: " + newRole);
    }

    private void broadcastSystemMessage(String conversationId, String text) {
        simpMessagingTemplate.convertAndSend("/topic/conversation." + conversationId,
                Map.of("type", "SYSTEM", "content", text, "conversationId", conversationId));
    }

    private void addMember(String conversationId, String userId) {
        var convo = findConversationId(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        conversationUpdateRepository.addMember(convo.getId(), userId, MemberRole.MEMBER);
    }


    public Conversation assertMember(String conversationId, String userId) {
        var convo = findConversationId(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        var isMember = convo.getMembers()
                .stream()
                .anyMatch(member -> member.getUserId().equals(userId) && member.isActive());

        if (!isMember) {
            throw new UnauthorizedException("Not a member of this conversation");
        }
        return convo;
    }


    private Conversation assertAdminOrOwner(String conversationId, String userId) {
        var convo = findConversationId(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        var member = convo.getMembers()
                .stream()
                .filter(member1 -> member1.getUserId().equals(userId))
                .filter(ConversationMember::isActive)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Not a member"));
        if (member.getRole() == MemberRole.MEMBER)
            throw new UnauthorizedException("Requires owner role");
        return convo;
    }

    private Conversation assertOwner(String conversationId, String userId) {
        var convo = findConversationId(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        var member = convo.getMembers()
                .stream()
                .filter(member1 -> member1.getUserId().equals(userId))
                .filter(ConversationMember::isActive)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Not a member"));
        if (member.getRole() != MemberRole.OWNER)
            throw new UnauthorizedException("Requires owner role");
        return convo;
    }

    private Conversation getGroupOrThrow(String id) {
        var c = findConversationId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", id));
        if (c.getConversationType() != ConversationType.GROUP) throw new BadRequestException("Not a group");
        return c;
    }

    private Optional<Conversation> findConversationId(String conversationId) {
        return conversationRepository.findById(conversationId);
    }
    private DirectConversationResponse mapToDirectResponse(String conversationId){
        var conversation = findConversationId(conversationId).orElseThrow(() -> new ResourceNotFoundException("Not found"));

        var members = conversation.getMembers()
                .stream()
                .filter(ConversationMember::isActive).toList();
        var userIds = members.stream()
                .map(ConversationMember::getUserId)
                .toList();

        var users = userService.findUserIn(userIds)
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        Function.identity()
                ));
        return conversationMapper.directResponse(conversation,members, users);
    }
    private ConversationResponse mapToResponse(String conversationId, String viewerId) {
        var conversation = findConversationId(conversationId).orElseThrow(() -> new ResourceNotFoundException("Not found"));
        var members = conversation.getMembers()
                .stream()
                .filter(ConversationMember::isActive).toList();
        var userIds = members.stream()
                .map(ConversationMember::getUserId)
                .toList();

        var users = userService.findUserIn(userIds)
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        Function.identity()
                ));
        var viewerMember = members.stream()
                .filter(member -> member.getUserId().equals(viewerId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Viewer is not a member"));
        long unread;

        if (viewerMember.getLastReadAt() == null) {

            unread = messageRepository.countByConversationIdAndDeletedFalseAndSenderIdNot(
                    conversation.getId(),
                    viewerId
            );

        } else {

            unread = messageRepository
                    .countByConversationIdAndDeletedFalseAndSenderIdNotAndSentAtAfter(
                            conversation.getId(),
                            viewerId,
                            viewerMember.getLastReadAt()
                    );
        }
        var lastMessage = messageRepository
                .findFirstByConversationIdAndDeletedFalseOrderBySentAtDesc(
                        conversation.getId()
                )
                .map(message ->
                        MessageMapper.conversationResponse(
                                message, users.get(message.getSenderId()), encryptionUtil.decryptContent(message.getContent(), message.getContentIv())
                        )
                )
                .orElse(null);
        return conversationMapper.toResponse(conversation,members,users,unread, viewerId);

    }



}
