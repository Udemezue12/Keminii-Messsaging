package com.astrotech.chat.tasks;
import com.astrotech.chat.configProperties.PingerProperties;
import com.astrotech.chat.dto.response.PingResult;
import com.astrotech.chat.ping.WebsiteMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@RequiredArgsConstructor
public class PingUrlTask {

    private final WebsiteMonitoringService websiteMonitoringService;
    private final PingerProperties properties;

    @Job(name = "ping-urls")
    public void pingUrls() {

        log.info("Starting website health check");

        var results =
                websiteMonitoringService.pingAll(properties.pingUrls());

        var healthy =
                results.stream()
                        .filter(PingResult::healthy)
                        .count();

        log.info(
                "Completed website health check. Healthy: {}/{}",
                healthy,
                results.size()
        );
    }

    @Job(name = "ping-url")
    public void pingUrl(String url) {
        websiteMonitoringService.ping(url);
    }
}
