package com.example.makemyshow.dto.response;


import com.example.makemyshow.model.Booking;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class BookingResponseDto {
    private Long id;
    private String bookingNumber;
    private Long showId;
    private String movieTitle;
    private String theaterName;
    private String screenName;
    private LocalDateTime showTime;
    private List<String> seats;
    private Double totalAmount;
    private Booking.BookingStatus status;
    private PaymentResponseDto payment;
    private LocalDateTime bookingTime;

    public static BookingResponseDto fromBooking(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setBookingNumber(booking.getBookingNumber());
        dto.setShowId(booking.getShow().getId());
        dto.setMovieTitle(booking.getShow().getMovie().getTitle());
        dto.setTheaterName(booking.getShow().getScreen().getTheater().getName());
        dto.setScreenName(booking.getShow().getScreen().getName());
        dto.setShowTime(booking.getShow().getStartTime());
        dto.setSeats(booking.getSeats().stream()
                .map(seat -> seat.getRowName() + seat.getSeatNumber())
                .collect(Collectors.toList()));
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStatus(booking.getStatus());
        dto.setBookingTime(booking.getBookingTime());

        if (booking.getPayment() != null) {
            dto.setPayment(PaymentResponseDto.fromPayment(booking.getPayment()));
        }

        return dto;
    }
}

