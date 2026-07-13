package com.astrotech.chat.core;


import  com.astrotech.chat.dto.request.AuthenticatedUser;
import com.astrotech.chat.entites.User;
import com.astrotech.chat.exceptions.ResourceNotFoundException;
import com.astrotech.chat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCurrentUser {
    private final UserRepository userRepo;

    public User getCurrentUser() {
        var userId = getCurrentUserId();
        return userRepo.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Not Found")
        );
    }
    public String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();



        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated");
        }

        var principal = authentication.getPrincipal();


        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser.userId();
        }


        if (principal instanceof String) {
            return (String) principal;
        }

        throw new IllegalStateException("Unknown principal type: " + principal.getClass().getName());
    }

    public String getCurrentUserIdOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        var principal = authentication.getPrincipal();


        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser.userId();
        }


        if (principal instanceof String stringId) {
            return stringId;
        }

        return null;
    }
}
