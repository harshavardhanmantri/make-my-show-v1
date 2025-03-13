package com.example.makemyshow.controller.customer;



import com.example.makemyshow.dto.response.MovieResponseDto;
import com.example.makemyshow.service.movie.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/movies")
@Tag(name = "Movies", description = "APIs for browsing movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping()
    @Operation(summary = "Get all movies with pagination")
    public ResponseEntity<Page<MovieResponseDto>> getAllMovies(Pageable pageable) {
        return ResponseEntity.ok(movieService.getAllMovies(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie details by ID")
    public ResponseEntity<MovieResponseDto> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/now-showing")
    @Operation(summary = "Get now showing movies")
    public ResponseEntity<List<MovieResponseDto>> getNowShowingMovies() {
        return ResponseEntity.ok(movieService.getNowShowingMovies());
    }

    @GetMapping("/coming-soon")
    @Operation(summary = "Get upcoming movies")
    public ResponseEntity<List<MovieResponseDto>> getUpcomingMovies() {
        return ResponseEntity.ok(movieService.getUpcomingMovies());
    }
}
