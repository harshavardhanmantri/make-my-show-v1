package com.example.makemyshow.service.auth;

public interface OtpService {
    void sendOtp(String email);
    boolean validateOtp(String email, String otp);
}