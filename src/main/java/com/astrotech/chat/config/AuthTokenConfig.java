package com.astrotech.chat.config;

import com.astrotech.chat.configProperties.AuthTokenProperties;
import com.astrotech.chat.core.GetSecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@RequiredArgsConstructor
@Configuration
public class AuthTokenConfig {
    private final AuthTokenProperties properties;

    @Bean("verifySecretKey")
    public SecretKey verifySecretKey() {
        return GetSecretKey.getKeys(properties.getVerifyEmailSecretKey());
    }

    @Bean("resetSecretKey")
    public SecretKey resetSecretKey() {
        return GetSecretKey.getKeys(properties.getResetSecretKey());
    }

}
