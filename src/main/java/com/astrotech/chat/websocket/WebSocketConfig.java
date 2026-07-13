package com.astrotech.chat.websocket;

import com.astrotech.chat.configProperties.AllowedOrigins;

import com.astrotech.chat.configProperties.StompRabbitBrokerProperties;
import com.astrotech.chat.exception_handlers.CustomStompErrorHandler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.tcp.TcpOperations;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Configuration;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;


import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final RabbitProperties rabbitProperties;
    @Value("${springdoc.swagger-ui.enabled}")
    private boolean swaggerUiEnabled;
    private final MappingJackson2MessageConverter jackson2MessageConverter;
    private static final int MESSAGE_SIZE_LIMIT = 2048 * 1024;
    private static final int BUFFER_SIZE_LIMIT = 4 * 1024 * 1024;
    private final WebSocketAuthenticationChannelInterceptor websocketAuthInterceptor;
    private final TcpOperations<byte[]> nettyTcpClient;
    private final StompRabbitBrokerProperties rabbitBrokerProperties;

    private final AllowedOrigins allowedOrigins1;
    private final CustomStompErrorHandler customStompErrorHandler;
    private final WebSocketJwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final ThreadPoolTaskScheduler heartbeatTaskScheduler;


    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {

        messageConverters.add(0, jackson2MessageConverter);

       
        return false;
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        if (swaggerUiEnabled) {
            registry.enableSimpleBroker("/topic", "/queue",  "/exchange")
                    .setHeartbeatValue(new long[]{10000,10000})
                    .setTaskScheduler(heartbeatTaskScheduler);
            registry.setUserDestinationPrefix("/user");
        } else {
            registry.enableStompBrokerRelay(
                            "/topic",
                            "/queue",
                            "/exchange"
                    )
                    .setTcpClient(nettyTcpClient)
                    .setRelayHost(rabbitProperties.getHost())
                    .setRelayPort(rabbitBrokerProperties.getRelayPort())

                    .setClientLogin(rabbitProperties.getUsername())
                    .setClientPasscode(rabbitProperties.getPassword())

                    .setSystemLogin(rabbitProperties.getUsername())
                    .setSystemPasscode(rabbitProperties.getPassword())

                    .setVirtualHost(rabbitProperties.getVirtualHost())
                    .setAutoStartup(true)

                    .setSystemHeartbeatSendInterval(10000)
                    .setUserDestinationBroadcast("/topic/unresolved-user-destination")
                    .setUserRegistryBroadcast("/topic/simp-user-registry")
                    .setSystemHeartbeatReceiveInterval(10000);
            registry.setUserDestinationPrefix("/user");
        }




    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        var patterns = allowedOrigins1.allowedOrigins().toArray(new String[0]);


        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns(patterns)
                .withSockJS()
                .setHeartbeatTime(25_000)
                .setDisconnectDelay(5_000);


        registry.addEndpoint("/ws/native")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns(patterns);
                
        registry.setErrorHandler(customStompErrorHandler);
    }
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration){
        registration.setMessageSizeLimit(MESSAGE_SIZE_LIMIT)
                .setSendBufferSizeLimit(BUFFER_SIZE_LIMIT)
                .setSendTimeLimit((int) Duration.ofSeconds(30).toMillis());
    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(websocketAuthInterceptor);

        registration.taskExecutor()
                .corePoolSize(8)
                .maxPoolSize(32).
                queueCapacity(400).
                keepAliveSeconds(40);
    }


    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.
                taskExecutor().
                corePoolSize(8).
                maxPoolSize(32).
                queueCapacity(400).
                keepAliveSeconds(40);
    }
}
