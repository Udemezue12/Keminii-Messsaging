package com.astrotech.chat.validators.email.verified;

import com.astrotech.chat.dto.request.AuthenticatedUser;
import com.astrotech.chat.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class EmailVerificationAspect {
    @Around("@annotation(emailVerified)")
    public Object verifyEmailStatus(
            ProceedingJoinPoint joinPoint,
            EmailVerified emailVerified
    ) throws Throwable {

            var authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            var principal =
                    (AuthenticatedUser) authentication.getPrincipal();
            var role = UserRole.valueOf(principal.role());

            if (!principal.emailVerified() && role != UserRole.ADMIN) {
            throw new AccessDeniedException(
                    emailVerified.message()
            );
        }

        return joinPoint.proceed();}

}