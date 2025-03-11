package com.example.makemyshow.repository;

import com.example.makemyshow.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
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

