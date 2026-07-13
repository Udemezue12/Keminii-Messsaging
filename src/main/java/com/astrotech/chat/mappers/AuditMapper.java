package com.astrotech.chat.mappers;

import com.astrotech.chat.dto.response.AuditLogResponse;
import com.astrotech.chat.entites.AuditLog;
import org.jspecify.annotations.NonNull;

import java.time.Instant;

public class AuditMapper {
    public static AuditLogResponse logResponse(String Id, String userId, String action, String resourceType, String resourceId, String ipAddress, String userAgent, String metaData, Instant createdAt){
        return new AuditLogResponse(
                Id,
                userId,
                action,
                resourceType,
                resourceId,
                ipAddress,
                userAgent,
                metaData,
                 createdAt
        );
    }
    public static  AuditLogResponse getAuditLogResponse(AuditLog a) {
        return new AuditLogResponse(
                a.getId(),
                a.getUserId(),
                a.getAction(),
                a.getResourceType(),
                a.getResourceId(),
                a.getIpAddress(),
                a.getUserAgent(),
                a.getMetadata(),
                a.getCreatedAt()
        );}
}
