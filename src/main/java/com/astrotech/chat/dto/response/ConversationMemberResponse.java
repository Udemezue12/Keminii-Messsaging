package com.astrotech.chat.dto.response;

import com.astrotech.chat.enums.MemberRole;

import java.time.Instant;

public record ConversationMemberResponse(
        String Id,
        UserResponse user,
        MemberRole role,
        boolean muted,
        Instant joinedAt
) {
}
