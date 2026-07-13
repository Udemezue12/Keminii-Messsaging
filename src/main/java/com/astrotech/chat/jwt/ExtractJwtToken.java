package com.astrotech.chat.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ExtractJwtToken {
    private String extractTokenForWebsocket(StompHeaderAccessor accessor) {

        var authHeader = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return accessor.getFirstNativeHeader("token");
    }
    public String getAccessTokenForJwt(HttpServletRequest request) {

        var authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }


        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())
                        && cookie.getValue() != null
                        && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }


        if (isWebSocketHandshakeRequest(request)) {
            String token = request.getParameter("access_token");
            if (token != null && !token.isBlank()) {
                return token;
            }
        }

        return null;
    }

    private boolean isWebSocketHandshakeRequest(HttpServletRequest request) {
        var uri = request.getRequestURI();
        var upgrade = request.getHeader("Upgrade");

        return uri != null
                && uri.startsWith("/ws")
                && "websocket".equalsIgnoreCase(upgrade);
    }
}
