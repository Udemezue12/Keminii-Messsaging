package com.astrotech.chat.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(String message, String ex) {
        super(message, new Throwable(ex));
    }
}
