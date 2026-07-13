package com.astrotech.chat.core;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class OTPRatelimit {
    private final StringRedisTemplate redisTemplate;
    private static final int OTP_LIMIT_SECONDS = 60;

    public boolean canRequestOtp(String email) {
        var key = buildKey(email);
        return !redisTemplate.hasKey(key);
    }

    public void recordOtp(String email) {
        var key = buildKey(email);
        redisTemplate.opsForValue().set(
                key, "1", Duration.ofSeconds(OTP_LIMIT_SECONDS)
        );
    }

    public Long getRetryAfter(String email) {
        var key = buildKey(email);
        var seconds = redisTemplate.getExpire(key);
        if (seconds < 0) {
            return 0L;
        }
        return seconds;
    }

    private String buildKey(String email) {
        return "otp_limit:" + email;
    }
}

