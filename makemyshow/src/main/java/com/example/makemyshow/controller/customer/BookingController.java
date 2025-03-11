package com.example.makemyshow.controller.customer;

import com.example.makemyshow.dto.request.BookingRequestDto;
import com.example.makemyshow.dto.response.BookingResponseDto;
import com.example.makemyshow.service.booking.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/bookings")
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
@Tag(name = "Booking Management", description = "APIs for booking operations")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody BookingRequestDto bookingRequest,
            Principal principal) {
        return ResponseEntity.ok(bookingService.createBooking(bookingRequest, principal.getName()));
    }

    @GetMapping
    @Operation(summary = "Get all bookings for the current user")
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(Principal principal) {
        return ResponseEntity.ok(bookingService.getUserBookings(principal.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details by ID")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(bookingService.getBookingById(id, principal.getName()));
    }

//    @PostMapping("/{id}/cancel")
//    @Operation(summary = "Cancel a booking")
//    public ResponseEntity<BookingResponseDto> cancelBooking(@PathVariable Long id, Principal principal) {
//        return ResponseEntity.ok(bookingService.cancelBooking(id, principal.getName()));
//    }
}
