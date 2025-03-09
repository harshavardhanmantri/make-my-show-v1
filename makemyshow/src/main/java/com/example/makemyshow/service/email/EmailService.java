package com.example.makemyshow.service.email;

import java.util.Map;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String body);
    void sendEmailWithTemplate(String to, String subject, String templateName, Map<String, Object> templateModel);
}
