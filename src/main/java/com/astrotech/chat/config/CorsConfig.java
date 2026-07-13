package com.astrotech.chat.config;


import com.astrotech.chat.configProperties.AllowedOrigins;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig{
    private final AllowedOrigins allowedOrigins1;
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins1.allowedOrigins());
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        config.setAllowedHeaders(
                List.of(
                        HttpHeaders.AUTHORIZATION,
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCEPT,
                        HttpHeaders.ACCESS_CONTROL_MAX_AGE,
                        "X-Requested-With",
                        "X-XSRF-TOKEN",
                        "Session-Key",
                        "Origin"
                )
        );
        config.setExposedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.ACCESS_CONTROL_MAX_AGE,
                "X-XSRF-TOKEN",
                "X-RateLimit-Remaining",
                "Session-Key"
        ));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;

    }


}
