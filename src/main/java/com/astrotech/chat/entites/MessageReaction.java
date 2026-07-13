package com.astrotech.chat.entites;

import com.astrotech.chat.core.AppGenerators;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import lombok.*;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageReaction {
    @Id
    @Builder.Default
    private String id = AppGenerators.generateTimestampedUUID();
    private String userId;
    private String emoji;
    private Instant createdAt;
}
