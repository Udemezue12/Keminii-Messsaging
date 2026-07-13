package com.astrotech.chat.entites;

import com.astrotech.chat.core.AppGenerators;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chat_room")
@Setter
public class ChatRoom {
    @Id
    @Builder.Default
    private String id = AppGenerators.generateTimestampedUUID();
    private String chatId;
    private String senderId;
    private String recipientId;

}
