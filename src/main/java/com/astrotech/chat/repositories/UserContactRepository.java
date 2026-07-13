package com.astrotech.chat.repositories;

import com.astrotech.chat.entites.UserContact;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
import java.util.Optional;

public interface UserContactRepository extends MongoRepository<UserContact, String> {

    Slice<UserContact> findByOwnerId(String ownerId, Pageable pageable);
    Optional<UserContact> findByIdAndOwnerId(String Id, String ownerId);



    boolean existsByOwnerIdAndId(String ownerId, String Id);
    boolean existsByOwnerIdAndContact(String contactName, String contact);

    void deleteByOwnerIdAndId(String ownerId, String ContactId);
    @Query("{ 'owner_id': ?0, 'contact': ?1 }")
    @Update("{ '$set': { 'blocked': ?2 } }")
    void updateBlockStatus(String ownerId, String contactId, boolean isBlocked);
}
