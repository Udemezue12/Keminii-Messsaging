package com.astrotech.chat.dto.response;

import com.astrotech.chat.enums.MediaType;

public record MediaResponse(
        String publicId,
        String secureUrl,
        String originalName,
        MediaType mediaType,
        Integer duration,
        Long fileSize

) {
}
