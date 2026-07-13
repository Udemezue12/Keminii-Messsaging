package com.astrotech.chat.dto.response;

import com.astrotech.chat.enums.ConversationType;

import java.time.Instant;
import java.util.List;

public record ConversationResponse(
        String Id,
        ConversationType conversationType,
        String name,
        String inviteCode,
        int memberCount,
        long unreadCount,
        String lastMessage,
        List<ConversationMemberResponse> members,
        Instant createdAt,
        Instant updatedAt

) {
}
