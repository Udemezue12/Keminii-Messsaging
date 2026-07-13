package com.astrotech.chat.entites;

import com.astrotech.chat.core.AppGenerators;
import com.astrotech.chat.enums.MessageStatus;

import com.astrotech.chat.enums.SendMessageType;
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

@Document(collection = "messages")
@CompoundIndexes({
        @CompoundIndex(name = "idx_msg_conv_sent", def = "{'conversationId': 1, 'sentAt': -1}"),
        @CompoundIndex(name = "idx_msg_conv_status", def = "{'conversationId': 1, 'status': 1}")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @Builder.Default

    private String id= AppGenerators.generateTimestampedUUID();

    @Indexed(name = "idx_msg_conversation_id")
    @Field("conversation_id")
    private String conversationId;

    @Indexed(name = "idx_msg_sender_id")
    @Field("sender_id")
    private String senderId;

    @Field("type")
    @Builder.Default
    private SendMessageType type = SendMessageType.TEXT;
    @Field("content")
    @Builder.Default
    private String content = null;

    @Field("content_iv")
    @Builder.Default
    private String contentIv = null;


    @Indexed(name = "idx_msg_reply_to_id", sparse = true)
    @Field("reply_to")
    private Message replyTo;

    @Field("is_forwarded")
    @Builder.Default
    private boolean forwarded = false;

    @Field("forwarded_from_conversation_id")
    @Builder.Default
    private String forwardedFromConversationId = null;

    @Field("forwarded_by_user_id")
    @Builder.Default
    private String forwardedByUser = null;

    @Field("is_edited")
    @Builder.Default
    private boolean edited = false;
    @Field("is_delivered")
    @Builder.Default
    private boolean delivered = false;

    @Field("edited_at")
    @Builder.Default
    private Instant editedAt = null;

    @Field("is_deleted")
    @Builder.Default
    private boolean deleted = false;

    @Field("deleted_for_all")
    @Builder.Default
    private boolean deletedForAll = false;

    @Field("reactions_count")
    @Builder.Default
    private int reactionsCount = 0;

    @Indexed(name = "idx_msg_status")
    @Field("status")
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    @CreatedDate
    @Field("sent_at")
    private Instant sentAt;

    @Field("delivered_at")
    private Instant deliveredAt;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    @Builder.Default
    private Instant updatedAt = null;

    @Indexed(name = "idx_media_public_id")
    @Builder.Default
    @Field("media_attachments")
    private List<MessageMedia> mediaAttachments = new ArrayList<>();

    @Builder.Default
    private List<MessageReaction> reactions = new ArrayList<>();
    @Builder.Default
    private List<MessageReadReceipt> readReceipts = new ArrayList<>();

}
