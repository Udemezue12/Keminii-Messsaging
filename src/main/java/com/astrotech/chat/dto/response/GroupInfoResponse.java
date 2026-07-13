package com.astrotech.chat.dto.response;



public record GroupInfoResponse(
        String name,
        String description,
        String avatarUrl,
        String adminId,
        String inviteCode,
        String groupKey,
        Integer maxMembers,
        boolean allowedMembersToEdit,
        boolean allowMembersToSend,
        boolean onlyAdminsCanPost

) {
}
