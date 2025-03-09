package com.example.makemyshow.controller.auth;

import com.example.makemyshow.dto.request.UserRegistrationDto;
import com.example.makemyshow.model.user.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @PostMapping("/register/customer")
    @Operation(summary = "Register a new customer")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody UserRegistrationDto registrationDto) {
        return ResponseEntity.ok(authService.registerUser(registrationDto, UserRole.ROLE_CUSTOMER));
    }

    @PostMapping("/register/theater-owner")
    @Operation(summary = "Register a new theater owner")
    public ResponseEntity<?> registerTheaterOwner(@Valid @RequestBody UserRegistrationDto registrationDto) {
        return ResponseEntity.ok(authService.registerUser(registrationDto, UserRole.ROLE_THEATER_OWNER));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user and get tokens")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<JwtResponseDto> refreshToken(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with OTP")
    public ResponseEntity<?> verifyEmail(@RequestParam String email, @RequestParam String otp) {
        authService.verifyEmail(email, otp);
        return ResponseEntity.ok().body("Email verified successfully");
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP for email verification")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        otpService.sendOtp(email);
        return ResponseEntity.ok().body("OTP sent successfully");
    }
}
