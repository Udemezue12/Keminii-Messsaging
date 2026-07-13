package com.astrotech.chat.dto.request;

import java.security.Principal;

public record AuthenticatedUser(
        String userId,
        String role,
        String displayName,
        boolean emailVerified
) implements Principal {

    @Override
    public String getName() {
        return userId;
    }
}
