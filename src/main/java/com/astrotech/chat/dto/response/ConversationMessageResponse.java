package com.astrotech.chat.dto.response;


import com.astrotech.chat.entites.MessageReaction;
import com.astrotech.chat.entites.MessageReadReceipt;
import com.astrotech.chat.enums.MessageStatus;
import com.astrotech.chat.enums.SendMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessageResponse {
    private String id;
    private String conversationId;
    private UserResponse sender; // Assumes UserResponse matches your architecture definitions
    private SendMessageType type;
    private String content;
    private MessageResponse replyTo;
    private String forwardedFromId;
    private boolean edited;
    private Instant editedAt;
    private boolean deleted;
    private boolean deletedForAll;
    private int reactionsCount;
    private MessageStatus status;
    private Instant sentAt;
    private Instant deliveredAt;


    private List<String> mediaUrls;
    private List<MessageReaction> reactions;
    private List<MessageReadReceipt> readReceipts;
}
