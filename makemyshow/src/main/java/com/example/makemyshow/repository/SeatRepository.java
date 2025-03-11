package com.example.makemyshow.repository;

import com.example.makemyshow.model.Seat;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreenId(Long screenId);

    @Query("SELECT s FROM Seat s JOIN s.bookings b WHERE b.id = :bookingId")
    List<Seat> findSeatsByBookingId(@Param("bookingId") Long bookingId);
}
