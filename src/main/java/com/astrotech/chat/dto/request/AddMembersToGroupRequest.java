package com.astrotech.chat.dto.request;

import com.astrotech.chat.validators.uuid.ValidUUID;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AddMembersToGroupRequest(
        @NotBlank( message = " Users are Required")
        @ValidUUID
        List<String> userId
) {
}
