package com.example.makemyshow.service.admin;

import com.example.makemyshow.dto.request.MovieRequestDto;
import com.example.makemyshow.dto.response.MovieResponseDto;

public interface AdminService {
    MovieResponseDto addMovie(MovieRequestDto movieRequest);
    MovieResponseDto updateMovie(Long id, MovieRequestDto movieRequest);
    void deleteMovie(Long id);
}

