package com.astrotech.chat.dto.request;

import com.astrotech.chat.enums.UserRole;

import com.astrotech.chat.validators.email.domain.ValidateEmailDomains;
import jakarta.validation.constraints.*;


public record UserRequest(
        @NotBlank(message = "Name is required") @Size(min = 10, max = 255, message = "Fullname must be more than ten characters") String fullName,
        @NotNull(message = "Role is required") UserRole role,
        @NotBlank(message = "Nick Name is required") @Size(min = 5, max = 255, message = "Nickname must be more than five characters")
        String nickName,


        @NotBlank(message = "Email is required") @Email(message = "Invalid email format")
        @ValidateEmailDomains
        String email,
        @NotBlank(message = "Phone number is required") @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format") String phoneNumber,

        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "Password must be 8–15 characters and include uppercase, lowercase, number, and special character")
        @Size(min = 8, max = 15, message = "Password must be between 8 and 15 characters") String password) {

}
