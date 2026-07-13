package com.astrotech.chat.dto.request;

import com.astrotech.chat.validators.uuid.ValidUUID;


public record LeaveConversationRequest(
//        @ValidUUID
        String newOwnerId
) {
}
