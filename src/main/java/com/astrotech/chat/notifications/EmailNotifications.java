package com.astrotech.chat.notifications;

import com.astrotech.chat.configProperties.NotificationProperties;

import com.astrotech.chat.email.brevo.BrevoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotifications {


    private final NotificationProperties properties;

    private final BrevoClient brevoClient;

    public void sendVerificationEmail(
            String email,
            String otp,
            String token,
            String name) {

        String verifyLink = properties.getFrontendUrl()
                + "/verify-email.html?token="
                + token;

        String html = """
                <html>
                <body style="font-family: Arial;">
                    <h2>Verify Your Email</h2>
                    <p>Hello %s,</p>
                    <p>Your OTP is:</p>
                    <h3>%s</h3>
                    <a href="%s">
                        Verify Email
                    </a>
                </body>
                </html>
                """.formatted(name, otp, verifyLink);

        String text = """
                Hello %s,
                
                Your OTP is: %s
                
                Verify email:
                %s
                """.formatted(name, otp, verifyLink);

        brevoClient.sendBrevoEmail(
                email,
                name,
                "Verify Your Email",
                html,
                text);
    }

    public void sendPasswordResetEmail(
            String email,
            String otp,
            String token,
            String name) {

        String resetLink = properties.getFrontendUrl()
                + "/reset-password?token="
                + token;

        String html = """
                <html>
                <body style="font-family: Arial;">
                    <h2>Password Reset</h2>
                    <p>Hello %s,</p>
                    <p>Your OTP is:</p>
                    <h3>%s</h3>
                    <a href="%s">
                        Reset Password
                    </a>
                </body>
                </html>
                """.formatted(name, otp, resetLink);

        String text = """
                Hello %s,
                
                Your Password Reset OTP is: %s
                
                Reset password:
                %s
                """.formatted(name, otp, resetLink);

        brevoClient.sendBrevoEmail(
                email,
                name,
                "Reset Your Password",
                html,
                text);
    }}
