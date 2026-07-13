package com.astrotech.chat.controllers;

import com.astrotech.chat.core.GetCalculatedPagination;
import com.astrotech.chat.core.GetCurrentUser;
import com.astrotech.chat.dto.request.AddContactRequest;
import com.astrotech.chat.dto.request.UpdateContactRequest;
import com.astrotech.chat.dto.response.SliceResponse;
import com.astrotech.chat.dto.response.UserContactResponse;
import com.astrotech.chat.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.chat.responseBuilder.ApiResponse;
import com.astrotech.chat.responseBuilder.ApiResponseBuilder;
import com.astrotech.chat.service.UserContactService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/contacts")
@Tag(name = "User Contacts", description = "for adding, updating, deleting and getting of contacts/phoneNumber")
public class UserContactController {
    private final UserContactService userContactService;
    private final GetCurrentUser getCurrentUser;


    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<UserContactResponse>>> getContacts(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var data = userContactService.getContacts(page, size,  getCurrentUser.getCurrentUserId());

        return ApiResponseBuilder.success("Fetched Successfully", data);
    }
    @GetMapping("/{contactId}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<UserContactResponse>> getContact(@PathVariable String contactId){
        var data = userContactService.getContact(contactId, getCurrentUser.getCurrentUserId());
        return ApiResponseBuilder.success("Fetched Successfully", data);
    }
    @PostMapping("/add")
    @Ratelimit
    public ResponseEntity<ApiResponse<UserContactResponse>> addContact(@Valid @RequestBody AddContactRequest request){
        var data = userContactService.addContact(request, getCurrentUser.getCurrentUserId());
        return  ApiResponseBuilder.success("Saved Successfully", data);
    }
    @PatchMapping("/{contactId}/update")
    @Ratelimit
    public ResponseEntity<ApiResponse<UserContactResponse>> updateContact(@PathVariable String contactId, @Valid @RequestBody UpdateContactRequest request){
        var data = userContactService.updateContact(request, contactId, getCurrentUser.getCurrentUserId());
        return  ApiResponseBuilder.success("Updated Successfully", data);
    }
    @DeleteMapping("/{contactId}/delete")
    @Ratelimit
    public Map<String, String> deleteContact(@PathVariable String contactId){
        userContactService.removeContact(contactId, getCurrentUser.getCurrentUserId());
        return Map.of("message", "Deleted Successfully");
    }



}
