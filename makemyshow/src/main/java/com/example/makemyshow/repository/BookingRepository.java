package com.example.makemyshow.repository;

import com.example.makemyshow.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);
    Optional<Booking> findByIdAndUserId(Long id, Long userId);
    List<Booking> findByShowIdAndStatusNot(Long showId, Booking.BookingStatus status);
    List<Booking> findByBookingTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, Booking.BookingStatus status);
    List<Booking> findByBookingTimeBetweenAndShowScreenTheaterIdAndStatus(LocalDateTime start, LocalDateTime end, Long theaterId, Booking.BookingStatus status);
    List<Booking> findByBookingTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Booking> findByBookingTimeBetweenAndShowMovieId(LocalDateTime start, LocalDateTime end, Long movieId);
    List<Booking> findByShowId(Long showId);
}
