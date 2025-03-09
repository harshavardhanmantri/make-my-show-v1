package com.example.makemyshow.service.auth;


import com.example.makemyshow.dto.request.LoginRequestDto;
import com.example.makemyshow.dto.request.UserRegistrationDto;
import com.example.makemyshow.dto.response.JwtResponseDto;
import com.example.makemyshow.model.user.UserRole;

public interface AuthService {
    Object registerUser(UserRegistrationDto registrationDto, UserRole role);
    JwtResponseDto authenticateUser(LoginRequestDto loginRequest);
    JwtResponseDto refreshToken(String refreshToken);
    void verifyEmail(String email, String otp);
}
