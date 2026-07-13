package com.astrotech.chat.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "BearerAuth";
    private static final String CSRF_AUTH = "CsrfAuth";
    @Value("${application.author}")
    private String name;
    @Value("${application.author-email}")
    private String email;
    @Value("${application.author-url}")
    private String url;


    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()

                .info(apiInfo())

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(BEARER_AUTH)
                                .addList(CSRF_AUTH)
                )

                .components(
                        new Components()

                                .addSecuritySchemes(
                                        BEARER_AUTH,
                                        bearerSecurityScheme()
                                )

                                .addSecuritySchemes(
                                        CSRF_AUTH,
                                        csrfSecurityScheme()
                                )

                );
    }

    private Info apiInfo() {

        return new Info()
                .title("Kemini Messaging API")
                .version("v1.0.0")
                .description("""
                        REST API for the Kemini Messaging Platform.
                        
                        Features:
                        - Authentication & Authorization
                        - Conversations
                        - Messages
                        - User Contacts
                        - User Blocking
                        - Audits
                        - GroupInfo
                        - Media Message
                      
                        
                        All protected endpoints require:
                        1. JWT Access Token
                        2. CSRF Token (for state-changing requests)
                        """)
                .contact(
                        new Contact()
                                .name(name)
                                .email(email)
                                .url(url)
                )
                .license(
                        new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")
                );
    }

    private SecurityScheme bearerSecurityScheme() {

        return new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        JWT Authorization Header
                        
                        Example:
                        
                        Bearer eyJhbGciOiJIUzI1NiIs...
                        """);
    }

    private SecurityScheme csrfSecurityScheme() {

        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-XSRF-TOKEN")
                .description("""
                        CSRF Token Header
                        
                        Required for:
                        - POST
                        - PUT
                        - PATCH
                        - DELETE
                        
                        Example:
                        
                        X-XSRF-TOKEN: abc123xyz
                        """);
    }



}