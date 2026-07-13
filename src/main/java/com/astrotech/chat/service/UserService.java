package com.astrotech.chat.service;


import com.astrotech.chat.core.GetCurrentUser;
import com.astrotech.chat.dto.request.ChangePasswordRequest;
import com.astrotech.chat.dto.response.UserResponse;
import com.astrotech.chat.entites.User;
import com.astrotech.chat.enums.OnlineStatus;
import com.astrotech.chat.enums.Status;
import com.astrotech.chat.enums.UserStatus;
import com.astrotech.chat.exceptions.BadRequestException;
import com.astrotech.chat.exceptions.ResourceNotFoundException;
import com.astrotech.chat.mappers.UserMapper;
import com.astrotech.chat.repositories.UserRepository;
import com.astrotech.chat.responseBuilder.ApiResponse;
import com.astrotech.chat.responseBuilder.ApiResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepo;

    private final PresenceService presenceService;
    private final PasswordEncoder passwordEncoder;
    private final GetCurrentUser getCurrentUser;



   @Transactional
   public void updateUserStatus(String userId){
       var user = getAuthorizedUser(userId);

       if (user != null){
           presenceService.disconnect(user.getId());
           user.setOnlineStatus(OnlineStatus.OFFLINE);
           user.setStatus(UserStatus.OFFLINE);
           userRepo.save(user);
       }
   }

    public List<User> findConnectedUsers(Status status) {
        return userRepo.findAllByStatus(status);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: "));

        if (user.isDeleted()) {
            throw new DisabledException(
                    "Account deleted");
        }

        if (user.isSuspended()) {
            throw new LockedException(
                    "Account suspended");
        }
        return new CustomUserDetails(user);

    }
    public ResponseEntity<ApiResponse<Void>> changePassword(ChangePasswordRequest request) {
        var user = getCurrentUser.getCurrentUser();
        if (!user.getPassword().equals(request.oldPassword())) {
            return ApiResponseBuilder.unAuthorized();

        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepo.save(user);
        return ApiResponseBuilder.success("Password changed successfully", null);



    }
    @Transactional()
    public List<UserResponse> searchUsers(String query, String currentUserId) {
        if (query == null || query.trim().length() < 2)
            throw new BadRequestException("Search query must be at least 2 characters");
        return userRepo.searchUsers(query.trim(), currentUserId)
                .stream().map(UserMapper::response).toList();
    }
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void deleteAccount(String userId) {
        User user = getAuthorizedUser(userId);
        user.setDeletedAt(java.time.Instant.now());
        userRepo.save(user);
        log.info("User account soft-deleted: {}", userId);
    }
    public User getUser(){
        return getCurrentUser.getCurrentUser();

    }

    public User getAuthorizedUser(String userId) {
        return findUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Does not exist"));
    }
    public  User getUserOrNull(String userId){
        return findUserId(userId).orElse(null);

    }
    public Optional<User> findUserId(String userId){
        return userRepo.findById(userId);
    }
    @Transactional
    public void updateOnlineStatus(String userId, OnlineStatus status) {
        userRepo.updateOnlineStatus(userId, status, Instant.now());
    }
    public List<User> findUserIn(List<String> userIds){
        return userRepo.findByIdIn(userIds);
    }
    public Optional<User> findUserByPhoneNumber(String phoneNumber){
        return userRepo.findByPhoneNumber(phoneNumber);
    }
}
