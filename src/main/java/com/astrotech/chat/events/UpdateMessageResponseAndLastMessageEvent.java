package com.astrotech.chat.events;

public record UpdateMessageResponseAndLastMessageEvent(
        String convoId,
        String lastMessageContent,
        String lastMessageSender
) {


}
