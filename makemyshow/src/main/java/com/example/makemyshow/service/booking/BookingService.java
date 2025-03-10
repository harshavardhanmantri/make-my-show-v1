package com.example.makemyshow.service.booking;

import com.example.makemyshow.dto.request.BookingRequestDto;
import com.example.makemyshow.dto.response.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto bookingRequest, String email);
    List<BookingResponseDto> getUserBookings(String email);
    BookingResponseDto getBookingById(Long id, String email);
    BookingResponseDto cancelBooking(Long id, String email);
}
