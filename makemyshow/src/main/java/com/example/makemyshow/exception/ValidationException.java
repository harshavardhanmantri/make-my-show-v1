package com.example.makemyshow.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

    private final ErrorCode errorCode;

    public ValidationException(String message) {
        super(message);
        this.errorCode = ErrorCode.VALIDATION_ERROR;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.VALIDATION_ERROR;
    }

    public ValidationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

