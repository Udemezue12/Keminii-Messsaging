package com.astrotech.chat.entites;
import com.astrotech.chat.enums.MemberRole;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMember {

    @Field("user_id")
    private String userId;


    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;

    @Field("nick_name")
    private String nickname;
    @Field("muted")
    @Builder.Default
    private boolean muted = false;

    @Field("muted_until")
    @Builder.Default

    private Instant mutedUntil = null;

    @Field("pinned_at")
    @Builder.Default
    private boolean pinned = false;

    @Field("last_read_at")
    @Builder.Default

    private Instant lastReadAt = null;
    @Field("last_read_message_id")
    @Builder.Default
    private String lastReadMessageId = null;


    @Field("archived")
    @Builder.Default
    private boolean archived = false;
    @CreatedDate
    @Field("joined_at")
    @Builder.Default
    private Instant joinedAt = Instant.now();
    @Field("archived_at")
    @Builder.Default
    private Instant archivedAt = null;
    @Field("left_at")
    @Builder.Default
    private Instant leftAt = null;
    @Field("is_active")
    @Builder.Default
    private Boolean isActive = true;


    public boolean isActive() {
        return leftAt == null;
    }
}
