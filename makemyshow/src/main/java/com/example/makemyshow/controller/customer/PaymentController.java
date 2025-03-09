package com.example.makemyshow.controller.customer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/customer/payments")
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "Payment Management", description = "APIs for payment operations")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    @Operation(summary = "Process payment for a booking")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @Valid @RequestBody PaymentRequestDto paymentRequest,
            Principal principal) {
        return ResponseEntity.ok(paymentService.processPayment(paymentRequest, principal.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment details by ID")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(paymentService.getPaymentById(id, principal.getName()));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Request refund for a payment")
    public ResponseEntity<PaymentResponseDto> requestRefund(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(paymentService.requestRefund(id, principal.getName()));
    }
}