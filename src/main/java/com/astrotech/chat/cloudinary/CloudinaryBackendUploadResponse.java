package com.astrotech.chat.cloudinary;

import com.astrotech.chat.enums.MediaType;
import lombok.*;

import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CloudinaryBackendUploadResponse {
    private Map<?, ?> result;
    private String secureUrl;
    private String thumbnailUrl;
    private String publicId;
    private String resourceType;
    private String mimeType;
    private MediaType mediaType;
    private long fileSize;
    private String originalName;
    private String checksum;
    private Integer width;
    private Integer height;
    private Integer duration;


}
