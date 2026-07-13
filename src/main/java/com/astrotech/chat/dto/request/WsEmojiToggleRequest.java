package com.astrotech.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WsEmojiToggleRequest(
        @NotBlank(message = "Add an emoji")
        @Size(min = 2, max = 15, message="Exceeded")
        String emoji
) {
}
