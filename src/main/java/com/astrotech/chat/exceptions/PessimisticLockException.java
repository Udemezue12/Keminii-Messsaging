package com.astrotech.chat.exceptions;

public class PessimisticLockException extends RuntimeException{
    public PessimisticLockException(String message) {
        super(message);
    }
}
