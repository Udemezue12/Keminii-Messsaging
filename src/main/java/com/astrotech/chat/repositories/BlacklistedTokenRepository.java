package com.astrotech.chat.repositories;

import com.astrotech.chat.entites.BlacklistedToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface BlacklistedTokenRepository extends MongoRepository<BlacklistedToken, String> {
    boolean existsByJti(String jti);

    @Query(value = "{}", delete = true)
    void deleteAllTokens();
}
