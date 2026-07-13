package com.astrotech.chat.entites;
import com.astrotech.chat.enums.MediaType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "message_media")
public class MessageMedia {
    @Id
    @Builder.Default

    private String id = UUID.randomUUID().toString();
    private MediaType mediaType;
    private String fileName;

    private String mimeType;
    private Long fileSize;
    private String storageUrl;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private Integer duration;
    private String checksum;
    private String fileHash;
    private String publicId;
    private Instant createdAt;
    private String message;
}