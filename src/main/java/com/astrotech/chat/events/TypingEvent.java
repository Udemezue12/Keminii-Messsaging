package com.astrotech.chat.events;

import java.time.Instant;

public record TypingEvent(
        String userId,
        String username,
        String conversationId,
        boolean isTyping,
        Instant timestamp
) {
}
