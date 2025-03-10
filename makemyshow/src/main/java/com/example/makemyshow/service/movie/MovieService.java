package com.example.makemyshow.service.movie;

import com.example.makemyshow.dto.response.MovieResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieService {
    Page<MovieResponseDto> getAllMovies(Pageable pageable);
    MovieResponseDto getMovieById(Long id);
    List<MovieResponseDto> getNowShowingMovies();
    List<MovieResponseDto> getUpcomingMovies();
    List<MovieResponseDto> searchMovies(String title, String genre, String language);
    // Add to MovieService interface
    Page<MovieResponseDto> getAllMoviesForAdmin(Pageable pageable);
}
