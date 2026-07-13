package com.astrotech.chat.security;

import com.astrotech.chat.dto.request.AuthenticatedUser;

import com.astrotech.chat.enums.JwtType;
import com.astrotech.chat.jwt.ExtractJwtToken;
import com.astrotech.chat.jwt.JwtProvider;
import com.astrotech.chat.service.BlacklistedTokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilterChain extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final BlacklistedTokenService blacklistedTokenService;
    private final ExtractJwtToken extractJwtToken;


    @Override

    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            var token = extractJwtToken.getAccessTokenForJwt(request);


            if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {


                var jwt = jwtProvider.extractClaims(token, JwtType.ACCESS);

                if (jwt.isToken()) {
                    var jti = jwt.id();
                    if (!blacklistedTokenService.isBlacklisted(jti)) {
                        var role = String.valueOf(jwt.role());
                        var displayName = jwt.displayName();
                        var emailVerified = jwt.emailVerified();

                        var principal = new AuthenticatedUser(jwt.userId(), role, displayName, emailVerified );

                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);

                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        MDC.put("userId", principal.userId());
                    }
                }
            }
        } catch (Exception e) {

            SecurityContextHolder.clearContext();
            log.warn("JWT authentication failed: {}", e.getMessage());
        } finally {
            try {
                filterChain.doFilter(request, response);
            } finally {
                MDC.remove("userId");
            }
        }
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/actuator/health")
                || path.equals("/")
                || path.equals("/index.html")
                || path.endsWith(".js")
                || path.endsWith(".css")
                || path.startsWith("/assets/")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/v3/api-docs/admin")
                || path.startsWith("/swagger-ui")
                || path.equals("/favicon.ico");
    }


}
