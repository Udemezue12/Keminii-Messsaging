package com.astrotech.chat.dto.request;

import com.astrotech.chat.enums.SendMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank
        String conversationId,
        @NotNull
        SendMessageType messageType,
        @NotBlank
        @Size(max = 800)
        String content,
        String replyToId,
        String senderId,
        String mediaId

) {
}
