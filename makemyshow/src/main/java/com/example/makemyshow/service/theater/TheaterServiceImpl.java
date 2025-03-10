package com.example.makemyshow.service.theater;

import com.example.makemyshow.dto.request.TheaterRequestDto;
import com.example.makemyshow.dto.response.TheaterResponseDto;
import com.example.makemyshow.repository.TheaterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TheaterServiceImpl implements TheaterService {

    @Autowired
    private TheaterRepository theaterRepository;

    @Override
    public List<TheaterResponseDto> getTheatersByCity(String city) {
        return theaterRepository.findByCityAndIsActiveTrue(city)
                .stream()
                .map(TheaterResponseDto::fromTheater)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCities() {
        return theaterRepository.findDistinctCityByIsActiveTrue();
    }

    @Override
    public TheaterResponseDto addTheater(TheaterRequestDto theaterRequest, String ownerEmail) {
        return null;
    }

    @Override
    public TheaterResponseDto updateTheater(Long id, TheaterRequestDto theaterRequest, String ownerEmail) {
        return null;
    }

    @Override
    public void deleteTheater(Long id, String ownerEmail) {

    }

    @Override
    public List<TheaterResponseDto> getTheatersByOwner(String ownerEmail) {
        return List.of();
    }

    @Override
    public TheaterResponseDto getTheaterByIdForOwner(Long id, String ownerEmail) {
        return null;
    }
}