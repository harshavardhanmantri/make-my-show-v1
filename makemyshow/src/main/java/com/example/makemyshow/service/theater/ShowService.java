package com.example.makemyshow.service.theater;

import java.time.LocalDate;
import java.util.List;

public interface ShowService {
    List<ShowResponseDto> searchShows(Long movieId, LocalDate date, String city);
}
