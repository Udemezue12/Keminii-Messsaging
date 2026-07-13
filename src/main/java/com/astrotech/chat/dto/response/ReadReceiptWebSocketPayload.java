package com.astrotech.chat.dto.response;

public record ReadReceiptWebSocketPayload(String conversationId, String userId, String lastMessageId) {
}
