package com.astrotech.chat.repositories;


import com.astrotech.chat.entites.Conversation;
import com.astrotech.chat.entites.Message;
import com.astrotech.chat.entites.MessageMedia;
import com.astrotech.chat.enums.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessageMediaUpdateRepository {

    private final MongoTemplate mongoTemplate;


    public void updateMediaAttachments(
            String messageId,
            MessageMedia media
    ) {

        var query = Query.query(
                Criteria.where("_id").is(messageId)
        );

        var update = new Update().push("mediaAttachments", media);

        mongoTemplate.updateFirst(query, update, Message.class);


    }
}
