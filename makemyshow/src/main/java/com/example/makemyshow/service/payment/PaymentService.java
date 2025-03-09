package com.example.makemyshow.service.payment;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto paymentRequest, String email);
    PaymentResponseDto getPaymentById(Long id, String email);
    PaymentResponseDto requestRefund(Long id, String email);
}
