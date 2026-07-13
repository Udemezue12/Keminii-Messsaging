package com.astrotech.chat.repositories;

import java.util.List;

import com.astrotech.chat.entites.MessageReaction;

public interface MessageReactionProjection {
    
    List<MessageReaction> getReactions();
}