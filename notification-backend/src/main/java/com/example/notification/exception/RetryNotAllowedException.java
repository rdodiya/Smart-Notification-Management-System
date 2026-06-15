package com.example.notification.exception;

public class RetryNotAllowedException extends RuntimeException {
    public RetryNotAllowedException(String message) {
        super(message);
    }
}
