package com.astrotech.chat.dto.response;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken

){

}
