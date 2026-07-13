package com.astrotech.chat.websocket;

import com.astrotech.chat.enums.JwtType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import com.astrotech.chat.dto.request.AuthenticatedUser;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.astrotech.chat.jwt.JwtProvider;
import com.astrotech.chat.service.BlacklistedTokenService;
import java.util.List;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthenticationChannelInterceptor implements ChannelInterceptor {
    private final JwtProvider jwtProvider;

    private final BlacklistedTokenService blacklistedTokenService;

    @Override
    public Message<?> preSend(
            @NonNull Message<?> message,
            @NonNull MessageChannel channel) {

        var accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class);

        StompCommand command = accessor.getCommand();

        if (command != null) {
            log.info("STOMP command = {}", command);
            log.info("Headers = {}", accessor.toNativeHeaderMap());
        }
        if (!StompCommand.CONNECT.equals(accessor.getCommand()))
            return message;
        log.debug("{} {}", accessor.getCommand(),
                accessor.getDestination());

        var attributes = accessor.getSessionAttributes();
        var userId = (attributes != null) ? (String) attributes.get("userId") : null;
        var role = (attributes != null) ? (String) attributes.get("role") : null;
        var displayName = (attributes != null) ? (String) attributes.get("display") : null;
        var emailVerified = (attributes != null) ? (Boolean) attributes.get("emailVerified") : null;


        if (userId == null || role == null) {
            var authHeader = accessor.getFirstNativeHeader("Authorization");

            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    var jwt = jwtProvider.extractClaims(token, JwtType.ACCESS);
                    if (jwt.isToken() && !blacklistedTokenService.isBlacklisted(jwt.id())) {
                        userId = jwt.userId();
                        role = String.valueOf(jwt.role());
                        emailVerified = jwt.emailVerified();
                        displayName = jwt.displayName();
                        if (attributes != null) {
                            attributes.put("userId", userId);
                            attributes.put("role", role);
                            attributes.put("jti", jwt.id());
                            attributes.put("emailVerified", emailVerified);
                            attributes.put("displayName", jwt.displayName());
                        }

                    }
                } catch (Exception e) {
                    log.warn("STOMP authentication failed: {}", e.getMessage());
                    throw new AccessDeniedException("Invalid token");
                }
            }
        }
        if (userId == null || role == null) {
            throw new AccessDeniedException("Unauthenticated connection attempt");
        }

        var auth = getUsernamePasswordAuthenticationToken(userId, role, displayName, emailVerified);

        accessor.setUser(auth);

        log.debug("Authenticated websocket user {}", userId);

        return message;
    }

    private static @NonNull UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(String userId,
            String role, String displayName, boolean emailVerified) {
        if (userId == null || role == null)
            throw new AccessDeniedException("Unauthenticated");

        var principal = new AuthenticatedUser(userId, role, displayName, emailVerified);

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role)));
    }
}
