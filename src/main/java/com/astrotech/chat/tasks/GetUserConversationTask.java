package com.astrotech.chat.tasks;

import com.astrotech.chat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
@Slf4j
public class GetUserConversationTask {
    private final ConversationService conversationService;

    @Job(name = "user-conversations", retries = 2)
    public void fetchAndLogMyConversations(String userId, int page, int size) {
        log.info("Starting GettingConversations Jobs");
        var conversation = conversationService.getMyConversations(
                userId, page,size
        );
        log.info("Result from scheduled job: {}", conversation);
    }
}
