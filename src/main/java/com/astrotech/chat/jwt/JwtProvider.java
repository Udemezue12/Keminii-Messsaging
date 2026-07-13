package com.astrotech.chat.jwt;


import com.astrotech.chat.config.JwtConfig;
import com.astrotech.chat.dto.response.TokenData;
import com.astrotech.chat.enums.JwtType;
import com.astrotech.chat.enums.UserRole;


import com.astrotech.chat.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;



import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {
    private final JwtConfig jwtConfig;
    private final Jwt jwt;

    public String generateAccessToken(String userId, String displayName, String sessionId, UserRole role, boolean emailVerified) {
        Map<String, Object> claims = new HashMap<>();
        var expiry = jwtConfig.getAccessExpiration() * 1000;
        var now = System.currentTimeMillis();
        claims.put("type", JwtType.ACCESS.toString());
        claims.put("emailVerified", emailVerified);
        claims.put("displayName", displayName);
        claims.put("sessionId", sessionId);
        claims.put("role", role.toString());
        return  jwt.buildToken(claims, userId,  jwtConfig.getSecretKey(), new Date(now + expiry));

    }
    public String generateRefreshToken(String userId, String displayName, String sessionId, UserRole role, boolean emailVerified) {
        Map<String, Object> claims = new HashMap<>();
        var expiry = jwtConfig.getRefreshExpiration() * 1000;
        var now = System.currentTimeMillis();
        claims.put("type", JwtType.REFRESH.toString());
        claims.put("emailVerified", emailVerified);
        claims.put("displayName", displayName);
        claims.put("sessionId", sessionId);
        claims.put("role", role.toString());

        return  jwt.buildToken(claims, userId,  jwtConfig.getSecretKey(),  new Date(now + expiry));

    }



    public TokenData extractClaims(String token, JwtType expectedType) {
        return jwt.validateAndExtractClaims(token)
                .map(claims -> {
                    JwtType actualType;
                    try {
                        actualType = JwtType.valueOf(
                                claims.get("type", String.class)
                        );
                    } catch (Exception ex) {
                        throw new BadRequestException("Invalid token type.");
                    }

                    if (actualType != expectedType) {
                        throw new BadRequestException(
                                "Expected %s token but received %s token."
                                        .formatted(expectedType, actualType)
                        );
                    }
                    var emailVerified = Boolean.TRUE.equals(
                            claims.get("emailVerified", Boolean.class));

                    return new TokenData(
                            claims.getId(),
                            claims.getSubject(),
                            claims.getExpiration(),
                            true,
                            UserRole.valueOf(claims.get("role", String.class)),
                            actualType,
                            claims.getExpiration().before(new Date()),
                            claims.get("sessionId", String.class),
                            emailVerified,
                            claims.get("displayName", String.class)
                    );
                })
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));
    }









}
