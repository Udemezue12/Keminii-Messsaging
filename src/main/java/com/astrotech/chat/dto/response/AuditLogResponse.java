package com.astrotech.chat.dto.response;

import java.time.Instant;

public record AuditLogResponse(
        String Id,
        String userId,
        String action,
        String resourceType,
        String resourceId,
        String ipAddress,
        String userAgent,
        String metadata,
        Instant createdAt
) {
}
