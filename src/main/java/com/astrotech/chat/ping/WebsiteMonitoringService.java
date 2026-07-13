package com.astrotech.chat.ping;

import com.astrotech.chat.dto.response.PingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class WebsiteMonitoringService {
    private final WebsitePingService pingService;

    //    @Async("pingExecutor")
    public CompletableFuture<PingResult> pingAsync(String url) {
        return CompletableFuture.completedFuture(
                pingService.ping(url)
        );
    }

    public PingResult ping(String url) {
        return pingService.ping(url);

    }

    public List<CompletableFuture<PingResult>> pingAllAsync(
            List<String> urls
    ) {

        return urls.stream()
                .map(this::pingAsync)
                .toList();
    }

    public List<PingResult> pingAll(
            List<String> urls
    ) {

        return urls.stream()
                .map(this::ping)
                .toList();
    }
}
