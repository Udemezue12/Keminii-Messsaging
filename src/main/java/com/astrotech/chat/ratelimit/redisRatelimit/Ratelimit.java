package com.astrotech.chat.ratelimit.redisRatelimit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ratelimit {

    int times() default 3;

    int seconds() default 10;
}
