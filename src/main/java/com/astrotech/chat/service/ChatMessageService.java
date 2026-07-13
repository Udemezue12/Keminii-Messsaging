package com.astrotech.chat.service;

import com.astrotech.chat.core.AppGenerators;
import com.astrotech.chat.entites.ChatMessage;
import com.astrotech.chat.repositories.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatMessageService {

    private final ChatRoomService roomService;
    private final ChatMessageRepository chatMessageRepo;

    @Transactional
    public ChatMessage saveChat(ChatMessage message) {
        var chatId = roomService.
                getChatRoomId(
                        message.getSenderId(),
                        message.getRecipientId(), true).orElseThrow();
        message.setChatId(chatId);
        message.setTimestamp(LocalDateTime.now());
        return chatMessageRepo.save(message);
    }
    @Transactional
    public ChatMessage saveGroupChat(ChatMessage message, String groupId, String userId){
        var chatId = AppGenerators.generateTimestampedUUID();


        message.setChatId(chatId);
        message.setSenderId(userId);
        message.setTimestamp(LocalDateTime.now());
        return chatMessageRepo.save(message);

    }


    public List<ChatMessage> findChatMessages(
            String recipientId, String senderId
    ) {
        var chatRoomId = roomService.
                getChatRoomId(senderId, recipientId, true);
        return chatRoomId.map(chatMessageRepo::findByChatId).orElse(new ArrayList<>());

    }
    public List<ChatMessage> getMessagesByChatId(String chatId) {
        return chatMessageRepo.findByChatId(chatId);
    }
}
