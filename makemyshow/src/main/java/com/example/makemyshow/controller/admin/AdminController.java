package com.example.makemyshow.controller.admin;

import com.example.makemyshow.dto.request.MovieRequestDto;
import com.example.makemyshow.dto.response.MovieResponseDto;
import com.example.makemyshow.service.admin.AdminService;
import com.example.makemyshow.service.movie.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/movies")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Movie Management", description = "APIs for admins to manage movies")
public class AdminController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private AdminService adminService;

    @PostMapping
    @Operation(summary = "Add a new movie")
    public ResponseEntity<MovieResponseDto> addMovie(@Valid @RequestBody MovieRequestDto movieRequest) {
        return ResponseEntity.ok(adminService.addMovie(movieRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update movie details")
    public ResponseEntity<MovieResponseDto> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequestDto movieRequest) {
        return ResponseEntity.ok(adminService.updateMovie(id, movieRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a movie")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        adminService.deleteMovie(id);
        return ResponseEntity.ok().body("Movie deleted successfully");
    }

    @GetMapping
    @Operation(summary = "Get all movies with pagination")
    public ResponseEntity<Page<MovieResponseDto>> getAllMovies(Pageable pageable) {
        return ResponseEntity.ok(movieService.getAllMoviesForAdmin(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie details by ID")
    public ResponseEntity<MovieResponseDto> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }
}
