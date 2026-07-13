package com.astrotech.chat.entites;


import com.astrotech.chat.core.AppGenerators;
import com.astrotech.chat.enums.OnlineStatus;
import com.astrotech.chat.enums.UserRole;
import com.astrotech.chat.enums.UserStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@Setter
@Builder
@Document(collection = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Field(name = "full_name")
    private String fullName;

    @Id
    @Builder.Default
    private String id = AppGenerators.generateTimestampedUUID();
    @Field("nickname")
    @Indexed(unique = true)
    private String nickName;
    @Field("display_name")
    private String displayName;
    @Field(name = "status")
    private UserStatus status;
    @Field(name = "role")
    private UserRole role;
    @Field(name ="email")
    @Indexed(unique = true)
    private String email;
    @Field(name = "phone_number")
    @Indexed(unique = true)
    private String phoneNumber;
    @Field(name = "password")
    private String password;
    @Field(name = "last_seen")
    private Instant lastSeen;
    @Field(name = "online_status")
    @Builder.Default
    private OnlineStatus onlineStatus= OnlineStatus.OFFLINE;
    @Field(name = "two_fa_enabled")
    @Builder.Default
    private boolean twoFaEnabled = false;

    @Field(name = "public_key")
    @Builder.Default
    private String publicKey = null;



    @Field("two_fa_secret")
    @Builder.Default
    private String twoFaSecret = null;
    @Field(name = "suspended")
    @Builder.Default
    private boolean suspended = false;
    @Field(name = "deleted")
    @Builder.Default
    private boolean deleted = false;
    @Field(name = "verified")
    @Builder.Default
    private boolean verified = false;

    @Field(name = "verified_at")
    private Instant verifiedAt;
    @Field(name = "deleted_at")
    private Instant deletedAt;
    @Field(name = "suspended_at")
    private Instant suspendedAt;
    @Field(name = "login_at")
    private Instant lastLoginAt;
    @CreatedDate
    @Field(name = "created_at")
    private Instant createdAt;




}
