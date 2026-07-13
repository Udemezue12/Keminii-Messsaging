package com.astrotech.chat.mappers;

import com.astrotech.chat.dto.response.GroupInfoResponse;
import com.astrotech.chat.entites.GroupInfo;

public class GroupInfoMapper {
    public static GroupInfo create(String adminId, String description,String name,String avatarUrl, String inviteCode, Integer maxMembers) {


        return GroupInfo.
                builder()
                .description(description)
                .adminId(adminId)
                .name(name)
                .maxMembers(maxMembers)

                .avatarUrl(avatarUrl)
                .inviteCode(inviteCode)
                .build();
    }
    public static GroupInfoResponse response(GroupInfo groupInfo){
        if (groupInfo == null) {
            return null;
        }



        return new GroupInfoResponse(
                groupInfo.getName(),
                groupInfo.getDescription(),
                groupInfo.getAvatarUrl(),
                groupInfo.getAdminId(),
                groupInfo.getInviteCode(),
                groupInfo.getGroupKey(),
                groupInfo.getMaxMembers(),
                groupInfo.isAllowMembersToEdit(),
                groupInfo.isAllowMembersToSend(),
                groupInfo.isOnlyAdminsCanPost()
        );

    }

}
