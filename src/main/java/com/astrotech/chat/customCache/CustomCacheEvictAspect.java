package com.astrotech.chat.customCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomCacheEvictAspect {

    private final CacheManager cacheManager;

    private final ExpressionParser parser = new SpelExpressionParser();

    private final ParameterNameDiscoverer parameterNames =
            new DefaultParameterNameDiscoverer();

    private final Map<String, Expression> expressionCache =
            new ConcurrentHashMap<>();

    @Around("@annotation(evict)")
    public Object around(
            ProceedingJoinPoint joinPoint,
            CustomCacheEvict evict
    ) throws Throwable {

        validate(evict);

        var method =
                ((MethodSignature) joinPoint.getSignature()).getMethod();

        var args = joinPoint.getArgs();

        if (evict.beforeInvocation()) {
            evictCaches(joinPoint, method, args, null, evict);
        }

        var result = joinPoint.proceed();

        if (!evict.beforeInvocation()) {
            evictCaches(joinPoint, method, args, result, evict);
        }

        return result;
    }

    private void evictCaches(
            ProceedingJoinPoint joinPoint,
            Method method,
            Object[] args,
            Object result,
            CustomCacheEvict evict
    ) {

        StandardEvaluationContext context =
                buildContext(joinPoint, method, args, result);

        if (!evaluateCondition(evict.condition(), context)) {
            log.trace("Condition '{}' evaluated to false.", evict.condition());
            return;
        }

        if (evaluateUnless(evict.unless(), context)) {
            log.trace("Unless '{}' evaluated to true.", evict.unless());
            return;
        }

        for (String cacheName : evict.cacheNames()) {

            Cache cache = cacheManager.getCache(cacheName);

            if (cache == null) {
                log.warn("Cache '{}' not found.", cacheName);
                continue;
            }

            if (evict.allEntries()) {

                log.debug("Clearing cache '{}'", cacheName);

                cache.clear();

                continue;
            }

            for (String keyExpression : evict.keys()) {

                var key = resolveExpression(keyExpression, context);

                log.debug(
                        "Evicting cache '{}' key '{}'",
                        cacheName,
                        key
                );

                cache.evict(key);
            }
        }
    }

    private StandardEvaluationContext buildContext(
            ProceedingJoinPoint joinPoint,
            Method method,
            Object[] args,
            Object result
    ) {

        StandardEvaluationContext context =
                new StandardEvaluationContext();

        String[] names = parameterNames.getParameterNames(method);

        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                context.setVariable(names[i], args[i]);
            }
        }

        context.setVariable("args", args);
        context.setVariable("result", result);

        RootObject root = new RootObject(
                joinPoint.getTarget(),
                method,
                args
        );

        context.setVariable("root", root);

        return context;
    }

    private boolean evaluateCondition(
            String expression,
            StandardEvaluationContext context
    ) {

        if (expression.isBlank()) {
            return true;
        }

        Boolean value = getExpression(expression)
                .getValue(context, Boolean.class);

        return Boolean.TRUE.equals(value);
    }

    private boolean evaluateUnless(
            String expression,
            StandardEvaluationContext context
    ) {

        if (expression.isBlank()) {
            return false;
        }

        Boolean value = getExpression(expression)
                .getValue(context, Boolean.class);

        return Boolean.TRUE.equals(value);
    }

    private Object resolveExpression(
            String expression,
            StandardEvaluationContext context
    ) {

        if (!expression.contains("#")) {
            return expression;
        }

        return getExpression(expression).getValue(context);
    }

    private Expression getExpression(String expression) {

        return expressionCache.computeIfAbsent(
                expression,
                parser::parseExpression
        );
    }

    private void validate(CustomCacheEvict evict) {

        Assert.notEmpty(
                evict.cacheNames(),
                "At least one cache name must be supplied."
        );

        if (!evict.allEntries()) {

            Assert.isTrue(
                    evict.keys().length > 0,
                    "keys() must be supplied when allEntries=false."
            );
        }
    }

    public record RootObject(
            Object target,
            Method method,
            Object[] args
    ) {
    }
}