package com.astrotech.chat.service;

import com.astrotech.chat.entites.UserSession;
import com.astrotech.chat.events.SessionTerminatedEvent;
import com.astrotech.chat.exceptions.ResourceNotFoundException;
import com.astrotech.chat.repositories.UserSessionRepository;
import com.astrotech.chat.exceptions.ForbiddenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static com.astrotech.chat.core.AppGenerators.generateSessionKey;


@RequiredArgsConstructor
@Service
@Slf4j
public class UserSessionService {
    private final UserSessionRepository userSessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserSession createUserSession(String userAgent, String deviceInfo, String userId, String ipAddress , String sessionId, String accessToken, String refreshToken) {
        var session =  UserSession.builder()
                .id(sessionId)
                .userId(userId)
                .sessionKey(generateSessionKey())
                .deviceInfo(deviceInfo != null ?deviceInfo : "Unknown")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .ipAddress(ipAddress).userAgent(userAgent).isActive(true)
                .lastActiveAt(Instant.now())
                .build();
        return userSessionRepository.save(session);


    }
    @Transactional
    public void terminateSession(String userId, String sessionId) {
        var session = getUserSession(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (!session.getUserId().equals(userId))
            throw new ForbiddenException("Cannot terminate another user's session");
        userSessionRepository.deactivate(sessionId);
        eventPublisher.publishEvent(new SessionTerminatedEvent(userId, session.getSessionKey()));
    }
    public void deactivateUser(String sessionId){
        getUserSession(sessionId).ifPresent(session -> userSessionRepository.deactivate(session.getId()));


    }
    public Optional<UserSession> getUserSession(String sessionId){
        return userSessionRepository.findById(sessionId);

    }
}
