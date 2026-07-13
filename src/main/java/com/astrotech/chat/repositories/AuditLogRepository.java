package com.astrotech.chat.repositories;

import com.astrotech.chat.entites.AuditLog;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    Slice<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<AuditLog> findByUserIdAndCreatedAtAfter(String userId, Instant after);

}
