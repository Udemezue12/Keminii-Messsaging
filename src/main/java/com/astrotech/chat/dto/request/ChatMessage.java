package com.astrotech.chat.dto.request;

import com.astrotech.chat.enums.MessageType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    private  String content;
    private String sender;
   private MessageType type;
}
