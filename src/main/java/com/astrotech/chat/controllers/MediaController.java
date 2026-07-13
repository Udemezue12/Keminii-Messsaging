package com.astrotech.chat.controllers;

import com.astrotech.chat.cloudinary.CloudinaryImageDeletionRequest;
import com.astrotech.chat.cloudinary.CloudinaryService;
import com.astrotech.chat.cloudinary.CloudinarySignatureRequest;
import com.astrotech.chat.cloudinary.CloudinarySignedUploadResponse;


import com.astrotech.chat.core.GetCurrentUser;
import com.astrotech.chat.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.chat.service.MediaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@Tag(name = "Media Uploads", description = "For sending media messages")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1")
public class MediaController {

    private final CloudinaryService cloudinaryService;
    private final GetCurrentUser getCurrentUser;
    private final MediaService mediaService;


    @PostMapping(value = "/upload/{conversationId}/message/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Ratelimit
    public Map<String, Object> upload(
            @RequestParam("file") MultipartFile file,
             @PathVariable String conversationId
            ) {

        log.info("REST request to upload media file for conversation: {}", conversationId);

        return mediaService.sendMediaMessage(file, conversationId, getCurrentUser.getCurrentUserId());
    }
    @PostMapping(value  = "/admin/upload-multiple/{conversationId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadMultiple(
            @RequestParam("files") MultipartFile[] files, @PathVariable String conversationId) {
        return mediaService.sendMultipleMediaMessage(files,  conversationId, getCurrentUser.getCurrentUserId());


    }


    @PostMapping("/admin/uploads/signature")

    @Ratelimit
    public CloudinarySignedUploadResponse generateUploadSignature(
            @RequestParam("conversationId") String conversationId, @Valid @RequestBody CloudinarySignatureRequest request) {

        log.info("REST request to generate client upload signature for conversation: {}", conversationId);

        return cloudinaryService.generateUploadSignature(
                conversationId, request
        );


    }


    @DeleteMapping("/cloudinary/{publicId}/delete")
    @Ratelimit
    public Map<String, String> delete(
            @PathVariable String publicId,
            @RequestParam(defaultValue = "image") String resourceType) {

        var userId = getCurrentUser.getCurrentUserId();
        log.info("REST request to delete media asset: {} by user: {}", publicId, userId);

        cloudinaryService.deleteResource(publicId, resourceType);

        return Map.of("success", "Media asset deleted from storage successfully");
    }
    @DeleteMapping("/cloudinary/{publicId}/delete/multiple")
    @Ratelimit
    public Map<String, String> deleteMultiple(
            @Valid @RequestBody CloudinaryImageDeletionRequest request) {



        cloudinaryService.deleteResources(request.publicIds());

        return Map.of("success", "Media asset deleted from storage successfully");
    }


}
