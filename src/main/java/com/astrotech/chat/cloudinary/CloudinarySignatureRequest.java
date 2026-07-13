package com.astrotech.chat.cloudinary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CloudinarySignatureRequest(
        @NotBlank(message = "Conversation ID is required")
        String conversationId,

        @NotNull(message = "File size is required")
        @Positive(message = "File size must be greater than zero")
        Long fileSize,

        @NotBlank(message = "MIME type is required")
        String mimeType,

        @NotBlank(message = "File name is required")
        String fileName
) {}
