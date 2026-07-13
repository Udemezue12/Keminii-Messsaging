package com.astrotech.chat.websocket;


import com.astrotech.chat.dto.request.WsDeleteMessage;
import com.astrotech.chat.dto.request.WsEmojiToggleRequest;
import com.astrotech.chat.dto.request.WsForwardMessage;
import com.astrotech.chat.dto.request.WsReplyInboundMessage;
import com.astrotech.chat.dto.response.WsEditMessage;
import com.astrotech.chat.events.WsInboundMessage;
import com.astrotech.chat.ratelimit.redisRatelimit.Ratelimit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;


@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final WebSocketService webSocketService;


    @MessageMapping("/chat.sendMessage/{conversationId}")
    @Ratelimit
    public void sendMessage(@DestinationVariable String conversationId,@Valid @Payload WsInboundMessage payload,
                            SimpMessageHeaderAccessor headers) {
        log.info("Send Message Controller reached");
        webSocketService.sendMessage(conversationId,payload, headers);
    } 
    @MessageMapping("/chat.replyMessage/{messageId}/forward")
    @Ratelimit
    public void forwardMessage(@DestinationVariable String messageId,@Valid @Payload WsForwardMessage payload,
                            SimpMessageHeaderAccessor headers) {
        log.info("Forward Controller reached");
        webSocketService.forwardMessages(messageId,payload, headers);
    }
    @MessageMapping("/chat.replyMessage/{conversationId}/{messageId}")
    @Ratelimit
    public void replyMessage(@DestinationVariable String conversationId,@DestinationVariable String messageId,@Valid @Payload WsReplyInboundMessage payload,
                             SimpMessageHeaderAccessor headers) {
        log.info("Reply Controller reached");
        webSocketService.replyMessage(conversationId,messageId,payload, headers);
    }
    @MessageMapping("/chat.editMessage/{messageId}")
    @Ratelimit
    public void editMessage(@DestinationVariable String messageId,@Valid @Payload WsEditMessage payload,
                            SimpMessageHeaderAccessor headers) {
        log.info("Edit Message Controller reached");
        webSocketService.editMessage(messageId,payload, headers);
    }
    @MessageMapping("/chat.deleteMessage/{messageId}")
    @Ratelimit
    public void deleteMessage(@DestinationVariable String messageId, @Valid @Payload WsDeleteMessage payload,
                              SimpMessageHeaderAccessor headers) {
        log.info("Delete Message Controller reached");
        webSocketService.deleteMessage(messageId,payload, headers);
    }
    @MessageMapping("/chat.toggleReaction/{messageId}")
    @Ratelimit
    public void toggleReaction(@DestinationVariable String messageId, @Valid @Payload WsEmojiToggleRequest payload,
                               SimpMessageHeaderAccessor headers) {
        log.info("Toggle Reaction Controller reached");
        webSocketService.toggleReaction(messageId,payload, headers);
    }
    @MessageMapping("/chat.markAsDelivered/{messageId}")
    @Ratelimit
    public void markMessageAsDelivered(@DestinationVariable String messageId, SimpMessageHeaderAccessor headers) {
        log.info("Delivered Message Controller reached");
        webSocketService.markMessageAsDelivered(messageId, headers);
    }



    @MessageMapping("/chat.typing/{conversationId}")
    @Ratelimit
    public void typing(@DestinationVariable String conversationId,
                       SimpMessageHeaderAccessor headers) {
        webSocketService.typing(conversationId, headers);
    }


    @MessageMapping("/chat.read/{conversationId}")
    @Ratelimit
    public void markRead(@DestinationVariable String conversationId,   SimpMessageHeaderAccessor headers) {
        webSocketService.markRead(conversationId, headers);
    }


    @MessageMapping("/chat.ping")
    @SendToUser("/queue/pong")
    @Ratelimit
    public Map<String, Object> ping(SimpMessageHeaderAccessor headers) {
        return webSocketService.ping(headers);
    }


}
