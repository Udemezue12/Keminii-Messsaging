package com.astrotech.chat.entites;



import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupInfo {

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("avatar_url")
    private String avatarUrl;

    @Field("admin_id")
    private String adminId;

    @Field("invite_code")
    private String inviteCode;

    @Field("group_key")
    @Builder.Default
    private String groupKey = null;

    @Field("max_members")
    @Builder.Default
    private Integer maxMembers = 1000;
    @Builder.Default
    private boolean allowMembersToEdit = true;

    @Field("allow_members_to_send")
    @Builder.Default
    private boolean allowMembersToSend = true;

    @Field("disappearing_messages_duration")
    @Builder.Default
    private int disappearingMessagesDuration = 0;

    @Field("only_admins_can_post")
    @Builder.Default
    private boolean onlyAdminsCanPost = false;




}

