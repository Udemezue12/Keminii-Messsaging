package com.astrotech.chat.taskScheduler;

import com.astrotech.chat.configProperties.PingerProperties;
import com.astrotech.chat.tasks.DeleteBlacklistedTokenTask;
import com.astrotech.chat.tasks.PingUrlTask;
import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import static com.astrotech.chat.core.TrimWhiteSpace.sanitize;

@RequiredArgsConstructor
@Configuration
public class TaskScheduler {
    private final JobScheduler jobScheduler;
    private final DeleteBlacklistedTokenTask cleanupToken;
    private final PingerProperties properties;
    private final PingUrlTask pingUrlTask;
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
