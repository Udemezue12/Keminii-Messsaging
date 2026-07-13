package com.astrotech.chat.mappers;

import com.astrotech.chat.dto.response.ConversationMemberResponse;
import com.astrotech.chat.dto.response.ConversationResponse;
import com.astrotech.chat.dto.response.DirectConversationResponse;
import com.astrotech.chat.entites.Conversation;
import com.astrotech.chat.entites.ConversationMember;
import com.astrotech.chat.entites.User;
import com.astrotech.chat.enums.ConversationType;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


@Component
public class ConversationMapper {


    public DirectConversationResponse directResponse(Conversation conversation, List<ConversationMember> members, Map<String, User> users) {
        var responses = getConversationMemberResponses(members, users);
        return new DirectConversationResponse(
                conversation.getId(),
                conversation.getConversationType(),
                conversation.getLastMessageText(),
                responses,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    public ConversationResponse toResponse(Conversation conversation,
                                           List<ConversationMember> members,
                                           Map<String, User> users,
                                           long unread,
                                           String viewerId) {
        var responses = getConversationMemberResponses(members, users);
        var groupInfoName = conversation.getGroupInfo() != null ? conversation.getGroupInfo().getName() : null;
        var inviteCode = conversation.getGroupInfo() != null ? conversation.getGroupInfo().getInviteCode() : null;
        var name = new AtomicReference<>(groupInfoName);

        checkViewerId(conversation, members, users, viewerId, name);


        return new ConversationResponse(
                conversation.getId(),
                conversation.getConversationType(),
                name.get(),
                inviteCode,
                members.size(),
                unread,
                conversation.getLastMessageText(),
                responses,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    private static void checkViewerId(Conversation conversation, List<ConversationMember> members, Map<String, User> users, String viewerId, AtomicReference<String> name) {
        if (conversation.getConversationType() == ConversationType.DIRECT) {
            members.stream()
                    .filter(m -> !m.getUserId().equals(viewerId))
                    .map(m -> users.get(m.getUserId()))
                    .findFirst().ifPresent(other -> name.set(other.getDisplayName()));
        }
    }

    private static @NonNull List<ConversationMemberResponse> getConversationMemberResponses(List<ConversationMember> members, Map<String, User> users) {
        return members.stream()
                .map(member ->
                        new ConversationMemberResponse(
                                member.getUserId(),
                                UserMapper.conversationResponse(users.get(member.getUserId())),
                                member.getRole(),
                                member.isMuted(),
                                member.getJoinedAt()
                        )
                )
                .toList();
    }
}