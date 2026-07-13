package com.astrotech.chat.controllers;


import com.astrotech.chat.core.GetCalculatedPagination;
import com.astrotech.chat.core.GetCurrentUser;
import com.astrotech.chat.dto.request.*;
import com.astrotech.chat.dto.response.ConversationResponse;
import com.astrotech.chat.dto.response.DirectConversationResponse;
import com.astrotech.chat.dto.response.SliceResponse;
import com.astrotech.chat.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.chat.responseBuilder.ApiResponse;
import com.astrotech.chat.responseBuilder.ApiResponseBuilder;
import com.astrotech.chat.service.ConversationService;
import com.astrotech.chat.validators.email.verified.EmailVerified;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Conversation", description = "For everything that relates to conversations")
@RequestMapping("/api/v1/")
public class ConversationController {
    private final ConversationService conversationService;
    private final GetCurrentUser getCurrentUser;





    @GetMapping("/conversations")
    @Ratelimit
    @EmailVerified
    public ResponseEntity<ApiResponse<SliceResponse<ConversationResponse>>> getMyConversations(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
            ) {
        var convo = conversationService.getMyConversations(getCurrentUser.getCurrentUserId(), page, size);
        return ApiResponseBuilder.success("Fetched Successfully",convo);
    }


    @GetMapping("/conversation/{id}")
    @Ratelimit
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @PathVariable String id) {
        var convo = conversationService.getById(id, getCurrentUser.getCurrentUserId());

        return ApiResponseBuilder.success("Fetched successfully", convo);
    }



    @PostMapping("/conversations/direct/{targetUserId}")
    @Ratelimit
    public ResponseEntity<ApiResponse<DirectConversationResponse>> openDirect(
            @PathVariable String targetUserId) {
        var response = conversationService.getOrCreateDirect(targetUserId, getCurrentUser.getCurrentUserId());
        return ApiResponseBuilder.success("Success", response);
    }




    @PostMapping("/conversation/groups/create")
    public ResponseEntity<ConversationResponse> createGroup(
            @Valid @RequestBody GroupChatRequest request) {
        var response = conversationService.createGroup(request, getCurrentUser.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PatchMapping("/conversation/{id}/update")
    public ResponseEntity<ConversationResponse> updateGroup(
            @PathVariable String id,
            @Valid @RequestBody UpdateGroupRequest request) {
        var convo = conversationService.updateGroup(id, request, getCurrentUser.getCurrentUserId());
        return ResponseEntity.ok(convo);
    }


    @DeleteMapping("/conversation/{id}/group/delete")
    public Map<String, String> deleteGroup(
            @PathVariable String id) {
        conversationService.deleteGroup(id, getCurrentUser.getCurrentUserId());
        return Map.of("message", "Deleted Successfully");
    }


    @PostMapping("/conversation/join/{code}")
    public ResponseEntity<ApiResponse<ConversationResponse>> joinByCode(
            @PathVariable String code) {
        return ApiResponseBuilder.success("Joined Successfully",conversationService.joinByInviteCode(code));
    }


    @PostMapping("/conversation/{conversationId}/leave")
    public Map<String, String> leave(
           @PathVariable String conversationId, @Valid LeaveConversationRequest request) {
        conversationService.leaveConversation(conversationId, request.newOwnerId());
        return Map.of("message", "You have successfully left this conversation");
    }

//    @GetMapping("/conversation{id}/members")
//    public ResponseEntity<ApiResponse<List<ConversationMemberResponse>>> getMembers(
//            @PathVariable String id) {
//        return ApiResponseBuilder.success("Success",conversationService.getMembers(id));
//    }


    @PostMapping("/conversation/{id}/member/{userId}/add")
    public Map<String, Object> addMember(
            @PathVariable String conversationId,
            @PathVariable String userId) {
         var result = conversationService.addMemberToGroup(conversationId, userId, getCurrentUser.getCurrentUserId());
        return Map.of("message", result);
    }
    @PostMapping("/conversation/{id}/members/{userId}/add")
    public Map<String, Object> addMembers(
            @PathVariable String conversationId,
            @Valid AddMembersToGroupRequest request) {
        conversationService.addMembersToGroup(conversationId, request.userId(), getCurrentUser.getCurrentUserId());
        return Map.of("message","Members added successfully");
    }


    @DeleteMapping("/conversation/{conversationId}/members/{userId}/remove")
    public Map<String, String> removeMember(
            @PathVariable String conversationId,
            @PathVariable String userId) {
        conversationService.removeMemberFromGroup(conversationId, userId);
        return Map.of("message","Member removed successfully");
    }


    @PatchMapping("/conversation/{id}/members/{userId}/role/update")
    public Map<String, String> updateMemberRole(
            @PathVariable String conversationId,
            @PathVariable String userId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        conversationService.changeMemberRole(conversationId, userId, request.role(), getCurrentUser.getCurrentUserId());
        return Map.of("message", "Role updated");
    }




    @PostMapping("/conversation/{conversationId}/mute")
    public Map<String, String> mute(
            @PathVariable String conversationId
            ) {
        return conversationService.muteConversation(conversationId, getCurrentUser.getCurrentUserId());

    }
    @PostMapping("/conversation/{conversationId}/unmute")
    public Map<String, String> unMute(
            @PathVariable String conversationId
    ) {
        return conversationService.unMuteConversation(conversationId, getCurrentUser.getCurrentUserId());

    }
    @PostMapping("/conversation/{conversationId}/pin")
    public Map<String, String> pin(
            @PathVariable String conversationId
    ) {
        return conversationService.pinConversation(conversationId, getCurrentUser.getCurrentUserId());

    }
    @PostMapping("/conversation/{conversationId}/unpin")
    public Map<String, String> unpin(
            @PathVariable String conversationId
    ) {
        return conversationService.unpinConversation(conversationId, getCurrentUser.getCurrentUserId());

    }




    @PostMapping("/conversation/{conversationId}/invite-code/regenerate")
    public Map<String, String> regenerateInviteCode(
            @PathVariable String conversationId) {
        String newCode = conversationService.regenerateInviteCode(conversationId, getCurrentUser.getCurrentUserId());
        return Map.of("inviteCode", newCode);}
}
