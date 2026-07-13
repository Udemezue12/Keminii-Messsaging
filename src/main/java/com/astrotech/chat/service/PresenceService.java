package com.astrotech.chat.service;


import com.astrotech.chat.core.GetCurrentUser;
import com.astrotech.chat.dto.response.PresenceResponse;
import com.astrotech.chat.enums.OnlineStatus;
import com.astrotech.chat.events.PresenceEvent;
import com.astrotech.chat.exceptions.BadRequestException;
import com.astrotech.chat.exceptions.ResourceNotFoundException;
import com.astrotech.chat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService {

    private static final Duration HEARTBEAT_TTL = Duration.ofMinutes(2);

    private static final String HEARTBEAT_KEY = "presence:heartbeat:";
    private static final String CONNECTION_KEY = "presence:connections:";
    private static final String ONLINE_KEY = "presence:online";
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GetCurrentUser getCurrentUser;


    @Async("taskExecutor")
    public void connect(String userId) {

        var now = Instant.now();

        var connections = redisTemplate.opsForValue().increment(CONNECTION_KEY + userId);

        redisTemplate.expire(CONNECTION_KEY + userId, HEARTBEAT_TTL);
        redisTemplate.opsForValue().set(
                HEARTBEAT_KEY + userId,
                now.toString(),
                HEARTBEAT_TTL
        );

        if (connections != null && connections == 1) {

            broadcast(userId, OnlineStatus.ONLINE, now);

            log.debug("User {} became ONLINE", userId);
        }
    }


    public void heartbeat(String userId) {

        redisTemplate.expire(CONNECTION_KEY + userId, HEARTBEAT_TTL);

        redisTemplate.opsForValue().set(
                HEARTBEAT_KEY + userId,
                Instant.now().toString(),
                HEARTBEAT_TTL
        );
    }


    @Async("taskExecutor")
    public void disconnect(String userId) {

        var now = Instant.now();

        var connections = redisTemplate.opsForValue().decrement(CONNECTION_KEY + userId);

        if (connections == null || connections <= 0) {

            redisTemplate.delete(CONNECTION_KEY + userId);
            redisTemplate.delete(HEARTBEAT_KEY + userId);

            userRepository.updateLastSeen(userId, now);

            broadcast(userId, OnlineStatus.OFFLINE, now);

            log.debug("User {} became OFFLINE", userId);
        }
    }


    public boolean isOnline(String userId) {
        return redisTemplate.hasKey(HEARTBEAT_KEY + userId);
    }

    public long getConnectionCount(String userId) {

        Object value = redisTemplate.opsForValue().get(CONNECTION_KEY + userId);

        if (value == null) {
            return 0;
        }

        return Long.parseLong(value.toString());
    }


    public Instant getLastHeartbeat(String userId) {

        var value = redisTemplate.opsForValue().get(HEARTBEAT_KEY + userId);

        if (value == null) {
            return null;
        }

        return Instant.parse(value.toString());
    }

    private void broadcast(
            String userId,
            OnlineStatus status,
            Instant timestamp
    ) {

        var event = new PresenceEvent(
                userId,
                status,
                timestamp
        );

        messagingTemplate.convertAndSend(
                "/topic/presence",
                event
        );
    }
    public PresenceResponse getPresence(String userId){
        var isOnline = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_KEY, userId));
        return userRepository.findById(userId)
                .map(u -> new PresenceResponse(
                        userId,
                        isOnline ? OnlineStatus.ONLINE : u.getOnlineStatus(),
                        u.getLastSeen()
                )).orElseThrow(() -> new ResourceNotFoundException("Not Found"));

    }
    public List<PresenceResponse> getBatchPresence(Map<String, List<String>> body){
        var userIds = body.getOrDefault("userIds", List.of());
        if (userIds.isEmpty()) {
            return List.of();
        }
        var onlineIds = redisTemplate.opsForSet().members(ONLINE_KEY);
        var onlineSet = onlineIds == null ? Set.of()
                : onlineIds.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        return userIds.stream()
                .distinct()
                .limit(1000)
                .map(uid -> {
                    boolean online = onlineSet.contains(uid);
                    return  PresenceResponse.builder()
                            .userId(uid)
                            .status(online ? OnlineStatus.ONLINE : OnlineStatus.OFFLINE)
                            .build();
                }).toList();

    }
    public Map<String, Object> updateStatus(Map<String, String> body){
        var statusStr = body.get("status");
        var userId = getCurrentUser.getCurrentUserId();
        if (statusStr == null) {
            throw new BadRequestException("");
        }
        OnlineStatus status;
        try{
            status = OnlineStatus.valueOf(statusStr.toUpperCase());


        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return Map.of("Invalid status. Use ONLINE, AWAY or BUSY", 400);
        }
        if (status == OnlineStatus.OFFLINE) {
            return Map.of("Cannot manually set status to OFFLINE", 400);
        }
        userRepository.updateOnlineStatus(userId, status, Instant.now());
        return Map.of("message", "Status updated");
    }

}
