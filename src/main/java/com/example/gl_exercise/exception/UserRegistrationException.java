package com.example.gl_exercise.exception;

public class UserRegistrationException extends RuntimeException {
    public UserRegistrationException(String message, Throwable e) {
        super(message, e);
    }
}
