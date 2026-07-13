package com.astrotech.chat.tasks;

import com.astrotech.chat.events.UpdateMessageResponseAndLastMessageEvent;
import com.astrotech.chat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class UpdateMessageResponseAndLastMessageTask {
    private final JobScheduler jobScheduler;
    private final ConversationService conversationService;

    @Job(name = "update-last-message-task", retries = 3)
    @EventListener(UpdateMessageResponseAndLastMessageEvent.class)
    public void update(UpdateMessageResponseAndLastMessageEvent event){
        var convoId = event.convoId();
        var lastMessageContent = event.lastMessageContent();
        var lastMessageSender = event.lastMessageSender();
        jobScheduler.enqueue(() -> {
                   
                        conversationService.updateLastMessageSenderId(convoId, lastMessageContent, lastMessageSender);
        }
        );



    }
}
