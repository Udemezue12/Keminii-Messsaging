package com.astrotech.chat.repositories;

import com.astrotech.chat.entites.Conversation;
import com.astrotech.chat.enums.MemberRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;


import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Slice<Conversation> findByMembersUserIdAndGroupInfoNotNull(String userId, Pageable pageable);
    Optional<Conversation> findByGroupInfoInviteCode(String inviteCode);
    Optional<Conversation> findByIdAndCreatedById(String id, String createdById);

    Slice<Conversation> findByGroupInfoAdminId(String adminId, Pageable pageable);
    Slice<Conversation> findByMembersUserIdAndMembersLeftAtNullAndDeletedAtNullOrderByLastMessageAtDesc(String userId, Pageable pageable);

    @Query("{ 'type': 'DIRECT', '$and': [ " +
            "  { 'members': { '$elemMatch': { 'userId': ?0, 'leftAt': null } } }, " +
            "  { 'members': { '$elemMatch': { 'userId': ?1, 'leftAt': null } } } " +
            "] }")
    Optional<Conversation> findDirectConversation(String user1Id, String user2Id);

    @Query(value = "{ '_id': ?0, 'members': { $elemMatch: { 'user_id': ?1, 'left_at': null } } }", exists = true)
    boolean isUserActiveMember(String conversationId, String userId);

    @Query("{ '_id': ?0, 'members.userId': ?1 }")
    @Update("{ '$set': { 'members.$.role': ?2 } }")
    void updateMemberRole(String conversationId, String userId, MemberRole role);
}
