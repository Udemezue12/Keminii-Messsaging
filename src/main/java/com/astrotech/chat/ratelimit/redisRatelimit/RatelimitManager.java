package com.astrotech.chat.ratelimit.redisRatelimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

//import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatelimitManager {
    private final StringRedisTemplate redisTemplate;

    public String getIdentifier(HttpServletRequest request) {
//        try {
//            var auth = SecurityContextHolder.getContext().getAuthentication();
//            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
//                return "user:" + auth.getName();
//            }
//        } catch (Exception ignored) {
//
//        }
        try {
            String ip = request.getRemoteAddr();
            if (ip != null) {
                return "ip:" + ip;

            }
        } catch (Exception ignored) {

        }
        return "anonymous";
    }

    public boolean isRateLimited(
            String key,
            int times,
            int seconds) {

        try {

            String redisKey = "rate_limit:" + key;

            long now = System.currentTimeMillis();

            long windowStart =
                    now - (seconds * 1000L);

            var zSet = redisTemplate.opsForZSet();

            zSet.removeRangeByScore(
                    redisKey,
                    0,
                    windowStart);

            zSet.add(
                    redisKey,
                    String.valueOf(now),
                    now);

            Long count =
                    zSet.zCard(redisKey);

            redisTemplate.expire(
                    redisKey,
                    Duration.ofSeconds(seconds));

            return count != null &&
                    count > times;

        } catch (Exception e) {

            log.error(
                    "Rate Limit Check failed",
                    e);

            return false;
        }
    }

}
