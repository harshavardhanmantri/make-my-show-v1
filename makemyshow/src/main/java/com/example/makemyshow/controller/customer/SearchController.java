package com.example.makemyshow.controller.customer;


import com.example.makemyshow.dto.response.MovieResponseDto;
import com.example.makemyshow.dto.response.ShowResponseDto;
import com.example.makemyshow.dto.response.TheaterResponseDto;
import com.example.makemyshow.service.movie.MovieService;
import com.example.makemyshow.service.theater.ShowService;
import com.example.makemyshow.service.theater.TheaterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/search")
@Tag(name = "Search", description = "APIs for searching movies and shows")
public class SearchController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private ShowService showService;

    @Autowired
    private TheaterService theaterService;

    @GetMapping("/movies")
    @Operation(summary = "Search movies by title or genre")
    public ResponseEntity<List<MovieResponseDto>> searchMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(movieService.searchMovies(title, genre, language));
    }

    @GetMapping("/shows")
    @Operation(summary = "Search shows by movie, date, and city")
    public ResponseEntity<List<ShowResponseDto>> searchShows(
            @RequestParam Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String city) {
        return ResponseEntity.ok(showService.searchShows(movieId, date, city));
    }

    @GetMapping("/theaters")
    @Operation(summary = "Search theaters by city")
    public ResponseEntity<List<TheaterResponseDto>> searchTheaters(
            @RequestParam String city) {
        return ResponseEntity.ok(theaterService.getTheatersByCity(city));
    }

    @GetMapping("/cities")
    @Operation(summary = "Get all cities with theaters")
    public ResponseEntity<List<String>> getAllCities() {
        return ResponseEntity.ok(theaterService.getAllCities());
    }
}

