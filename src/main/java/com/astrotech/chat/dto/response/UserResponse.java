package com.astrotech.chat.dto.response;

import com.astrotech.chat.enums.Status;
import com.astrotech.chat.enums.UserRole;
import com.astrotech.chat.enums.UserStatus;

public record UserResponse(
        String Id,
        String displayName,
        String fullName,
        String email,
        UserStatus status,
        String phoneNumber,
        UserRole role
) {
}
