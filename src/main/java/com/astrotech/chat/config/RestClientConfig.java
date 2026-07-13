package com.astrotech.chat.config;

import com.astrotech.chat.configProperties.NotificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    private final NotificationProperties notificationProperties;

    @Bean("sharedRestClient")
    public RestClient sharedRestClient() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(20000);
        factory.setReadTimeout(10000);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Bean("termiiRestClient")
    public RestClient client() {
        var httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15))
                .build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(notificationProperties.getTermiiBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
