package com.astrotech.chat.entites;

import com.astrotech.chat.core.AppGenerators;
import com.astrotech.chat.enums.ConversationType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndexes({
        @CompoundIndex(name = "idx_conversation_member_user", def = "{'_id': 1, 'members.user_id': 1}"),
        @CompoundIndex(name = "idx_member_user_latest_message", def = "{'members.user_id': 1, 'last_message_at': -1}"),
        @CompoundIndex(name = "idx_group_invite_code", def = "{'group_info.invite_code': 1}", unique = true, sparse = true)
})
public class Conversation {

    @Id
    @Builder.Default
    private String id = AppGenerators.generateTimestampedUUID();

    @Indexed
    @Field("type")
    private ConversationType conversationType;

    @Indexed
    @Field("created_by_id")
    private String createdById;

    @Field("is_encrypted")
    @Builder.Default
    private boolean encrypted = true;

    @Field("muted_until")
    @Builder.Default
    private Instant mutedUntil = null;

    @Indexed(name = "idx_members_user_id")
    @Builder.Default
    @Field("members")
    private List<ConversationMember> members = new ArrayList<>();


    @Field("group_info")
    @Builder.Default
    private GroupInfo groupInfo = null;

    @CreatedDate
    @Field("created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    @Field("updated_at")
    @Builder.Default
    private Instant updatedAt = null;

    @Field("deleted_at")
    @Builder.Default
    private Instant deletedAt = null;

    @Indexed(name = "idx_last_message_at")
    @Field("last_message_at")
    @Builder.Default
    private Instant lastMessageAt = null;

    @Field("last_message_text")
    @Builder.Default
    private String lastMessageText = null;

    @Field("last_message_sender_id")
    @Builder.Default
    private String lastMessageSenderId = null;
}