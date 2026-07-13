package com.astrotech.chat.jwt;

import com.astrotech.chat.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class Jwt {
    private final JwtConfig jwtConfig;

    public String buildToken(Map<String, Object> claims, String subject, SecretKey secretKey, Date expiration) {
        var now = System.currentTimeMillis();
        var jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .claims(claims)
                .subject(subject)
                .issuer(jwtConfig.getIssuer())
                .issuedAt(new Date(now))
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public Claims getClaims(String token, SecretKey secretKey) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {

            throw new RuntimeException("Token has expired", e);
        } catch (SignatureException e) {

            throw new RuntimeException("Invalid token signature", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Malformed JWT token", e);
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("Unsupported JWT token", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT claims string is empty", e);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while processing the token", e);
        }
    }

    public Optional<Claims> validateAndExtractClaims(String token) {
        try {
            var claims = getClaims(token, jwtConfig.getSecretKey());

            if (claims.getExpiration().before(new Date())) {
                log.warn("JWT token is expired");
                return Optional.empty();
            }
            return Optional.of(claims);

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
