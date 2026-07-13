package com.astrotech.chat.config;

import com.astrotech.chat.configProperties.NotificationProperties;
import com.astrotech.chat.configProperties.PaymentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    private final PaymentProperties properties;
    private final NotificationProperties notificationProperties;

    @Bean("paystackWebClient")
    public WebClient paystackWebClient() {
        return buildClient("https://api.paystack.co", properties.getPaystackSecretKey());
    }

    @Bean("flutterwaveWebClient")
    public WebClient flutterwaveWebClient() {
        return buildClient("https://api.flutterwave.com/v3", properties.getFlutterwaveSecretKey());
    }

    @Bean("fileHashWebClient")
    public WebClient fileHashWebClient() {
        return WebClient.builder()
                .build();
    }

    @Bean("brevoWebClient")
    public WebClient brevoWebClient() {
        return WebClient.builder()
                .baseUrl(notificationProperties.getBrevoUrl())
                .defaultHeader("api-key", notificationProperties.getBrevoApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean("termiiWebClient")
    public WebClient client() {

        return WebClient.builder()
                .baseUrl(notificationProperties.getTermiiBaseUrl())
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean("sendChampWebClient")
    public WebClient sendChampWebClient() {
        return WebClient.builder()
                .baseUrl(notificationProperties.getSendChampBaseUrl())
                .defaultCookie(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + notificationProperties.getSendChampApiKey())
                .build();
    }

    private WebClient buildClient(String baseUrl, String secretKey) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
