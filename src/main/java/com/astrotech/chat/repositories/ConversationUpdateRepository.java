package com.astrotech.chat.repositories;

import com.astrotech.chat.entites.Conversation;
import com.astrotech.chat.entites.ConversationMember;
import com.astrotech.chat.enums.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConversationUpdateRepository {

    private final MongoTemplate mongoTemplate;


    public boolean updateMemberRole(
            String conversationId,
            String userId,
            MemberRole role
    ) {

        var query = Query.query(
                Criteria.where("_id").is(conversationId).and("members").elemMatch(Criteria.where("user_id").is(userId))
        );

        var update = new Update()
                .set("members.$.role", role);

        var result =
                mongoTemplate.updateFirst(
                        query,
                        update,
                        Conversation.class
                );

        return result.getModifiedCount() > 0;
    }

    public boolean leaveConversation(
            String conversationId,
            String userId
    ) {

        var query = Query.query(
                Criteria.where("_id").is(conversationId).and("members").elemMatch(Criteria.where("user_id").is(userId))

        );

        var update = new Update()
                .set("members.$.left_at", Instant.now())
                .set("members.$.is_active", false);

        return mongoTemplate
                .updateFirst(query, update, Conversation.class)
                .getModifiedCount() > 0;
    }
    public boolean removeMember(
            String conversationId,
            String userId
    ) {

        var query = Query.query(Criteria.where("_id").is(conversationId)
                .and("members").elemMatch(Criteria.where("user_id").is(userId)
                        .orOperator(
                                Criteria.where("role").is(MemberRole.MEMBER),
                                Criteria.where("role").is(MemberRole.ADMIN)
                        )
                )
        );

        var update = new Update()
                .set("members.$.left_at", Instant.now())
                .set("members.$.is_active", false);

        return mongoTemplate
                .updateFirst(query, update, Conversation.class)
                .getModifiedCount() > 0;
    }

    public boolean joinByInviteCodeIfExisting(String conversationId, String userId, String code) {
        var query = Query.query(
                Criteria.where("_id").is(conversationId)
                        .and("members").elemMatch(Criteria.where("user_id").is(userId))
                        .and("group_info.invite_code").is(code)
        );
        var update = new Update()
                .set("members.$.left_at", null)
                .set("members.$.joined_at", Instant.now())
        .set("members.$.is_active", true);
        return mongoTemplate.updateFirst(
                query, update, Conversation.class
        ).getModifiedCount() > 0;
    }
    public boolean addMember(String conversationId, String userId, MemberRole role) {

        var query = Query.query(Criteria.where("_id").is(conversationId));

        var newMember = ConversationMember.builder()
                .userId(userId)
                .role(role)
                .isActive(true)
                .build();


        var update = new Update().push("members", newMember);

        return mongoTemplate.updateFirst(
                query, update, Conversation.class
        ).getModifiedCount() > 0;
    }
    public boolean addMembers(String conversationId, List<ConversationMember> newMembers) {
        if (newMembers == null || newMembers.isEmpty()) {
            return false;
        }


        var query = Query.query(Criteria.where("_id").is(conversationId));


        var update = new Update().push("members").each(newMembers.toArray());

        return mongoTemplate.updateFirst(query, update, Conversation.class).getModifiedCount() > 0;
    }

    public boolean promoteMember(String conversationId, String targetUserId, String currentOwnerId, MemberRole role) {
        var query = Query.query(
                Criteria.where("_id").is(conversationId)
                        .and("created_by_id").is(currentOwnerId)
                        .and("members").elemMatch(
                                Criteria.where("user_id").is(targetUserId)
                                        .and("is_active").is(true)
                        )
        );


        var update = new Update()
                .set("members.$.role", role);
        return mongoTemplate.updateFirst(
                query, update, Conversation.class
        ).getModifiedCount() > 0;
    }

    public boolean transferOwnership(String conversationId, String currentOwnerId, String newOwnerId) {
        var query = Query.query(
                Criteria.where("_id").is(conversationId).and("created_by_id").is(currentOwnerId)
        );
        var update = new Update()
                .set("created_by_id", newOwnerId)
                .set("updated_at", Instant.now())
                .set("members.$[oldOwner].role", MemberRole.ADMIN)
                .set("members.$[newOwner].role", MemberRole.OWNER);

        update.filterArray(
                Criteria.where("oldOwner.user_id").is(currentOwnerId)
        );

        update.filterArray(
                Criteria.where("newOwner.user_id").is(newOwnerId)
        );
        return mongoTemplate.updateFirst(
                query,
                update,
                Conversation.class
        ).getModifiedCount() > 0;
    }
    public boolean pinConversation(String conversationId, String userId, boolean pinned) {

        var query = Query.query(
                Criteria.where("_id").is(conversationId)
                        .and("members").elemMatch(Criteria.where("user_id").is(userId)).and("members").elemMatch(Criteria.where("left_at").is(null))
        );

        var update = new Update()
                .set("members.$.pinned", pinned);

        var result = mongoTemplate.updateFirst(
                query,
                update,
                Conversation.class
        );

        return result.getModifiedCount() > 0;
    }





    public boolean muteConversation(String conversationId, String userId, boolean mute) {

       var query = Query.query(
                Criteria.where("_id").is(conversationId)
                        .and("members").elemMatch(Criteria.where("user_id").is(userId)).and("members").elemMatch(Criteria.where("left_at").is(null))
        );

        var update = new Update()
                .set("members.$.muted", mute)
                .unset("members.$.muted_until");

        var result = mongoTemplate.updateFirst(
                query,
                update,
                Conversation.class
        );

        return result.getModifiedCount() > 0;
    }



    public boolean muteConversationUntil(
            String conversationId,
            String userId,
            Instant mutedUntil
    ) {

        Query query = Query.query(
                Criteria.where("_id").is(conversationId)
                        .and("members").elemMatch(Criteria.where("user_id").is(userId)).and("members").elemMatch(Criteria.where("left_at").is(null))
        );

        Update update = new Update()
                .set("members.$.muted", true)
                .set("members.$.muted_until", mutedUntil);

        var result = mongoTemplate.updateFirst(
                query,
                update,
                Conversation.class
        );

        return result.getModifiedCount() > 0;
    }







    public boolean markConversationAsRead(String conversationId, String userId, String lastMessageId) {

        var query = new Query(
                Criteria.where("_id").is(conversationId)
                        .and("members").elemMatch(Criteria.where("user_id").is(userId))
        );


        var update = new Update()
                .set("members.$.last_read_message_id", lastMessageId)
                .set("members.$.last_read_at", Instant.now());

        var result =mongoTemplate.updateFirst(query, update, Conversation.class);
        return result.getModifiedCount() > 0;

    }
    public boolean archiveConversation(String conversationId, boolean oldArchived, boolean newArchived) {

        Query query = Query.query(
                Criteria.where("_id").is(conversationId)
                        .and("archived").is(oldArchived)
        );

        var update = new Update()
                .set("archived", newArchived);

        var result = mongoTemplate.updateFirst(
                query,
                update,
                Conversation.class
        );

        return result.getModifiedCount() > 0;
    }


    public boolean updateLastReadAt(
            String conversationId,
            String userId,
            Instant lastReadAt
    ) {

        var query = Query.query(
                Criteria.where("_id").is(conversationId).and("members").elemMatch(Criteria.where("user_id").is(userId)).and("members").elemMatch(Criteria.where("left_at").is(null))
        );

        var update = new Update()
                .set("members.$.last_read_at", lastReadAt);

        var result = mongoTemplate.updateFirst(
                query,
                update,
                Conversation.class
        );

        return result.getModifiedCount() > 0;
    }



}
