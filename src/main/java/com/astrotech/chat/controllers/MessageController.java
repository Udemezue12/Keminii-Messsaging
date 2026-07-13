package com.astrotech.chat.controllers;

import com.astrotech.chat.core.GetCalculatedPagination;
import com.astrotech.chat.core.GetCurrentUser;
import com.astrotech.chat.dto.request.WsDeleteMessage;
import com.astrotech.chat.dto.request.WsEmojiToggleRequest;
import com.astrotech.chat.dto.request.WsForwardMessage;
import com.astrotech.chat.dto.request.WsReplyInboundMessage;
import com.astrotech.chat.dto.response.ConversationMessageResponse;
import com.astrotech.chat.dto.response.MessageResponse;
import com.astrotech.chat.dto.response.SliceResponse;
import com.astrotech.chat.dto.response.WsEditMessage;
import com.astrotech.chat.entites.Conversation;
import com.astrotech.chat.entites.Message;
import com.astrotech.chat.enums.SendMessageType;
import com.astrotech.chat.events.WsInboundMessage;
import com.astrotech.chat.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.chat.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
@Tag(name = "Message", description = "For sending, receiving, editing, deleting messages using http")
public class MessageController{
    private final MessageService messageService;
    private final GetCurrentUser getCurrentUser;

    @PostMapping("/{conversationId}/send")
    @Ratelimit
    public MessageResponse sendMessage(@PathVariable String conversationId, @Valid @RequestBody WsInboundMessage request){
        var userId = getCurrentUser.getCurrentUserId();
        return messageService.sendMessage(conversationId, userId, SendMessageType.TEXT, request.content());
    }
    @PostMapping("/{conversationId}/reply/{messageId}")
    @Ratelimit
    public MessageResponse replyToMessage(
            @PathVariable String conversationId,
            @PathVariable String messageId,
            @Valid @RequestBody WsReplyInboundMessage request) {

        var userId = getCurrentUser.getCurrentUserId();
        

        return messageService.replyToMessage(
                conversationId,
                userId,
                messageId,
                SendMessageType.TEXT,
                request.content()
        );
    }
    @PostMapping("/{messageId}/forward")
    @Ratelimit
    public List<MessageResponse> forwardMessage(
            @PathVariable String messageId,
            @Valid @RequestBody WsForwardMessage request) {

        var userId = getCurrentUser.getCurrentUserId();
        return messageService.forwardMessages(request.conversationIds(), userId, messageId);
    }


    @PutMapping("/{messageId}/edit")
    @Ratelimit
    public MessageResponse editMessage(@PathVariable String messageId, @Valid @RequestBody WsEditMessage request){
        var userId = getCurrentUser.getCurrentUserId();
        return messageService.editMessage(messageId, request.content(), userId);
    }
    @DeleteMapping("/{messageId}/delete")
    @Ratelimit
    public Map<String, MessageResponse> deleteMessage(@PathVariable String messageId, @Valid @RequestBody WsDeleteMessage request){
        return messageService.deleteMessage(messageId, request.deleteForAll(), getCurrentUser.getCurrentUserId());}

    @PutMapping("/{messageId}/toggle/emoji")
    @Ratelimit
    public void toggleReaction(@PathVariable String messageId, @Valid @RequestBody WsEmojiToggleRequest request){
        messageService.toggleReaction(messageId, request.emoji(), getCurrentUser.getCurrentUserId());

    }
    @GetMapping("/{conversationId}/get")
    @Ratelimit
    public SliceResponse<ConversationMessageResponse> getChatsHistory(
            @PathVariable String conversationId,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
            ){
        return messageService.getChatHistory(conversationId, page, size, getCurrentUser.getCurrentUserId());
    }
    @GetMapping("/{messageId}")
    @Ratelimit
    public Optional<ConversationMessageResponse> getChatHistory(
            @PathVariable String messageId
    ){
        return messageService.getMessageById(messageId, getCurrentUser.getCurrentUserId());
    }
}
