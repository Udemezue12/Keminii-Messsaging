package com.astrotech.chat.websocket;


import com.astrotech.chat.dto.request.WsDeleteMessage;
import com.astrotech.chat.dto.request.WsEmojiToggleRequest;
import com.astrotech.chat.dto.request.WsForwardMessage;
import com.astrotech.chat.dto.request.WsReplyInboundMessage;
import com.astrotech.chat.dto.response.ForwardMessageResponse;
import com.astrotech.chat.dto.response.MessageResponse;
import com.astrotech.chat.dto.response.WsEditMessage;
import com.astrotech.chat.enums.OnlineStatus;
import com.astrotech.chat.enums.SendMessageType;
import com.astrotech.chat.events.TypingEvent;
import com.astrotech.chat.events.WsInboundMessage;

import com.astrotech.chat.service.MessageService;
import com.astrotech.chat.service.UserService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;



import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import org.springframework.messaging.simp.SimpMessagingTemplate;


import org.springframework.stereotype.Service;


import java.time.Instant;

import java.util.Map;


@RequiredArgsConstructor

@Slf4j
@Service
public class WebSocketService {


    private final MessageService messageService;

    private final UserService userService;

    private final SimpMessagingTemplate messaging;


    public void sendMessage(String conversationId, WsInboundMessage payload,
                            SimpMessageHeaderAccessor headers) {
        log.info("WebSocketService reached");
        var user = WebSocketUtils.extractUserFromHeader(headers.getUser());

        if (user == null) {

            sendError(headers, "UNAUTHORIZED", "Not authenticated", payload.tempId());

            return;

        }

        if (conversationId == null || conversationId.isBlank()) {

            sendError(headers, "INVALID_PAYLOAD", "conversationId is required", payload.tempId());

            return;

        }

        try {

            var response = messageService.sendMessage(

                    conversationId,
                    user.userId(),

                    SendMessageType.TEXT,

                    payload.content());


            messaging.convertAndSendToUser(

                    user.userId(),

                    "/queue/notifications",

                    Map.of("tempId", payload.tempId() != null ? payload.tempId() : "",

                            "serverId", response.getId(),

                            "status", "SENT",

                            "conversationId", conversationId));


        } catch (Exception e) {

            log.error("WS sendMessage error: userId={} convId={} error={}",

                    user.userId(), conversationId, e);

            sendError(headers, "SEND_FAILED", e.getMessage(), payload.tempId());

        }

    }
    public void replyMessage(String conversationId,String messageId, WsReplyInboundMessage payload,
                            SimpMessageHeaderAccessor headers) {
        log.info("Reply WebSocketService reached");
        var user = WebSocketUtils.extractUserFromHeader(headers.getUser());

        if (user == null) {

            sendError(headers, "UNAUTHORIZED", "Not authenticated", payload.tempId());

            return;

        }

        if (conversationId == null || conversationId.isBlank()) {

            sendError(headers, "INVALID_PAYLOAD", "conversationId is required", payload.tempId());

            return;

        }

        try {

            var response = messageService.replyToMessage(

                    conversationId,
                    user.userId(),
                    messageId,
                    SendMessageType.TEXT,
                    payload.content());


            messaging.convertAndSendToUser(

                    user.userId(),

                    "/queue/notifications",

                    Map.of("tempId", payload.tempId() != null ? payload.tempId() : "",

                            "serverId", response.getId(),

                            "status", "SENT",

                            "conversationId", conversationId));


        } catch (Exception e) {

            log.error("WS sendMessage error: userId={} convId={} error={}",

                    user.userId(), conversationId, e);

            sendError(headers, "SEND_FAILED", e.getMessage(), payload.tempId());

        }

    }
    public void forwardMessages(String messageId,WsForwardMessage payload,SimpMessageHeaderAccessor headers) {

        log.info("Forward WebSocketService reached");

        var user = WebSocketUtils.extractUserFromHeader(headers.getUser());

        if (user == null) {
            sendError(headers, "UNAUTHORIZED", "Not authenticated", null);
            return;
        }

        if (messageId == null || messageId.isBlank()) {
            sendError(headers, "INVALID_PAYLOAD", "messageId is required", null);
            return;
        }

        try {

            var responses = messageService.forwardMessages(
                    payload.conversationIds(),
                    user.userId(),
                    messageId);

            for (MessageResponse response : responses) {

                messaging.convertAndSendToUser(
                        user.userId(),
                        "/queue/notifications",
                        Map.of(
                                "serverId", response.getId(),
                                "conversationId", response.getConversationId(),
                                "status", "SENT"
                        ));
            }

        } catch (Exception e) {

            log.error(
                    "WS forwardMessage error: userId={} messageId={}",
                    user.userId(),
                    messageId,
                    e);

            sendError(headers, "FORWARD_FAILED", e.getMessage(), null);
        }
    }
    public void editMessage(String messageId, WsEditMessage payload, SimpMessageHeaderAccessor headers){
        var extractUser = WebSocketUtils.extractUserFromHeader(headers.getUser());
        if (extractUser == null) return;
        var user = userService.getUserOrNull(extractUser.userId());


        if (user == null || messageId == null) return;
        messageService.editMessage(messageId, payload.content(), extractUser.userId());

    }
    public void deleteMessage(String messageId, WsDeleteMessage payload, SimpMessageHeaderAccessor headers){
        var extractUser = WebSocketUtils.extractUserFromHeader(headers.getUser());
        if (extractUser == null) return;
        var user = userService.getUserOrNull(extractUser.userId());


        if (user == null || messageId == null) return;
        messageService.deleteMessage(messageId, payload.deleteForAll(), extractUser.userId());

    }
    public void toggleReaction(String messageId, WsEmojiToggleRequest payload, SimpMessageHeaderAccessor headers){
        log.info("Toggle Reaction WebSocketService reached");
        var extractUser = WebSocketUtils.extractUserFromHeader(headers.getUser());
        if (extractUser == null) return;
        var user = userService.getUserOrNull(extractUser.userId());


        if (user == null || messageId == null) return;
        messageService.toggleReaction(messageId, payload.emoji(), extractUser.userId());
    }
    public void markMessageAsDelivered(String messageId, SimpMessageHeaderAccessor headers){
        var extractUser = WebSocketUtils.extractUserFromHeader(headers.getUser());
        if (extractUser == null) return;
        var user = userService.getUserOrNull(extractUser.userId());


        if (user == null || messageId == null) return;
        messageService.markMessageDelivered(messageId, extractUser.userId());
    }




   

