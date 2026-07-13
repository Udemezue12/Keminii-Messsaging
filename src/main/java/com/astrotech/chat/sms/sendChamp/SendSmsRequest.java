package com.astrotech.chat.sms.sendChamp;

import java.util.List;

public  record SendSmsRequest(
        List<String> to,
        String otp,
        String message,
        String name,
        String senderId,
        String route
) {
}
