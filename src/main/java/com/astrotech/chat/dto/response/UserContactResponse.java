package com.astrotech.chat.dto.response;

public record UserContactResponse(
        String contactName,
        String contact,
        boolean blocked
) {
}