    public void typing(String conversationId,

                       SimpMessageHeaderAccessor headers) {

        var extractUser = WebSocketUtils.extractUserFromHeader(headers.getUser());
        if (extractUser == null) return;
        var user = userService.getUserOrNull(extractUser.userId());


        if (user == null || conversationId == null) return;


        var event = new TypingEvent(

                user.getId(),
                user.getDisplayName(),

                conversationId,

                true,

                Instant.now());


        messaging.convertAndSend(

                "/topic/conversation." + conversationId, event);

    }


    public void markRead(String conversationId,

                         SimpMessageHeaderAccessor headers) {

        var user = WebSocketUtils.extractUserFromHeader(headers.getUser());

        if (user == null || conversationId == null) return;

        try {

            messageService.markConversationRead(conversationId, user.userId());

        } catch (Exception e) {

            log.debug("markRead error: {}", e.getMessage());

        }

    }



    public Map<String, Object> ping(SimpMessageHeaderAccessor headers) {

        log.info("PING RECEIVED");

    var user = WebSocketUtils.extractUserFromHeader(headers.getUser());

    log.info("USER = {}", user);

        if (user != null) {

            try {

                userService.updateOnlineStatus(user.userId(), OnlineStatus.ONLINE);

            }
            catch(Exception ex){



            }


        }

        return Map.of("pong", true, "ts", Instant.now().toString());

    }

    private void sendError(
            SimpMessageHeaderAccessor headers,
            String code,
            String message,
            String ref) {

        var user = WebSocketUtils.extractUserFromHeader(headers.getUser());

        if (user == null) {
            return;
        }

        messaging.convertAndSendToUser(
                user.userId(),
                "/queue/errors",
                Map.of(
                        "code", code,
                        "message", message != null ? message : "Unknown error",
                        "ref", ref != null ? ref : ""
                )
        );
    }


}
