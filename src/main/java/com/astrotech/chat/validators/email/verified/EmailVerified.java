package com.astrotech.chat.validators.email.verified;

import java.lang.annotation.*;

@Target({
        ElementType.TYPE,
        ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailVerified {
    String message() default
            "Email verification required.";
    boolean allowAdmins() default true;
}
