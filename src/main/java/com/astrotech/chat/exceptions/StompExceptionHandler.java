package com.astrotech.chat.exceptions;

import org.springframework.web.bind.annotation.ExceptionHandler;


public class StompExceptionHandler extends  RuntimeException {
    public StompExceptionHandler(String message) {
        super(message);
    }
}

