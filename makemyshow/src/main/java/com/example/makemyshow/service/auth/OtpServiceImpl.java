package com.example.makemyshow.service.auth;
import com.example.makemyshow.service.cache.CacheService;
import com.example.makemyshow.service.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OtpServiceImpl implements OtpService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private CacheService cacheService;

    private static final long OTP_EXPIRATION = 10; // 10 minutes

    private static final String OTP_KEY_PREFIX = "OTP:";

    @Override
    public void sendOtp(String email) {
        String otp = generateOtp();

        // Store OTP in cache
        cacheService.put(OTP_KEY_PREFIX + email, otp, OTP_EXPIRATION, TimeUnit.MINUTES);

        // For development, log the OTP instead of sending email
        System.out.println("==== OTP for " + email + ": " + otp + " ====");

//        try {
//            // Try to send the actual email
//            Map<String, Object> templateModel = new HashMap<>();
//            templateModel.put("otp", otp);
//            templateModel.put("expirationMinutes", OTP_EXPIRATION);
//
//            emailService.sendEmailWithTemplate(
//                    email,
//                    "MakeMyShow - Email Verification Code",
//                    "otp-template",
//                    templateModel
//            );
//        } catch (Exception e) {
//            // Log the error but don't fail the process during development
//            System.err.println("Failed to send email: " + e.getMessage());
//        }
    }

    @Override
    public boolean validateOtp(String email, String otp) {
        String cachedOtp = cacheService.get(OTP_KEY_PREFIX + email, String.class);

        if (cachedOtp == null) {
            return false;
        }

        boolean isValid = cachedOtp.equals(otp);

        if (isValid) {
            cacheService.delete(OTP_KEY_PREFIX + email);
        }

        return isValid;
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }
}
