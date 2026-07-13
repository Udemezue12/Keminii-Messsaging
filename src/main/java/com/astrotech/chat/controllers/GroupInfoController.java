package com.astrotech.chat.controllers;

import com.astrotech.chat.core.GetCalculatedPagination;
import com.astrotech.chat.core.GetCurrentUser;
import com.astrotech.chat.dto.request.GroupChatRequest;
import com.astrotech.chat.dto.response.GroupInfoResponse;
import com.astrotech.chat.dto.response.SliceResponse;
import com.astrotech.chat.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.chat.responseBuilder.ApiResponse;
import com.astrotech.chat.responseBuilder.ApiResponseBuilder;
import com.astrotech.chat.service.GroupInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "GroupInfo", description = "For viewing Group Information")
@RequiredArgsConstructor
public class GroupInfoController {
    private final GroupInfoService groupInfoService;
    private final GetCurrentUser getCurrentUser;

    @PostMapping("/groupInfo/{conversationId}/create")
    @Ratelimit
    public ResponseEntity<ApiResponse<GroupInfoResponse>> createGroupInfo(@PathVariable String conversationId, @Valid @RequestBody GroupChatRequest request) {
        var data = groupInfoService.createGroupInfo(conversationId, getCurrentUser.getCurrentUserId(), request.name(), request.description(), request.avatarUrl());
        return ApiResponseBuilder.success("Created Successfully", data);

    }
    @PostMapping("/groupInfo/get/members")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<GroupInfoResponse>>> getGroupInfoForMembers(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ){
        var data = groupInfoService.getGroupsForUser(getCurrentUser.getCurrentUserId(), page, size);
        return ApiResponseBuilder.success("Fetched Successfully", data);


    }
    @PostMapping("/groupInfo/get/admins")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<GroupInfoResponse>>> getGroupInfoForAdmins(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ){
        var data = groupInfoService.getGroupsWhereAdmin(getCurrentUser.getCurrentUserId(), page, size);
        return ApiResponseBuilder.success("Fetched Successfully", data);


    }


}
