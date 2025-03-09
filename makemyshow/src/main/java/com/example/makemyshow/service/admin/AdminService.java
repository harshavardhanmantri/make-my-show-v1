package com.example.makemyshow.service.admin;

=

public interface AdminService {
    MovieResponseDto addMovie(MovieRequestDto movieRequest);
    MovieResponseDto updateMovie(Long id, MovieRequestDto movieRequest);
    void deleteMovie(Long id);
}

