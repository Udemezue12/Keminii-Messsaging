package com.astrotech.chat.sms.termii;

import lombok.Data;

@Data
public class TermiiSmsResponse {

    private String code;
    private String message_id;
    private String message;
    private String balance;
    private String user;
}
