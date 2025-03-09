package com.example.makemyshow.service.theater;

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
}