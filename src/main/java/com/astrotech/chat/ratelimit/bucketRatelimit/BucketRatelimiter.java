package com.astrotech.chat.ratelimit.bucketRatelimit;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class BucketRatelimiter {
    private final LettuceBasedProxyManager proxyManager;

    public Bucket resolveBucket(String key, int capacity, int refillSeconds) {
        var configuration = BucketConfiguration.builder()
                .addLimit(
                        Bandwidth.simple(
                                capacity,
                                Duration.ofSeconds(
                                        refillSeconds
                                )
                        )
                )
                .build();
        return proxyManager.builder()
                .build(
                        key.getBytes(StandardCharsets.UTF_8),
                        () -> configuration
                );
    }


}
