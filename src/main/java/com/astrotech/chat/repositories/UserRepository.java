package com.astrotech.chat.repositories;

import com.astrotech.chat.entites.User;
import com.astrotech.chat.enums.OnlineStatus;
import com.astrotech.chat.enums.Status;
import com.astrotech.chat.projection.EmailValidationProjection;
import com.astrotech.chat.projection.NicknameValidationProjection;
import com.astrotech.chat.projection.PhoneNumberValidationProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;


import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends MongoRepository<User, String> {
    List<User> findAllByStatus(Status status);


    Optional<User> findByEmail(String email);
//    Set<User> findAllByMembers_Id(Set<String> membersId);

    Optional<User> findByNickName(String Nickname);
    Optional<PhoneNumberValidationProjection> findProjectionByPhoneNumber(String phoneNumber);
    Optional<EmailValidationProjection> findProjectionByEmail(String email);
    Optional<NicknameValidationProjection> findProjectionByNickName(String email);

    Optional<User> findBy(String s);

    @Query("{ '$and': [ " +
            "  { '$or': [ { 'nickname': { '$regex': ?0, '$options': 'i' } }, { 'display_name': { '$regex': ?0, '$options': 'i' } } ] }, " +
            "  { 'status': 'ACTIVE' }, " +
            "  { 'deleted_at': null }, " +
            "  { '_id': { '$ne': ?1 } } " +
            "] }")
    List<User> searchUsers(String query, String excludeId);
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'online_status': ?1, 'last_seen': ?2 } }")
    void updateOnlineStatus(String userId, OnlineStatus status, Instant lastSeen);

    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'public_key': ?1 } }")
    void updatePublicKey(String userId, String publicKey);

    List<User> findByIdIn(Collection<String> ids);

    Optional<User> findByPhoneNumber(String phoneNumber);
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': {'last_seen': ?1 } }") 
    void updateLastSeen(String userId, Instant now);

    Slice<User> findByIdIn(List<String> ids, Pageable pageable);
}
