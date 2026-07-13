package com.astrotech.chat.events;

import com.astrotech.chat.dto.response.MessageResponse;
import com.astrotech.chat.enums.MessageEventType;

import java.time.Instant;

public record MessageEvent(
        MessageEventType eventType,
        MessageResponse message,
        String conversationId,
        Instant timestamp
) {
    public record MediaMessageEvent(
            MessageEventType eventType,

            String conversationId,
            Instant timestamp
    ){

    }
}
