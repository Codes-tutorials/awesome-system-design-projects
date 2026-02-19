package com.example.antishake.exception;

public class DuplicateSubmitException extends RuntimeException {
    public DuplicateSubmitException(String message) {
        super(message);
    }
}
