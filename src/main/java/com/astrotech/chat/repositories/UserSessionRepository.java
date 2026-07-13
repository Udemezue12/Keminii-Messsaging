package com.astrotech.chat.repositories;

import com.astrotech.chat.entites.UserSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.*;

public interface UserSessionRepository extends MongoRepository<UserSession, String> {
    List<UserSession> findByUserIdAndActiveTrue(String userId);


    Optional<UserSession> findBySessionKey(String sessionKey);

    @Query("{'_id': ?0}")
    @Update("{ '$set': { 'is_active': false } }")
    void deactivate(String id);
    @Query("{ 'user_id': ?0 }")
    @Update("{ '$set': { 'is_active': false } }")
    void deactivateAllForUser(String userId);


    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'last_active_at': { '$currentDate': true } } }")
    void updateLastActive(String id);
}
