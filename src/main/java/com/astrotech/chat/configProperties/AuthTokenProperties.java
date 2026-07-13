package com.astrotech.chat.configProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthTokenProperties {

    private String redisUrl;

    private String resetPasswordSalt;

    private String resetSecretKey;

    private String verifyEmailSalt;

    private String verifyEmailSecretKey;


}
