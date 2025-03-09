package com.example.makemyshow.service.theater;

import java.util.List;

public interface TheaterService {
    List<TheaterResponseDto> getTheatersByCity(String city);
    List<String> getAllCities();
}
