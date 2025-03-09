package com.example.makemyshow.controller.auth;

import com.example.makemyshow.service.auth.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/otp")
@Tag(name = "OTP Management", description = "APIs for OTP operations")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    @Operation(summary = "Send OTP to email")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        otpService.sendOtp(email);
        return ResponseEntity.ok().body("OTP sent successfully");
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate OTP")
    public ResponseEntity<?> validateOtp(@RequestParam String email, @RequestParam String otp) {
        boolean isValid = otpService.validateOtp(email, otp);
        return ResponseEntity.ok().body(isValid);
    }
}
