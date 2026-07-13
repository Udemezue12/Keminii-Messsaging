package com.astrotech.chat.controllers;

import com.astrotech.chat.core.GetCalculatedPagination;
import com.astrotech.chat.core.GetCurrentUser;
import com.astrotech.chat.dto.response.SliceResponse;
import com.astrotech.chat.dto.response.UserResponse;
import com.astrotech.chat.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.chat.responseBuilder.ApiResponse;
import com.astrotech.chat.responseBuilder.ApiResponseBuilder;
import com.astrotech.chat.service.BlockedUserService;
import com.astrotech.chat.service.GroupInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "User Blocking", description = "For viewing Group Information")
@RequiredArgsConstructor
public class BlockedUserController {
    private final BlockedUserService blockedUserService;
    private final GetCurrentUser getCurrentUser;
    @PostMapping("/{targetUserId}/block")
    @Ratelimit
    public Map<String, String> block(@PathVariable String targetUserId) {
        blockedUserService.blockUser( getCurrentUser.getCurrentUserId(), targetUserId );
        return Map.of(
                "status", "User Blocked"
        );
    }
    @PostMapping("/{targetUserId}/unblock")
    @Ratelimit
    public Map<String, String> unblock(@PathVariable String targetUserId) {
        blockedUserService.unblockUser( getCurrentUser.getCurrentUserId(), targetUserId );
        return Map.of(
                "status", "User Unblocked"
        );
    }
    @GetMapping("/blocked-users")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<UserResponse>>> getBlockedUser(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ){
        var data = blockedUserService.getBlockedUsers(getCurrentUser.getCurrentUserId(), page, size);
        return ApiResponseBuilder.success("Fetched Successfully", data);
    }

    @GetMapping("/{targetId}/check-blocked")
    @Ratelimit
    public Map<String, Object> isBlocked(
            @PathVariable String targetId
    ){
        var result = blockedUserService.isBlockActive(getCurrentUser.getCurrentUserId(), targetId);
        return Map.of(
                "status",
                result
        );

    }


}
