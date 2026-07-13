package com.astrotech.chat.entites;

import com.astrotech.chat.core.AppGenerators;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    @Builder.Default
    private String Id = AppGenerators.generateTimestampedUUID();
    private String chatId;
    private String senderId;
    private String recipientId;
    private String content;
    private LocalDateTime timestamp;
}
