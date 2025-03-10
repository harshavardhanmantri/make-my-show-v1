package com.example.makemyshow.service.theater;

import com.example.makemyshow.dto.request.TheaterRequestDto;
import com.example.makemyshow.dto.response.TheaterResponseDto;

import java.util.List;

public interface TheaterService {
    List<TheaterResponseDto> getTheatersByCity(String city);
    List<String> getAllCities();


    TheaterResponseDto addTheater(TheaterRequestDto theaterRequest, String ownerEmail);
    TheaterResponseDto updateTheater(Long id, TheaterRequestDto theaterRequest, String ownerEmail);
    void deleteTheater(Long id, String ownerEmail);
    List<TheaterResponseDto> getTheatersByOwner(String ownerEmail);
    TheaterResponseDto getTheaterByIdForOwner(Long id, String ownerEmail);
}
