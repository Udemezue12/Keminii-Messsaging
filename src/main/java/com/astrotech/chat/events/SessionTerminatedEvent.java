package com.astrotech.chat.events;

import org.springframework.context.ApplicationEvent;

public record SessionTerminatedEvent(String userId, String sessionKey){
}
