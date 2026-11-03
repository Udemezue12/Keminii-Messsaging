package com.astrotech.chat.taskScheduler;

import com.astrotech.chat.configProperties.PingerProperties;
import com.astrotech.chat.jobrunr.JobRunrManagementService;
import com.astrotech.chat.tasks.DeleteBlacklistedTokenTask;
import com.astrotech.chat.tasks.PingUrlTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import static com.astrotech.chat.core.TrimWhiteSpace.sanitize;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class TaskScheduler {
    private final JobScheduler jobScheduler;
    private final DeleteBlacklistedTokenTask cleanupToken;
    private final PingerProperties properties;
    private final PingUrlTask pingUrlTask;
    private final JobRunrManagementService jobRunrManagementService;







    @EventListener(ApplicationReadyEvent.class)
    public void deleteSuccessfulJobs() {
        jobScheduler.scheduleRecurrently(
                "delete-successful-jobs",
                Cron.weekly(),
                jobRunrManagementService::purgeSucceededJobs

        );
        jobScheduler.scheduleRecurrently(
                "delete_successfully-deleted-jobs",
                Cron.weekly(),
                jobRunrManagementService::purgeDeletedJobs

        );

    }

    @EventListener(ApplicationReadyEvent.class)
    public void deleteTokens() {
        jobScheduler.scheduleRecurrently(
                "revoked-token-cleanup",
                Cron.weekly(),
                // "0 */2 * * * *",
                cleanupToken::cleanupExpiredTokens);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void pingUrls() {

        properties.pingUrls().forEach(site ->
                jobScheduler.scheduleRecurrently(
                        "ping-" + sanitize(site),
                        "*/8 * * * *",
                        () -> pingUrlTask.pingUrl(site)
                )
        );
    }

}
