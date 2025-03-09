package com.example.makemyshow.exception;

public enum ErrorCode {
    // Common errors
    VALIDATION_ERROR(400, "Validation error"),
    RESOURCE_NOT_FOUND(404, "Resource not found"),
    UNAUTHORIZED(401, "Unauthorized access"),
    FORBIDDEN(403, "Access forbidden"),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),

    // Authentication errors
    AUTHENTICATION_FAILED(401, "Authentication failed"),
    INVALID_TOKEN(401, "Invalid token"),
    TOKEN_EXPIRED(401, "Token expired"),

    // Business errors
    SEAT_ALREADY_BOOKED(400, "Seat already booked"),
    SHOW_FULL(400, "Show is full"),
    PAYMENT_FAILED(400, "Payment processing failed"),
    PAST_SHOW_BOOKING(400, "Cannot book tickets for past shows"),
    BOOKING_CANCELLED(400, "Booking already cancelled"),
    INVALID_BOOKING_STATUS(400, "Invalid booking status for this operation");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}