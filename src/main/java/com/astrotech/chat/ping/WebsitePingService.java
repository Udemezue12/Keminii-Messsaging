package com.astrotech.chat.ping;

import com.astrotech.chat.dto.response.PingResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Slf4j
@Service

public class WebsitePingService {
    private final RestClient restClient;

    public WebsitePingService(@Qualifier("sharedRestClient") RestClient restClient) {
        this.restClient = restClient;
    }


    @CircuitBreaker(name = "website-ping")
    public PingResult ping(String url) {
        var start = System.currentTimeMillis();
        try {

            var response = restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity();

            var status = response.getStatusCode();
            var duration = System.currentTimeMillis() - start;
            var healthy = status.is2xxSuccessful();
            return new PingResult(
                    url,
                    status.value(),
                    duration,
                    healthy,
                    Instant.now(),
                    "Success"
            );
        } catch (Exception e) {
            var duration = System.currentTimeMillis() - start;

            log.error("Ping failed for {}", url, e);

            return new PingResult(
                    url,
                    0,
                    duration,
                    false,
                    Instant.now(),
                    e.getMessage());
        }
    }


}
