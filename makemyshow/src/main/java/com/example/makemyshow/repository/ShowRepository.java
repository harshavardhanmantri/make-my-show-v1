package com.example.makemyshow.repository;

import com.example.makemyshow.model.Show;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

        @Query("SELECT s FROM Show s WHERE s.screen.id = :screenId AND " +
                "((s.startTime BETWEEN :startFrom AND :startTo) OR " +
                "(s.endTime BETWEEN :endFrom AND :endTo) OR " +
                "(s.startTime <= :startFrom AND s.endTime >= :endFrom))")
        List<Show> findOverlappingShows(
                @Param("screenId") Long screenId,
                @Param("startFrom") LocalDateTime startFrom,
                @Param("startTo") LocalDateTime startTo,
                @Param("endFrom") LocalDateTime endFrom,
                @Param("endTo") LocalDateTime endTo
        );

        List<Show> findByScreenIdAndStartTimeBetweenOrEndTimeBetween(
                Long screenId,
                LocalDateTime startFrom,
                LocalDateTime startTo,
                LocalDateTime endFrom,
                LocalDateTime endTo);

        List<Show> findByScreenIdAndStartTimeBetweenOrEndTimeBetweenAndIdNot(
                Long screenId,
                LocalDateTime startFrom,
                LocalDateTime startTo,
                LocalDateTime endFrom,
                LocalDateTime endTo,
                Long showId);

        List<Show> findByScreenIdInAndStartTimeBetweenOrderByStartTime(
                List<Long> screenIds,
                LocalDateTime start,
                LocalDateTime end);

        List<Show> findByMovieIdAndStartTimeBetweenAndIsActiveTrue(
                Long movieId,
                LocalDateTime start,
                LocalDateTime end);

        List<Show> findByMovieIdAndStartTimeBetweenAndScreenTheaterCityAndIsActiveTrue(
                Long movieId,
                LocalDateTime start,
                LocalDateTime end,
                String city);
    }

