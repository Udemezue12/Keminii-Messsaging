package com.astrotech.chat.mappers;

import com.astrotech.chat.dto.response.MediaResponse;
import com.astrotech.chat.dto.response.MessageResponse;
import com.astrotech.chat.dto.response.ReactionSummary;
import com.astrotech.chat.dto.response.UserResponse;
import com.astrotech.chat.entites.Message;
import com.astrotech.chat.entites.MessageReaction;
import com.astrotech.chat.entites.User;
import com.astrotech.chat.enums.MessageStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



public class MessageMapper {
    public static Message forwardMessageBuilder(String conversationId,String userId,Message message){
        return Message.builder()
                .conversationId(conversationId)
                .senderId(userId)
                .type(message.getType())
                .content(message.getContent())
                .contentIv(message.getContentIv())
                .mediaAttachments(message.getMediaAttachments())
                .forwarded(true)
                .forwardedFromConversationId(message.getConversationId())
                .forwardedByUser(userId)
                .status(MessageStatus.SENT)
                .sentAt(Instant.now())
                .createdAt(Instant.now())
                .build();
    }



    public static MessageResponse mapToResponse(Message m, String plainContent, User user) {

        var senderResp = new UserResponse(
                user.getId(),
                user.getDisplayName(),
                user.getFullName(),
                user.getEmail(),
                user.getStatus(),
                user.getPhoneNumber(),
                user.getRole()
        );
        List<MessageReaction> messageReactions = m.getReactions();

        Map<String, List<MessageReaction>> groupedReactions =
                messageReactions.stream()
                        .collect(Collectors.groupingBy(MessageReaction::getEmoji));

        List<ReactionSummary> reactions = groupedReactions.entrySet()
                .stream()
                .map(entry -> new ReactionSummary(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue()
                                .stream()
                                .map(MessageReaction::getUserId)
                                .toList()
                ))
                .toList();

        List<MediaResponse> media = m.getMediaAttachments().stream()
                .map(a -> new MediaResponse(
                        a.getId(),
                        a.getStorageUrl(),
                        a.getFileName(),
                        a.getMediaType(),
                        a.getDuration(),
                        a.getFileSize())
                )
                .toList();


        MessageResponse.MessageResponseBuilder builder = MessageResponse.builder()
                .id(m.getId())
                .conversationId(m.getConversationId())
                .sender(senderResp)
                .type(m.getType())
                .content(plainContent)
                .edited(m.isEdited())
                .editedAt(m.getEditedAt())
                .deleted(m.isDeleted())
                .reactionsCount(m.getReactionsCount())
                .reactions(reactions)
                .media(media)
                .status(m.getStatus())
                .sentAt(m.getSentAt());
        if (m.getReplyTo() != null) {
            var rt = m.getReplyTo();
            builder.replyTo(MessageResponse.builder()
                    .id(rt.getId())
                    .content(m.getContent())
                    .type(rt.getType())
                    .sender(senderResp)
                    .build());
        }

        return builder.build();
    }
    public static MessageResponse conversationResponse(Message message, User user, String decryptedContent) {
        if (message == null) return null;



        return mapToResponse(message, decryptedContent, user);
    }
}