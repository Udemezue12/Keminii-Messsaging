package com.astrotech.chat.repositories;

import com.astrotech.chat.entites.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends MongoRepository<Message, String> {

    Slice<Message> findByConversationIdAndDeletedFalseOrderBySentAtDesc(String conversationId, Pageable pageable);
    Optional<Message> findFirstByConversationIdInAndDeletedFalseOrderBySentAtDesc(String conversationId);

    @Query("{ 'conversation_id': ?0, 'sent_at': { '$lt': ?1 }, 'deleted': false }")
    Slice<Message> findMessagesBefore(String conversationId, Instant beforeSentAt, Pageable pageable);


    @Query(value = "{ 'conversation_id': ?0, 'sender_id': { '$ne': ?1 }, 'deleted': false, 'readReceipts.userId': { '$ne': ?1 } }", count = true)
    long countUnread(String conversationId, String userId);

    @Query("{ 'conversation_id': ?0, 'content': { '$regex': ?1, '$options': 'i' }, 'deleted': false }")
    Page<Message> searchMessages(String conversationId, String query, Pageable pageable);
    long countByConversationIdAndDeletedFalseAndSenderIdNotAndSentAtAfter(
            String conversationId,
            String senderId,
            Instant sentAt
    );

    long countByConversationIdAndDeletedFalseAndSenderIdNot(
            String conversationId,
            String senderId
    );
    @Query("{ 'conversation_id': ?0, 'sender_id': { '$ne': ?1 }, 'status': 'SENT' }")
    @Update("{ '$set': { 'status': 'DELIVERED', 'deliveredAt': ?2 } }")
    void markDelivered(String conversationId, String userId, Instant currentTime);

    Optional<Message> findFirstByConversationIdAndDeletedFalseOrderBySentAtDesc(String id);

    boolean existsByMediaAttachmentsPublicId(String publicId);

    boolean existsByReplyToIdAndDeletedFalse(String messageId);

    Slice<Message> findByConversationIdOrderBySentAtDesc(String conversationId, Pageable pageable);

    Optional<Message> findByIdAndDeletedFalse(String messageId);
    @Query("{ '_id': { $in: ?0 } }")
    @Update("{ '$set': { 'delivered': true } }")
    void bulkMarkAsDelivered(List<String> ids);
    @Query(value = "{ '_id': ?0, 'reactions.userId': ?1 }", fields = "{ 'reactions.$': 1 }")
    Optional<MessageReactionProjection> findUserReaction(String messageId, String userId);

}

