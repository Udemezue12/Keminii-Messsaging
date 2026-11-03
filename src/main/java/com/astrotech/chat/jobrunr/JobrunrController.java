package com.astrotech.chat.jobrunr;


import com.astrotech.chat.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.chat.responseBuilder.ApiResponse;
import com.astrotech.chat.responseBuilder.ApiResponseBuilder;
import com.astrotech.chat.service.ConversationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jobs")
@Tag(name = "JobRunr Jobs")
public class JobrunrController {

    private final JobRunrManagementService jobRunrManagementService;
    private final ConversationService conversationService;

    @GetMapping("/all-conversations/{userId}")
    @Ratelimit
    public Map<String, String> getConversations(@PathVariable String userId) {

        var page = 1;
        var size = 10;
        conversationService.getMyConversations(
                userId,
                page,
                size
        );
        return Map.of(
                "message",
                "Conversations loaded successfully"
        );


    }


    @PostMapping("/delete/successful")
    @Ratelimit(times = 4, seconds = 8)
    public ResponseEntity<ApiResponse<Integer>> deleteSucceededJobs() {
        var response = jobRunrManagementService.purgeSucceededJobs();
        return ApiResponseBuilder.success("Jobs deleted successfully", response);


    }

    @PostMapping("/delete/deleted")
    @Ratelimit(times = 4, seconds = 8)
    public ResponseEntity<ApiResponse<Integer>> deleteDeletedJobs() {
        var response = jobRunrManagementService.purgeDeletedJobs();
        return ApiResponseBuilder.success("Jobs deleted successfully", response);


    }
}
