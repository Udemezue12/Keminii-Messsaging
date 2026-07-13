package com.astrotech.chat.websocket;




import com.astrotech.chat.enums.JwtType;
import com.astrotech.chat.jwt.ExtractJwtToken;
import com.astrotech.chat.jwt.JwtProvider;
import com.astrotech.chat.service.BlacklistedTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketJwtHandshakeInterceptor implements HandshakeInterceptor {

    private final ExtractJwtToken extractJwtToken;
    private final JwtProvider jwtProvider;
    private final BlacklistedTokenService blacklistedTokenService;

    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {
                log.info("Handshake received");
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        var httpRequest = servletRequest.getServletRequest();

        var token = extractJwtToken.getAccessTokenForJwt(httpRequest);
        log.info("Token = {}", token);

        if (!StringUtils.hasText(token)) {
        // 🔴 CHANGE THIS: Don't reject here anymore! 
        // Let the handshake finish. The STOMP interceptor will catch it next.
        log.info("No HTTP token found. Deferring authentication to STOMP layer.");
        return true; 
    }

        try {

            var jwt = jwtProvider.extractClaims(token, JwtType.ACCESS);

            if (!jwt.isToken()) {
                log.warn("Handshake rejected: Bad token used");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            if (blacklistedTokenService.isBlacklisted(jwt.id())) {
                log.warn("Handshake rejected: Blacklisted token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            attributes.put("userId", jwt.userId());
            attributes.put("role", jwt.role());
            attributes.put("jti", jwt.id());
            attributes.put("emailVerified", jwt.emailVerified());
            attributes.put("displayName", jwt.displayName());

            log.debug("Handshake accepted for user {}", jwt.userId());

            return true;

        } catch (Exception ex) {

            log.warn("Handshake rejected: {}", ex.getMessage());

            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            Exception exception) {

        if (exception != null) {
            log.error("WebSocket handshake failed", exception);
        }
    }
}
