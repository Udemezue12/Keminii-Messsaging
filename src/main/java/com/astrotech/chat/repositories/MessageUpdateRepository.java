package com.astrotech.chat.repositories;

import com.astrotech.chat.entites.Message;
import com.astrotech.chat.entites.MessageReaction;
import com.astrotech.chat.entites.MessageReadReceipt;
import com.astrotech.chat.enums.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class MessageUpdateRepository {


    private final MongoTemplate mongoTemplate;

    public boolean addReaction(
            String messageId,
            MessageReaction reaction
    ) {

        var query = Query.query(
                Criteria.where("_id").is(messageId)
                        .and("reactions")
                        .not()
                        .elemMatch(
                                Criteria.where("userId").is(reaction.getUserId())
                                        .and("emoji").is(reaction.getEmoji())
                        )
        );

        var update = new Update()
                .push("reactions", reaction)
                .inc("reactionsCount", 1);

        var result =
                mongoTemplate.updateFirst(query, update, Message.class);

        return result.getModifiedCount() > 0;
    }


    public boolean removeReaction(
            String messageId,
            String userId,
            String emoji
    ) {

        var query = Query.query(
                Criteria.where("_id").is(messageId)
                        .and("reactions")
                        .elemMatch(
                                Criteria.where("userId").is(userId)
                                        .and("emoji").is(emoji)
                        )
        );

        var update = new Update()
                .pull(
                        "reactions",
                        Query.query(
                                Criteria.where("userId").is(userId)
                                        .and("emoji").is(emoji)
                        )
                )
                .inc("reactionsCount", -1);

        var result =
                mongoTemplate.updateFirst(query, update, Message.class);

        return result.getModifiedCount() > 0;
    }
    public boolean markAsRead(String messageId, String userId) {

        Query query = Query.query(
                Criteria.where("_id").is(messageId)
                        .and("readReceipts.userId").ne(userId)
        );

        var receipt = MessageReadReceipt.builder()
                .userId(userId)
                .readAt(Instant.now())
                .build();

        var update = new Update()
                .addToSet("readReceipts", receipt)
                .set("status", MessageStatus.READ);

       var result =
                mongoTemplate.updateFirst(query, update, Message.class);

        return result.getModifiedCount() > 0;
    }

}
