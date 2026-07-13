package com.astrotech.chat.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WsEditMessage(
        @NotBlank(message = "Content is required")
        @Size(min = 1, max = 512, message = "Content should not be more than 512 characters")
        String content
) {
}
