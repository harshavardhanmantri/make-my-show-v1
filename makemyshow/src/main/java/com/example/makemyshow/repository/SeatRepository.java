package com.example.makemyshow.repository;

import com.example.makemyshow.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;




@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT s FROM Seat s WHERE s.screen.id = :screenId AND s.id NOT IN " +
            "(SELECT bs.id FROM Booking b JOIN b.seats bs JOIN b.show sh " +
            "WHERE sh.id = :showId AND b.status <> 'CANCELLED')")
    List<Seat> findAvailableSeatsByScreenIdAndShowId(
            @Param("screenId") Long screenId,
            @Param("showId") Long showId
    );

    List<Seat> findByScreenId(Long screenId);

    @Query("SELECT DISTINCT s.id FROM Seat s JOIN s.bookings b JOIN b.show sh WHERE sh.id = :showId AND b.status <> 'CANCELLED'")
    List<Long> findBookedSeatIdsByShowId(@Param("showId") Long showId);

    @Query("SELECT CONCAT(s.rowName, s.seatNumber) FROM Seat s JOIN s.bookings b WHERE b.id = :bookingId")
    List<String> findSeatDetailsByBookingId(@Param("bookingId") Long bookingId);
}