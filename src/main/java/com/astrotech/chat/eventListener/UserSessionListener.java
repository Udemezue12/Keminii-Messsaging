package com.astrotech.chat.eventListener;


import com.astrotech.chat.dto.response.TokenData;
import com.astrotech.chat.enums.JwtType;
import com.astrotech.chat.events.SessionTerminatedEvent;
import com.astrotech.chat.repositories.UserSessionRepository;
import com.astrotech.chat.service.BlacklistedTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSessionListener {

    private final BlacklistedTokenService blacklistedTokenService;
    private final UserSessionRepository tokenRepository;

    @EventListener
    public void onSessionTerminated(SessionTerminatedEvent event) {
        log.info("Processing security cleanup for terminated session. User: {}, SessionKey: {}",
                event.userId(), event.sessionKey());

        try {

            tokenRepository.findBySessionKey(event.sessionKey()).ifPresent(tokenData -> {

                if (tokenData.getAccessToken() != null) {
                    blacklistedTokenService.blacklist(tokenData.getAccessToken(), JwtType.ACCESS);
                }

                if (tokenData.getRefreshToken() != null) {
                    blacklistedTokenService.blacklist(tokenData.getRefreshToken(), JwtType.REFRESH);
                }


                tokenRepository.delete(tokenData);
            });

        } catch (Exception e) {
            log.debug("Failed to cleanly blacklist tokens for session{}{}", e, event.sessionKey());

        }
    }
}
