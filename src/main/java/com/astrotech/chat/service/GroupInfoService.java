package com.astrotech.chat.service;



import com.astrotech.chat.core.GetPageRequest;
import com.astrotech.chat.customCache.CustomCacheEvict;
import com.astrotech.chat.customCache.CustomCacheable;
import com.astrotech.chat.dto.response.GroupInfoResponse;
import com.astrotech.chat.dto.response.SliceResponse;
import com.astrotech.chat.entites.Conversation;

import com.astrotech.chat.enums.ConversationType;
import com.astrotech.chat.exceptions.BadRequestException;
import com.astrotech.chat.exceptions.ResourceNotFoundException;
import com.astrotech.chat.mappers.GroupInfoMapper;
import com.astrotech.chat.repositories.ConversationRepository;
import com.astrotech.chat.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GroupInfoService {
    private final ConversationRepository conversationRepository;
    private final EncryptionUtil encryptionUtil;


    @Transactional
    @Caching( evict = {@CacheEvict(value = "groupInfo-admin", key = "#userId"),@CacheEvict(value = "groupInfo-user", key = "#userId") })
    public GroupInfoResponse createGroupInfo(String conversationId,String userId,String description, String name, String avatarUrl) {


        var convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation Not Found"));


        if (convo.getConversationType() != ConversationType.GROUP) {
            throw new BadRequestException("Cannot initialize group settings on a direct message channel.");
        }


        if (convo.getGroupInfo() != null) {
            throw new BadRequestException("Group settings have already been initialized for this conversation, use the update button");
        }


        if (!convo.getCreatedById().equals(userId)) {
            throw new AccessDeniedException("Only the conversation creator can initialize group configurations.");
        }
        var inviteCode = regenerateInviteCode(conversationId, userId);

        var groupInfo = GroupInfoMapper.create(
                convo.getCreatedById(),
                description,
                name,
                avatarUrl,
                inviteCode,
                250
        );

        convo.setGroupInfo(groupInfo);
        var savedConvo = conversationRepository.save(convo);

        return GroupInfoMapper.response(savedConvo.getGroupInfo());
    }
    @CustomCacheable(value = "groupInfo-admin",  key = "#userId")
    public SliceResponse<GroupInfoResponse> getGroupsForUser(String userId, int page, int size) {



        var pageable = GetPageRequest.getPageableWithSorting(page, size, "name", true);

        var conversations = conversationRepository.findByMembersUserIdAndGroupInfoNotNull(userId, pageable);


        if (conversations == null || conversations.isEmpty()) {
            return new SliceResponse<>(Collections.emptyList(), page, size, false, false);
        }


        var content = conversations.getContent().stream()
                .map(Conversation::getGroupInfo)
                .filter(Objects::nonNull)
                .map(GroupInfoMapper::response)
                .toList();


        return new SliceResponse<>(
                content,
                page,
                size,
                conversations.hasNext(),
                conversations.hasPrevious()
        );
    }

    @CustomCacheable(value = "groupInfo-admin",  key = "#adminId")
    public SliceResponse<GroupInfoResponse> getGroupsWhereAdmin(String adminId, int page, int size) {

        var pageable = GetPageRequest.getPageableWithSorting(page, size, "name", true);

        var conversations = conversationRepository.findByGroupInfoAdminId(adminId, pageable);


        if (conversations == null || conversations.isEmpty()) {
            return new SliceResponse<>(Collections.emptyList(), page, size, false, false);
        }


        var content = conversations.getContent().stream()
                .map(Conversation::getGroupInfo)
                .filter(Objects::nonNull)
                .map(GroupInfoMapper::response)
                .toList();


        return new SliceResponse<>(
                content,
                page,
                size,
                conversations.hasNext(),
                conversations.hasPrevious()
        );
    }
    @Transactional
    @CustomCacheEvict(cacheNames = "my-conversation", keys = "#conversationId")
    public String regenerateInviteCode(String convId, String userId) {

        var conv = conversationRepository.findByIdAndCreatedById(convId, userId).orElseThrow(() -> new ResourceNotFoundException("Conversation Not Found"));
        var inviteCode = encryptionUtil.generateSecureToken(6).toUpperCase();
        conv.getGroupInfo().setInviteCode(inviteCode);
        conversationRepository.save(conv);
        return conv.getGroupInfo().getInviteCode();
    }


}
