package com.astrotech.chat.events;

import com.astrotech.chat.enums.OnlineStatus;

import java.time.Instant;

public record PresenceEvent(String userId, OnlineStatus status, Instant timestamp) {
}
