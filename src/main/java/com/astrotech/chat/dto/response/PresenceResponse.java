package com.astrotech.chat.dto.response;

import com.astrotech.chat.enums.OnlineStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PresenceResponse{
    private String userId;
    private OnlineStatus status;
    private Instant lastSeen;
}
