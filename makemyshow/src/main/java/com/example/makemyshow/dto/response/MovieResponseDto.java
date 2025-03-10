package com.example.makemyshow.dto.response;



import com.example.makemyshow.model.Movie;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieResponseDto {
    private Long id;
    private String title;
    private String description;
    private String language;
    private String genre;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String posterUrl;
    private String trailerUrl;
    private boolean isActive;

    public static MovieResponseDto fromMovie(Movie movie) {
        MovieResponseDto dto = new MovieResponseDto();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setLanguage(movie.getLanguage());
        dto.setGenre(movie.getGenre());
        dto.setDurationMinutes(movie.getDurationMinutes());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setActive(movie.isActive());
        return dto;
    }
}