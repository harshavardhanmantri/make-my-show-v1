package com.example.makemyshow.service.payment;

import com.example.makemyshow.dto.request.PaymentRequestDto;
import com.example.makemyshow.dto.response.PaymentResponseDto;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto paymentRequest, String email);
    PaymentResponseDto getPaymentById(Long id, String email);
    PaymentResponseDto requestRefund(Long id, String email);
}
