package com.astrotech.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateGroupRequest(

        @Size(min = 10, max = 256)
        String name,

        @Size(min = 10, max = 800)
        String description,
        String avatarUrl
) {
}
