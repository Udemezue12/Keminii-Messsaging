package com.astrotech.chat.events;

import com.astrotech.chat.enums.MediaType;

public record MediaUploadEvent(String secureUrl, String mediaType, String originalName, String mimeType,
                               long fileSize, String thumbnailUrl, Integer width, Integer height, Integer duration,
                               String checksum, String publicId, String fileHash, String messageId )  {
}
