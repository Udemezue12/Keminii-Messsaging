package com.astrotech.chat.validators.email.domain;

import com.astrotech.chat.configProperties.AllowedOrigins;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EmailDomainValidator implements ConstraintValidator<ValidateEmailDomains, String> {
    private final AllowedOrigins allowedOrigins;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
       if (email == null || !email.contains("@")){
           return true;
       }
       var domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase().trim();
        if (allowedOrigins == null || allowedOrigins.allowedEmailDomains() == null) {
            return false;
        }
        return allowedOrigins.allowedEmailDomains().contains(domain);
    }
}
