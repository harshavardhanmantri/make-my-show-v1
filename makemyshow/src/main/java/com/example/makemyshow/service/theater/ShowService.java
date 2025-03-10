package com.example.makemyshow.service.theater;

import com.example.makemyshow.dto.request.ShowRequestDto;
import com.example.makemyshow.dto.response.ShowResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface ShowService {
    List<ShowResponseDto> searchShows(Long movieId, LocalDate date, String city);

    ShowResponseDto addShow(ShowRequestDto showRequest, String ownerEmail);
    ShowResponseDto updateShow(Long id, ShowRequestDto showRequest, String ownerEmail);
    void deleteShow(Long id, String ownerEmail);
    List<ShowResponseDto> getShowsByTheaterAndDate(Long theaterId, LocalDate date, String ownerEmail);
    ShowResponseDto getShowByIdForOwner(Long id, String ownerEmail);
}
