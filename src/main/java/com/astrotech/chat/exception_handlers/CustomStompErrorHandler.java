package com.astrotech.chat.exception_handlers;

import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Component
public class CustomStompErrorHandler extends StompSubProtocolErrorHandler {
    public CustomStompErrorHandler() {
        super();
    }
    @Override
    public @Nullable Message<byte[]> handleClientMessageProcessingError(@Nullable Message<byte[]> clientMessage, Throwable ex) {
        var rootCause = ex.getCause() != null ? ex.getCause() : ex;
        var errorMessage = rootCause.getMessage();
        var accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(errorMessage);
        accessor.setLeaveMutable(true);
        accessor.setNativeHeader("status", String.valueOf(401));

        var payload = errorMessage != null ? errorMessage.getBytes(StandardCharsets.UTF_8) : new byte[0];
        return MessageBuilder.createMessage(payload,accessor.getMessageHeaders());
    }
}
