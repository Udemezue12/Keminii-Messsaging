package com.astrotech.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record GroupChatRequest(
        @NotBlank
        @Size(min = 10, max = 256)
        String name,
        @NotBlank
        @Size(min = 10, max = 800)
        String description,
        String avatarUrl,

        @NotNull
        @Size(min = 1, max = 256)
        Set<String> membersId
) {


}
