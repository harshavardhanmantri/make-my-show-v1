package com.example.makemyshow.repository;

import com.example.makemyshow.model.Booking;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookingTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, Booking.BookingStatus status);
    List<Booking> findByBookingTimeBetweenAndShowScreenTheaterIdAndStatus(LocalDateTime start, LocalDateTime end, Long theaterId, Booking.BookingStatus status);
    List<Booking> findByBookingTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Booking> findByBookingTimeBetweenAndShowMovieId(LocalDateTime start, LocalDateTime end, Long movieId);

    @Query("SELECT s.id FROM Booking b JOIN b.seats s WHERE b.show.id = :showId AND b.status <> 'CANCELLED'")
    Set<Long> findBookedSeatIdsByShowId(@Param("showId") Long showId);

    List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);
    Optional<Booking> findByIdAndUserId(Long id, Long userId);
    List<Booking> findByShowIdAndStatusNot(Long showId, Booking.BookingStatus status);
    List<Booking> findByShowId(Long showId);
}
