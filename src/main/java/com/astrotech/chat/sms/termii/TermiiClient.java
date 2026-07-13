package com.astrotech.chat.sms.termii;


import com.astrotech.chat.configProperties.NotificationProperties;
import com.astrotech.chat.core.MonoAndNormalizeSms;
import com.astrotech.chat.exceptions.NotificationException;
import com.astrotech.chat.messages.SendOtpAndLinkMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class TermiiClient {
    private final WebClient client;
    private final NotificationProperties properties;
    private final MonoAndNormalizeSms monoAndNormalizeSms;

    public TermiiClient(@Qualifier("termiiWebClient") WebClient client, NotificationProperties properties, MonoAndNormalizeSms monoAndNormalizeSms) {
        this.client = client;
        this.properties = properties;
        this.monoAndNormalizeSms = monoAndNormalizeSms;
    }

    public boolean ping() {

        try {

            Map<String, Object> payload = Map.of(
                    "to", "2340000000000",
                    "from", properties.getTermiiSenderId(),
                    "sms", "Ping test",
                    "type", "plain",
                    "channel", "generic",
                    "api_key", properties.getTermiiApiKey());

            TermiiSmsResponse response = client
                    .post()
                    .uri("/api/sms/send")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(TermiiSmsResponse.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            return response != null;

        } catch (Exception ex) {

            log.error("Termii ping failed", ex);

            return false;
        }
    }

    public TermiiSmsResponse sendOtpSms(
            String to,
            String otp,
            String message,
            String name,
            String senderId) {

        var smsMessage = SendOtpAndLinkMessage.buildSmsMessage(
                otp,
                message,
                name);

        Map<String, Object> payload = Map.of(
                "to", monoAndNormalizeSms.normalizePhone(to),
                "from", senderId,
                "sms", smsMessage,
                "type", "plain",
                "channel", "generic",
                "api_key", properties.getTermiiApiKey());

        var response = client
                .post()
                .uri("/api/sms/send")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(TermiiSmsResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        if (response == null) {
            throw new NotificationException(
                    "Empty response from Termii");
        }

        return response;
    }


}
