package com.astrotech.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditMessageRequest(
        @NotBlank
        @Size(min = 1, max = 800)
        String content
) {
}
