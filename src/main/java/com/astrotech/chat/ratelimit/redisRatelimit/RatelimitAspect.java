package com.astrotech.chat.ratelimit.redisRatelimit;


import com.astrotech.chat.exceptions.RateLimitExceededException;
import com.astrotech.chat.ratelimit.bucketRatelimit.BucketRatelimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RatelimitAspect {

    private final RatelimitManager rateLimitManager;
    private final HttpServletRequest request;
    private final BucketRatelimiter bucketRateLimiter;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(
            ProceedingJoinPoint joinPoint,
            Ratelimit rateLimit) throws Throwable {

        String identifier =
                rateLimitManager.getIdentifier(request);

        String key =
                joinPoint.getSignature().getName()
                        + ":" + identifier;
        var bucket =
                bucketRateLimiter.resolveBucket(
                        key,
                        rateLimit.times(),
                        rateLimit.seconds());

        if (!bucket.tryConsume(1)) {

            throw new RateLimitExceededException(
                    "Bucket limit exceeded");
        }

        var limited =
                rateLimitManager.isRateLimited(
                        key,
                        rateLimit.times(),
                        rateLimit.seconds());

        if (limited) {

            throw new RateLimitExceededException(
                    "Rate limit exceeded. Please try again later.");
        }

        return joinPoint.proceed();
    }
}
