package com.example.makemyshow.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.RESOURCE_NOT_FOUND;
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.RESOURCE_NOT_FOUND;
    }
}