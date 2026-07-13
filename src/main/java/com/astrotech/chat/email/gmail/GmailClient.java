package com.astrotech.chat.email.gmail;


import com.astrotech.chat.configProperties.NotificationProperties;
import com.astrotech.chat.core.NotificationCircuitBreaker;
import com.astrotech.chat.exceptions.NotificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailClient {
    private final JavaMailSender mailSender;
    private final NotificationProperties properties;
    private final NotificationCircuitBreaker breaker;

    public void sendSmtpEmail(
            String to,
            String subject,
            String htmlContent) {

        breaker.execute(() -> {

            try {

                var message =
                        mailSender.createMimeMessage();

                var helper =
                        new MimeMessageHelper(
                                message,
                                true,
                                StandardCharsets.UTF_8.name());

                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                helper.setFrom(
                        properties.getEmailUsername());

                mailSender.send(message);

                log.info(
                        "Email sent successfully to {}",
                        to);

                return true;

            } catch (Exception ex) {

                log.error(
                        "Failed to send email to {}",
                        to,
                        ex);

                throw new NotificationException(
                        "Failed to send email");
            }
        });
    }
    

}
