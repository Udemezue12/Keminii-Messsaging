package com.astrotech.chat.websocket;

import com.astrotech.chat.configProperties.EncryptionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

@Configuration
@EnableWebSocketSecurity
@RequiredArgsConstructor
public class WebSocketSecurityConfig {
    private final EncryptionProperties encryptionProperties;


    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        if (!encryptionProperties.isProductionMode()){
             messages.anyMessage().permitAll();

        } else {

        messages

                .simpTypeMatchers(
                        org.springframework.messaging.simp.SimpMessageType.CONNECT,
                        org.springframework.messaging.simp.SimpMessageType.DISCONNECT,
                        org.springframework.messaging.simp.SimpMessageType.HEARTBEAT)
                .permitAll()


                .simpMessageDestMatchers("/app/chat.sendMessage/**").authenticated()
                .simpMessageDestMatchers("/app/chat.typing").authenticated()
                .simpMessageDestMatchers("/app/chat.ping").authenticated()


                .simpMessageDestMatchers("/app/**").authenticated()


                .simpMessageDestMatchers("/topic/presence/**", "/topic/group/**",
                        "/topic/typing/**")
                .authenticated()


                .simpSubscribeDestMatchers("/user/**").authenticated()
                .simpSubscribeDestMatchers("/topic/presence/**").authenticated()
                .simpSubscribeDestMatchers("/topic/group/**").authenticated()
                .simpSubscribeDestMatchers("/topic/notifications/**").authenticated()
                .simpSubscribeDestMatchers("/topic/conversation.**").authenticated()

                .simpSubscribeDestMatchers("/topic/conversation/**").authenticated()
                .simpSubscribeDestMatchers("/topic/public/**").permitAll()
                .simpSubscribeDestMatchers("/topic/admin/**").hasRole("ADMIN")

                .matchers(message -> {
                    SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
                    return accessor.getDestination() == null ||
                            accessor.getDestination().isEmpty();
                }).permitAll()

                .anyMessage().denyAll();

        }
        return messages.build();
    }

    @Bean
    public ChannelInterceptor csrfChannelInterceptor() {
        return new ChannelInterceptor() {
        };
    }
}