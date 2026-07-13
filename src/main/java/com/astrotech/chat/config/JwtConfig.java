package com.astrotech.chat.config;

import com.astrotech.chat.core.GetSecretKey;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
@ConfigurationProperties(prefix = "spring.jwt")
@Data
public class JwtConfig {
    private String secret;
    private Long accessExpiration;
    private Long refreshExpiration;
    private String issuer;

    public SecretKey getSecretKey() {
        return GetSecretKey.getKeys(secret);

    }
}
