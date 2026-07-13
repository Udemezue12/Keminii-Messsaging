package com.astrotech.chat.customCache;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomCacheable {
    String value();
    String key();
    long ttl() default  -1;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
