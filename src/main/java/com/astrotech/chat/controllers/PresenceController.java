package com.astrotech.chat.controllers;


import com.astrotech.chat.customCache.CustomCacheEvict;
import com.astrotech.chat.customCache.CustomCacheable;
import com.astrotech.chat.dto.response.PresenceResponse;
import com.astrotech.chat.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.chat.service.PresenceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/presence")
@Tag(name = "Presence", description = "Updating User Presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;


    @GetMapping("/{userId}")
    @Ratelimit
    @CustomCacheable(value = "single-presence", key = "#userId")
    public PresenceResponse getPresence(
            @PathVariable String userId) {
        return presenceService.getPresence(userId);
    }


    @PostMapping("/batch")
    @Ratelimit
    @CustomCacheable(value = "batch-presence", key = "#all")
    public List<PresenceResponse> getBatchPresence(
            @RequestBody Map<String, List<String>> body) {
        return presenceService.getBatchPresence(body);
    }


    @PatchMapping("/status")
    @Ratelimit
    @CustomCacheEvict(cacheNames = {"batch-presence", "single-presence"}, allEntries = true)
    public Map<String, Object> updateStatus(
            @RequestBody Map<String, String> body) {
        return presenceService.updateStatus(body);

    }
}
