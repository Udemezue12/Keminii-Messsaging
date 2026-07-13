package com.astrotech.chat.email.brevo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BrevoEmailRequest {

    private Sender sender;

    private List<Recipient> to;

    private String subject;

    private String htmlContent;

    private String textContent;

    @Data
    @Builder
    public static class Sender {

        private String name;

        private String email;
    }

    @Data
    @Builder
    public static class Recipient {

        private String email;

        private String name;
    }
}
