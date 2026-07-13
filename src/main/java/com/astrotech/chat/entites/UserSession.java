package com.astrotech.chat.entites;



import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;

import java.time.Instant;


@Document(collection = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Indexed(unique = true)
    @Field("session_key")
    private String sessionKey;

    @Field("access_token")
    private String accessToken;

    @Field("refresh_token")
    private String refreshToken;
    @Field("device_type")
    @Builder.Default
    private String deviceType = null;

    @Field("device_name")
    @Builder.Default
    private String deviceName = null;
    @Field("device_info")
    @Builder.Default
    private String deviceInfo = null;


    @Field("push_token")
    @Builder.Default
    private String pushToken = null;

    @Field("ip_address")
    private String ipAddress;

    @Field("user_agent")
    private String userAgent;

    @Field("is_active")
    @Builder.Default
    private boolean isActive = true;

    @Field("last_active_at")
    private Instant lastActiveAt;

    @CreatedDate
    @Field("created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
