package com.astrotech.chat.entites;

import com.astrotech.chat.core.AppGenerators;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @Builder.Default
    private String id = AppGenerators.generateTimestampedUUID();

    @Indexed
    @Field("user_id")
    private String userId;

    @Indexed
    private String action;

    @Field("resource_type")
    private String resourceType;

    @Field("resource_id")
    private String resourceId;

    @Field("ip_address")
    private String ipAddress;

    @Field("user_agent")
    private String userAgent;

    @Field("meta_data")
    private String metadata;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;
}

