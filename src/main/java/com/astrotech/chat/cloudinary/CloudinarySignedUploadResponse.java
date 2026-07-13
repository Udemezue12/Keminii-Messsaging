package com.astrotech.chat.cloudinary;

import lombok.Builder;

import java.util.List;

@Builder
public record CloudinarySignedUploadResponse(

        String signature,
        Long timestamp,
        String apiKey,
        String folder,
        String publicId,
        String resourceType,
        Long maxFileSize,
        String eager,
        List<String> allowedFormats,
        String cloudName

) {
}
