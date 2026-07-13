package com.astrotech.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddContactRequest(
        @NotBlank(message = "Phone number is required") @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
         String phoneNumber,
        @NotBlank(message = "Contact Name is required") @Size(min = 2, max = 255)
        String contactName

) {
}
