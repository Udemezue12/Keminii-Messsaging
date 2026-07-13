package com.astrotech.chat.dto.response;

import java.time.Instant;

public record PingResult(
        String url,
        int statusCode,
        long responseTimeMs,
        boolean healthy,
        Instant timestamp,
        String message

) {
}
