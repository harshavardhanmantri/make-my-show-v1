package com.example.makemyshow.dto.response;


import com.example.makemyshow.model.Payment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponseDto {
    private Long id;
    private String paymentId;
    private Long bookingId;
    private Double amount;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus status;
    private String transactionId;
    private LocalDateTime paymentTime;

    public static PaymentResponseDto fromPayment(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setPaymentId(payment.getPaymentId());
        dto.setBookingId(payment.getBooking().getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentTime(payment.getPaymentTime());
        return dto;
    }
}