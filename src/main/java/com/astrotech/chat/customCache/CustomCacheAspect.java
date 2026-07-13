package com.astrotech.chat.customCache;



import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;



@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomCacheAspect {

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;

    @Around("@annotation(cacheable)")
    public Object cache(ProceedingJoinPoint joinPoint, CustomCacheable cacheable) throws Throwable {
        var cacheName = cacheable.value();
        var signature = (MethodSignature) joinPoint.getSignature();
        var method = signature.getMethod();


        var context = new MethodBasedEvaluationContext(
                null, method, joinPoint.getArgs(), new DefaultParameterNameDiscoverer()
        );
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));
        var parameters = signature.getParameterNames();
        var args = joinPoint.getArgs();
        for (int i = 0; i < parameters.length; i++) {
            context.setVariable(parameters[i], args[i]);
        }
        var key = new SpelExpressionParser()
                .parseExpression(cacheable.key())
                .getValue(context, String.class);

        var cache = cacheManager.getCache(cacheName);

        if (cache != null && key != null) {
            var wrapper = cache.get(key);
            if (wrapper != null && wrapper.get() != null) {
                try {

                    var jsonWrapper = (String) wrapper.get();
                    var ttlWrapper = objectMapper.readValue(jsonWrapper, TtlCacheWrapper.class);


                    if (ttlWrapper.isExpired()) {
                        log.debug("Cache expired for key: {} in bucket: {}", key, cacheName);
                        cache.evict(key);
                    } else {

                        var javaType = objectMapper.getTypeFactory().constructType(method.getGenericReturnType());
                        return objectMapper.readValue(ttlWrapper.getJsonPayload(), javaType);
                    }
                } catch (Exception ex) {
                    System.err.println("Cache read failed or structural mismatch: " + ex.getMessage());
                }
            }
        }


        var result = joinPoint.proceed();

        if (cache != null && key != null) {
            try {
                var rawJsonPayload = objectMapper.writeValueAsString(result);


                long expiresAt = -1;
                if (cacheable.ttl() > 0) {
                    long ttlMillis = cacheable.timeUnit().toMillis(cacheable.ttl());
                    expiresAt = System.currentTimeMillis() + ttlMillis;
                }


                var ttlWrapper = new TtlCacheWrapper(rawJsonPayload, expiresAt);
                var finalCacheString = objectMapper.writeValueAsString(ttlWrapper);

                cache.put(key, finalCacheString);
            } catch (Exception ex) {
                System.err.println("Cache write serialization failed: " + ex.getMessage());
            }
        }

        return result;
    }
}