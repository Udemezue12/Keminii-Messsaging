package com.astrotech.chat.dto.projections;

import com.astrotech.chat.dto.response.MessageResponse;
import com.astrotech.chat.enums.ConversationType;

import java.time.Instant;

public record ConversationSummary(
        String Id,
        ConversationType conversationType,
        String Name,
        String inviteCode,
        Instant createdAt,
        Instant updatedAt,
        long unreadCount

) {
}
