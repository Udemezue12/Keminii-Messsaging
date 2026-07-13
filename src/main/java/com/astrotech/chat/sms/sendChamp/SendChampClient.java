package com.astrotech.chat.sms.sendChamp;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class SendChampClient {
    private final WebClient client;


    public SendChampClient(@Qualifier("sendChampWebClient") WebClient client) {
        this.client = client;
    }

    public Mono<String> sendOtpSms(List<String> to,
                                   String otp,
                                   String message,
                                   String name,
                                   String senderId, String route) {
        var request = new SendSmsRequest(to, otp, message, name, senderId, route);
        return client
                .post()
                .uri("/sms/send")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);

    }
}
