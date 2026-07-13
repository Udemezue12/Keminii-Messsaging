package com.astrotech.chat.security;

import com.astrotech.chat.configProperties.SwaggerProperties;
import com.astrotech.chat.core.TrimWhiteSpace;
import com.astrotech.chat.csrf.CustomCsrfTokenRepository;
import com.astrotech.chat.csrf.CustomSpaCsrfTokenRequestHandler;
import com.astrotech.chat.enums.UserRole;
import com.astrotech.chat.ratelimit.bucketRatelimit.BucketRateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final SwaggerProperties swaggerProperties;
    private final CorsConfigurationSource corsConfigurationSource;
    private final UserDetailsService userDetailsService;
    private final JwtFilterChain jwtAuthFilter;
    private final CustomCsrfTokenRepository csrfTokenRepository;
    private final BucketRateLimitFilter rateLimitFilter;
    private final PasswordEncoder passwordEncoder;
    private final CustomSpaCsrfTokenRequestHandler csrfTokenRequestHandler;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {

        return (web) -> web.ignoring().requestMatchers(
                "/ws",
                "/ws/**",
                "/ws/native",
                "/ws/native/**");
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminSwaggerDocsFilterChain(HttpSecurity http) throws Exception {
        var email = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(swaggerProperties.username(), false);

        var swaggerUser = User.builder()
                .username(email)
                .password(passwordEncoder.encode(swaggerProperties.password()))
                .roles(swaggerProperties.role())
                .build();

        var swaggerUserDetailsService =
                new InMemoryUserDetailsManager(swaggerUser);

        var swaggerAuthenticationProvider =
                new DaoAuthenticationProvider(swaggerUserDetailsService);
        swaggerAuthenticationProvider.setPasswordEncoder(passwordEncoder);

        var swaggerAuthenticationManager =
                new ProviderManager(swaggerAuthenticationProvider);

        return http
                .securityMatcher("/v3/api-docs/admin", "/v3/api-docs/admin/**")
                .authenticationManager(swaggerAuthenticationManager)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasRole(swaggerProperties.role()))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(csrfTokenRequestHandler)
                        .ignoringRequestMatchers(
                                "/api/v1/auth/**",
                                "/ws",
                                "/ws/**",
                                "/ws/native",
                                "/ws/native/**",
                                "/actuator/health",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/",
                                "/swagger-ui.html",
                                "/templates/**"))

                // If using Authorization header only
                // .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**",
                                "/ws/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/csrf",
                                "/templates/**",
                                "/actuator/health",
                                "/error")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/",
                                "/index.html",
                                "/**/*.js",
                                "/**/*.css",
                                "/**/*/img",
                                "/assets/**", "/favicon.ico",
                                "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(
                                "/api/v1/admin/**")
                        .hasRole(UserRole.ADMIN.name())
                        .requestMatchers("/api/v1/cloudinary/**")
                        .hasRole(swaggerProperties.role())
                        .anyRequest().authenticated())

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("""
                                        {"success":false,"message":"Access denied"}
                                    """);
                        }))

                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:; " +
                                        "connect-src 'self' ws: wss:;"))
                        .referrerPolicy(r -> r.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))

                .build();
    }


}
