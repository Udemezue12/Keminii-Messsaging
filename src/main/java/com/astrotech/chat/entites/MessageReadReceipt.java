package com.astrotech.chat.entites;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageReadReceipt{

    private String userId;

    @Builder.Default
    private Instant readAt = Instant.now();
}
