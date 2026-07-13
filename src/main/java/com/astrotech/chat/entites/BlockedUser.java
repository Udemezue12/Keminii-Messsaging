package com.astrotech.chat.entites;
import com.astrotech.chat.core.AppGenerators;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "blocked_users")
@CompoundIndex(name = "blocker_blocked_unique_idx", def = "{'blockerId': 1, 'blockedId': 1}", unique = true)
public class BlockedUser {
    @Id
    @Builder.Default
    private String id = AppGenerators.generateTimestampedUUID();
    @Field("blocker_id")
    private String blockerId;

    @Field("blocked_id")
    private String blockedId;

    @CreatedDate
    @Field("created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();


}
