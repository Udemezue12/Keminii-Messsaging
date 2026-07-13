package com.astrotech.chat.messages;

public class SendOtpAndLinkMessage {
    public static String buildSmsMessage(
            String otp,
            String message,
            String name) {

        if (message != null && !message.isBlank()) {
            return message;
        }

        if (name != null && !name.isBlank()) {

            return String.format(
                    "Hello %s, your OTP is %s. "
                            + "This code expires in 5 minutes. "
                            + "Do not share it with anyone.",
                    name,
                    otp);
        }

        return String.format(
                "Your OTP is %s. "
                        + "This code expires in 5 minutes. "
                        + "Do not share it with anyone.",
                otp);
    }
}
