package com.astrotech.chat.events;

import com.astrotech.chat.enums.SendMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WsInboundMessage(
        @NotBlank(message = "Content is required")
        @Size(min = 1, max = 512, message = "Content should not be more than 512 characters")
        String content,
        @NotBlank(message = "Should not be blank")
        String tempId

) {
}
