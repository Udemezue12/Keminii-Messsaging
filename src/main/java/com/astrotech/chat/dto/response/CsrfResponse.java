package com.astrotech.chat.dto.response;

public record CsrfResponse(
        String token,
        String header,
        String parameter
) {
}
