package com.astrotech.chat.repositories;

import com.astrotech.chat.entites.BlockedUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;


import java.util.Optional;

public interface BlockedUserRepository extends MongoRepository<BlockedUser, String> {
    Slice<BlockedUser> findByBlockerId(String blockerId, Pageable pageable);


    Optional<BlockedUser> findByBlockerIdAndBlockedId(String blockerId, String blockedId);
    boolean existsByBlockerIdAndBlockedId(String blockerId, String blockedId);
    void deleteByBlockerIdAndBlockedId(String blockerId, String blockedId);

}
