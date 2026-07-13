package com.astrotech.chat.mappers;

import com.astrotech.chat.core.TrimWhiteSpace;
import com.astrotech.chat.dto.request.UserRequest;
import com.astrotech.chat.dto.response.UserResponse;
import com.astrotech.chat.entites.User;
import com.astrotech.chat.enums.UserStatus;

import java.time.Instant;


public class UserMapper {
    public static User create(UserRequest request){
        var email = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.email(), false);
        var nickName = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.nickName(), false);
        var displayName = TrimWhiteSpace.trimWhiteSpace(request.nickName());
        return User.builder()
                .nickName(nickName)
                .email(email)
                .fullName(request.fullName())
                .role(request.role())
                .displayName(displayName)
                .verified(false)
                .password(request.password())
                .phoneNumber(request.phoneNumber())
                .status(UserStatus.ACTIVE)
                .deleted(false)
                .suspended(false)
                .createdAt(Instant.now())

                .build();
    }
    public static UserResponse response(User user){
        return new UserResponse(
                user.getId(),
                user.getDisplayName(),
                user.getFullName(),
                user.getEmail(),
                user.getStatus(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }
    public static UserResponse conversationResponse(User user){
        if (user == null){
            return null;
        }
        return response(user);
    }
}
