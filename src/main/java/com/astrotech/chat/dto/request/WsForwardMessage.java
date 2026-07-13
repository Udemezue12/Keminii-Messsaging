package com.astrotech.chat.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record WsForwardMessage(
        @NotBlank(message = "At least one conversation Id needed")
        List<String> conversationIds
) {
}
