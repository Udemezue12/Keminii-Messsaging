package com.astrotech.chat.ping;

import com.astrotech.chat.core.TrimWhiteSpace;
import com.astrotech.chat.dto.response.PingResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
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

            var uri = URI.create(normalizeUrl(url));

            var response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toBodilessEntity();

            var duration = System.currentTimeMillis() - start;
            log.error("Ping Success for {}", url);

            return new PingResult(
                    uri.toString(),
                    response.getStatusCode().value(),
                    duration,
                    response.getStatusCode().is2xxSuccessful(),
                    Instant.now(),
                    "Success"
            );

        } catch (Exception ex) {

            var duration = System.currentTimeMillis() - start;

            log.error("Ping failed for {}", url, ex);

            return new PingResult(
                    url,
                    0,
                    duration,
                    false,
                    Instant.now(),
                    ex.getMessage()
            );
        }
    }

    private String normalizeUrl(String url) {

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be blank");
        }

        url = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(url, false);

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        return url;
    }


}
