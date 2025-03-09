package com.example.makemyshow.service.booking;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto bookingRequest, String email);
    List<BookingResponseDto> getUserBookings(String email);
    BookingResponseDto getBookingById(Long id, String email);
    BookingResponseDto cancelBooking(Long id, String email);
}
