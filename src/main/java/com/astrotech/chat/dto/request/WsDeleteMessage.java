package com.astrotech.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record WsDeleteMessage(
        @NotNull(message = "Required")
        Boolean deleteForAll
) {
}
