package com.example.makemyshow.service.theater;

import com.example.makemyshow.dto.request.ShowRequestDto;
import com.example.makemyshow.dto.response.ShowResponseDto;
import com.example.makemyshow.model.Show;
import com.example.makemyshow.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowServiceImpl implements ShowService {

    @Autowired
    private ShowRepository showRepository;

    @Override
    public List<ShowResponseDto> searchShows(Long movieId, LocalDate date, String city) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Show> shows;

        if (city != null && !city.isEmpty()) {
            shows = showRepository.findByMovieIdAndStartTimeBetweenAndScreenTheaterCityAndIsActiveTrue(
                    movieId, startOfDay, endOfDay, city);
        } else {
            shows = showRepository.findByMovieIdAndStartTimeBetweenAndIsActiveTrue(
                    movieId, startOfDay, endOfDay);
        }

        return shows.stream()
                .map(ShowResponseDto::fromShow)
                .collect(Collectors.toList());
    }

    @Override
    public ShowResponseDto addShow(ShowRequestDto showRequest, String ownerEmail) {
        return null;
    }

    @Override
    public ShowResponseDto updateShow(Long id, ShowRequestDto showRequest, String ownerEmail) {
        return null;
    }

    @Override
    public void deleteShow(Long id, String ownerEmail) {

    }

    @Override
    public List<ShowResponseDto> getShowsByTheaterAndDate(Long theaterId, LocalDate date, String ownerEmail) {
        return List.of();
    }

    @Override
    public ShowResponseDto getShowByIdForOwner(Long id, String ownerEmail) {
        return null;
    }
}