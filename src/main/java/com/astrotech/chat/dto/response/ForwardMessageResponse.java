package com.astrotech.chat.dto.response;

public record ForwardMessageResponse(
        String conversationId,
        String messageId
) {
}
