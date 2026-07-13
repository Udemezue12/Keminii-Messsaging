package com.astrotech.chat.exception_handlers;


import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RedisCacheErrorHandler {
    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn(
                        "Redis GET failed for cache {} key {}",
                        cache.getName(),
                        key,
                        exception);

            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
                log.warn(
                        "Redis PUT failed for cache {} key {}",
                        cache.getName(),
                        key,
                        exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn(
                        "Redis DELETE failed for cache {} key {}",
                        cache.getName(),
                        key,
                        exception);

            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn(
                        "Redis CLEAR failed for cache {}",
                        cache.getName(),
                        exception);

            }
        };
    }
}
