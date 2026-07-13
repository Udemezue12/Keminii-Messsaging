package com.astrotech.chat.dto.response;

import com.astrotech.chat.enums.MessageStatus;
import com.astrotech.chat.enums.SendMessageType;

import java.time.Instant;
import java.util.List;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private String id;
    private String conversationId;
    private UserResponse sender;
    private SendMessageType type;
    private String content;
    private MessageResponse replyTo;
    private boolean edited;
    private Instant editedAt;
    private boolean deleted;
    private int reactionsCount;
    private List<ReactionSummary> reactions;
    private List<MediaResponse> media;
    private MessageStatus status;
    private Instant sentAt;

    public record SimpleMessageResponse(String id, String senderId, String decryptedContent,
                                        Instant sentAt) {
    }
}
