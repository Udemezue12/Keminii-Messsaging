package com.astrotech.chat.entites;

import com.astrotech.chat.core.AppGenerators;
import com.astrotech.chat.enums.JwtType;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken {

    @Id
    @Builder.Default
    private String id = AppGenerators.generateTimestampedUUID();

    @Field("user_id")
    private String userId;

    @Field(name = "jti")
    private String jti;


    @Field(name = "jwtType")
    private JwtType tokenType;

    @Field(name = "expiresAt")
    @Builder.Default
    private OffsetDateTime expiresAt = OffsetDateTime.now();

    @Field(name = "revokedAt")
    @Builder.Default
    private OffsetDateTime revokedAt = OffsetDateTime.now();


}
