package com.example.makemyshow.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class MovieRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    private String language;

    @NotBlank(message = "Genre is required")
    private String genre;

    @NotNull(message = "Duration is required")
    private Integer durationMinutes;

    @NotNull(message = "Release date is required")
    @Future(message = "Release date must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate releaseDate;

    private String posterUrl;

    private String trailerUrl;
}
