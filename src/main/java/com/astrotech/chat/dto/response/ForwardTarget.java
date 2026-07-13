package com.astrotech.chat.dto.response;

import com.astrotech.chat.entites.*;

public record ForwardTarget(
        Conversation conversation,
        Message message) {
}
