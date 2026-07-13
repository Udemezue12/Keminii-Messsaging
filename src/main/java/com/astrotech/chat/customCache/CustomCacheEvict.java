package com.astrotech.chat.customCache;



import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomCacheEvict {


    String[] cacheNames();


    String[] keys() default {};

    
    boolean allEntries() default false;


    boolean beforeInvocation() default false;


    String condition() default "";


    String unless() default "";
}
