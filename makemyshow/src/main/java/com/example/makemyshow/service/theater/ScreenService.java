package com.example.makemyshow.service.theater;


import com.example.makemyshow.dto.request.ScreenRequestDto;
import com.example.makemyshow.dto.response.ScreenResponseDto;

import java.util.List;

public interface ScreenService {
    ScreenResponseDto addScreen(ScreenRequestDto screenRequest, String ownerEmail);
    ScreenResponseDto updateScreen(Long id, ScreenRequestDto screenRequest, String ownerEmail);
    void deleteScreen(Long id, String ownerEmail);
    List<ScreenResponseDto> getScreensByTheater(Long theaterId, String ownerEmail);
    ScreenResponseDto getScreenById(Long id, String ownerEmail);
}
