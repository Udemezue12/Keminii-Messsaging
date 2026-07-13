package com.astrotech.chat.dto.response;

import com.astrotech.chat.enums.ConversationType;

import java.time.Instant;
import java.util.List;

public record DirectConversationResponse(
        String Id,
        ConversationType conversationType,
        String lastMessage,
        List<ConversationMemberResponse> members,
        Instant createdAt,
        Instant updatedAt

) {
}
