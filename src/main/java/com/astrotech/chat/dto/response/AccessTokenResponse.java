package com.astrotech.chat.dto.response;

public record AccessTokenResponse(
        RefreshTokenResponse tokenResponse,
        String Id,
        String displayName,
        String email,
        String phoneNumber,
        boolean verified,
        String sessionId
        ) {

}
