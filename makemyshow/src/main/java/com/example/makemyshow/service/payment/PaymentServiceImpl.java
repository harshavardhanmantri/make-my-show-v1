package com.example.makemyshow.service.payment;

import com.example.makemyshow.dto.request.PaymentRequestDto;
import com.example.makemyshow.dto.response.PaymentResponseDto;
import com.example.makemyshow.exception.ResourceNotFoundException;
import com.example.makemyshow.exception.UnauthorizedException;
import com.example.makemyshow.model.Booking;
import com.example.makemyshow.model.Payment;
import com.example.makemyshow.model.user.User;
import com.example.makemyshow.repository.BookingRepository;
import com.example.makemyshow.repository.PaymentRepository;
import com.example.makemyshow.repository.UserRepository;
import com.example.makemyshow.service.email.EmailService;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto paymentRequest, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(paymentRequest.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Security check - only the booking owner can make payment
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to make payment for this booking");
        }

        // Check if booking is already paid
        if (booking.getPayment() != null &&
                booking.getPayment().getStatus() == Payment.PaymentStatus.COMPLETED) {
            throw new ValidationException("Booking is already paid");
        }

        // Validate payment amount
        if (!paymentRequest.getAmount().equals(booking.getTotalAmount())) {
            throw new ValidationException("Payment amount does not match booking amount");
        }

        // Create payment
        Payment payment = new Payment();
        payment.setPaymentId("PAY" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        payment.setBooking(booking);
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId(paymentRequest.getTransactionId());
        payment.setPaymentTime(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPayment(savedPayment);
        bookingRepository.save(booking);

        // Send booking confirmation email
//        sendBookingConfirmationEmail(booking);

        return PaymentResponseDto.fromPayment(savedPayment);
    }

    @Override
    public PaymentResponseDto getPaymentById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Security check - only the payment owner can view it
        if (!payment.getBooking().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to view this payment");
        }

        return PaymentResponseDto.fromPayment(payment);
    }

    @Override
    @Transactional
    public PaymentResponseDto requestRefund(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Security check - only the payment owner can request refund
        if (!payment.getBooking().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to request refund for this payment");
        }

        // Check if payment is already refunded
        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED) {
            throw new ValidationException("Payment is already refunded");
        }

        // Check if show time is in the future (to allow refund)
        if (payment.getBooking().getShow().getStartTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Cannot request refund for past shows");
        }

        // Update payment status
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        Payment updatedPayment = paymentRepository.save(payment);

        // Update booking status
        Booking booking = payment.getBooking();
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return PaymentResponseDto.fromPayment(updatedPayment);
    }

    /**
     * Send booking confirmation email
     */
    private void sendBookingConfirmationEmail(Booking booking) {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("booking", booking);
        templateModel.put("user", booking.getUser());
        templateModel.put("show", booking.getShow());
        templateModel.put("movie", booking.getShow().getMovie());
        templateModel.put("theater", booking.getShow().getScreen().getTheater());
        templateModel.put("seats", booking.getSeats());

        emailService.sendEmailWithTemplate(
                booking.getUser().getEmail(),
                "Booking Confirmation - " + booking.getBookingNumber(),
                "booking-confirmation",
                templateModel
        );
    }
}
