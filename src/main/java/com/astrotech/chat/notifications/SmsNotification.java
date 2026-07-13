package com.astrotech.chat.notifications;

import com.astrotech.chat.configProperties.NotificationProperties;
import com.astrotech.chat.sms.termii.TermiiClient;
import com.astrotech.chat.sms.termii.TermiiSmsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsNotification {

    private final TermiiClient termiiClient;

    private final NotificationProperties properties;

    public void sendOtp(
            String phone,
            String otp,
            String name) {

        termiiClient.sendOtpSms(
                phone,
                otp,
                null,
                name,
                properties.getTermiiSenderId());
    }

    public TermiiSmsResponse sendCustomSms(
            String phone,
            String message) {

        return termiiClient.sendOtpSms(
                phone,
                null,
                message,
                null,
                properties.getTermiiSenderId());
    }


}
