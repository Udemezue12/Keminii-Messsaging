package com.astrotech.chat.dto.request;

public record VerifyEmailRequest(
        String otp,
        String token
) {
}
