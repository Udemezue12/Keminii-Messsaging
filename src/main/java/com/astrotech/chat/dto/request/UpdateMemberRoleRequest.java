package com.astrotech.chat.dto.request;

import com.astrotech.chat.enums.MemberRole;

public record UpdateMemberRoleRequest(
        MemberRole role
) {
}
