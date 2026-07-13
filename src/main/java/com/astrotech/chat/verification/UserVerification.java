package com.astrotech.chat.verification;

import com.astrotech.chat.configProperties.AuthTokenProperties;
import com.astrotech.chat.core.GetSecretKey;
import com.astrotech.chat.core.TrimWhiteSpace;
import com.astrotech.chat.enums.JwtType;
import com.astrotech.chat.jwt.Jwt;

import io.jsonwebtoken.JwtException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserVerification {

    private final AuthTokenProperties properties;

    private final StringRedisTemplate redisTemplate;



    private final Random random = new Random();
    private final Jwt jwtService;

    public String generateVerifyToken(String email) {
        var trimmedEmail = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(email, false);
        var key = GetSecretKey.getKeys(properties.getVerifyEmailSecretKey());


        return jwtService.buildToken(
                Map.of("type", JwtType.VERIFY_EMAIL.toString()),
                trimmedEmail,
                key,
                new Date( System.currentTimeMillis() + 300_000));
    }

    public String generateResetToken(String email) {
        var key = GetSecretKey.getKeys(properties.getResetSecretKey());
        var trimmedEmail = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(email, false);
        return jwtService.buildToken(
                Map.of("type", JwtType.RESET_EMAIL.toString()),
                trimmedEmail,
                key,
                new Date(System.currentTimeMillis()
                + 3000_000));
    }

    public String generateOtp(String email) {
        var otp = String.valueOf(100000 + random.nextInt(900000));
        redisTemplate.opsForValue().set(
                "otp:" + email,
                otp,
                Duration.ofMinutes(5)
        );

        return otp;
    }

    public String verifyOtp(String otp) {

        var keys =
                redisTemplate.keys("otp:*");

        if (keys.isEmpty()) {
            return null;
        }

        for (String key : keys) {

            var storedOtp =
                    redisTemplate.opsForValue().get(key);

            if (otp.equals(storedOtp)) {

                redisTemplate.delete(key);

                return key.replace("otp:", "");
            }
        }

        return null;
    }

    public String verifyResetToken(String token) {

        try {
            var key = GetSecretKey.getKeys(properties.getResetSecretKey());

            var claims = jwtService.getClaims(token, key);
            return claims.getSubject();

        } catch (JwtException ex) {
            return null;
        }
    }

    public String verifyVerifyToken(String token) {

        try {
            var key = GetSecretKey.getKeys(properties.getVerifyEmailSecretKey());

            var claims = jwtService.getClaims(token, key);
            return claims.getSubject();

        } catch (JwtException ex) {
            return null;
        }
    }





}
