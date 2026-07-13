package com.astrotech.chat.service;


import com.astrotech.chat.entites.ChatRoom;
import com.astrotech.chat.repositories.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepo;

    public Optional<String> getChatRoomId(String senderId, String recipientId, boolean createRoomIfNotExists) {
        return
                chatRoomRepo.findBySenderIdAndRecipientId(senderId, recipientId)
                        .map(ChatRoom::getChatId)
                        .or(
                                () -> {
                                    if (createRoomIfNotExists) {
                                        var chatId = createChatId(senderId, recipientId);
                                        return Optional.of(chatId);
                                    }
                                    return Optional.empty();
                                }
                        );
    }

    private String createChatId(String senderId, String recipientId) {
        var chatId = String.format("%s_%s", senderId, recipientId);
        var senderRecipient = ChatRoom.builder()
                .chatId(chatId)
                .senderId(senderId)
                .recipientId(recipientId)
                .build();
        var recipientSender = ChatRoom.builder()
                .chatId(chatId)
                .senderId(recipientId)
                .recipientId(senderId)
                .build();
        chatRoomRepo.save(senderRecipient);
        chatRoomRepo.save(recipientSender);
        return chatId;
    }
}
