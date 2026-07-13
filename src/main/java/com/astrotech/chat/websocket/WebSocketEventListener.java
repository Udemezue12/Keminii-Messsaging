package com.astrotech.chat.websocket;


import com.astrotech.chat.dto.request.AuthenticatedUser;
import com.astrotech.chat.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        var userId = getStompAccessorHeader(headerAccessor);


        if (userId != null) {
            log.info("WebSocket disconnect captured. Cleaning up counts for userId: {}", userId);
            presenceService.disconnect(userId);
        } else {
            log.warn("WebSocket session closed but no authenticated userId could be resolved for session: {}",
                    headerAccessor.getSessionId());
        }
    }

    private static @Nullable String getStompAccessorHeader(StompHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        String userId = null;


        if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
            userId = (String) sessionAttributes.get("userId");
        }


        if (userId == null && headerAccessor.getUser() instanceof UsernamePasswordAuthenticationToken authToken) {
            if (authToken.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
                userId = authenticatedUser.userId();
            }
        }
        return userId;
    }
}