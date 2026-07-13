package com.astrotech.chat.configProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.swagger")
public record SwaggerProperties(
        String username,
        String password,
        String role
) {
}
