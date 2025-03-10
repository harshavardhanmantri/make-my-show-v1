package com.example.makemyshow.service.admin;

import com.example.makemyshow.dto.request.MovieRequestDto;
import com.example.makemyshow.dto.response.MovieResponseDto;
import com.example.makemyshow.exception.ResourceNotFoundException;
import com.example.makemyshow.model.Movie;
import com.example.makemyshow.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private MovieRepository movieRepository;

    @Override
    @Transactional
    public MovieResponseDto addMovie(MovieRequestDto movieRequest) {
        Movie movie = new Movie();
        movie.setTitle(movieRequest.getTitle());
        movie.setDescription(movieRequest.getDescription());
        movie.setLanguage(movieRequest.getLanguage());
        movie.setGenre(movieRequest.getGenre());
        movie.setDurationMinutes(movieRequest.getDurationMinutes());
        movie.setReleaseDate(movieRequest.getReleaseDate());
        movie.setPosterUrl(movieRequest.getPosterUrl());
        movie.setTrailerUrl(movieRequest.getTrailerUrl());
        movie.setActive(true);

        Movie savedMovie = movieRepository.save(movie);
        return MovieResponseDto.fromMovie(savedMovie);
    }

    @Override
    @Transactional
    public MovieResponseDto updateMovie(Long id, MovieRequestDto movieRequest) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        movie.setTitle(movieRequest.getTitle());
        movie.setDescription(movieRequest.getDescription());
        movie.setLanguage(movieRequest.getLanguage());
        movie.setGenre(movieRequest.getGenre());
        movie.setDurationMinutes(movieRequest.getDurationMinutes());
        movie.setReleaseDate(movieRequest.getReleaseDate());
        movie.setPosterUrl(movieRequest.getPosterUrl());
        movie.setTrailerUrl(movieRequest.getTrailerUrl());

        Movie updatedMovie = movieRepository.save(movie);
        return MovieResponseDto.fromMovie(updatedMovie);
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        // Soft delete - set isActive to false
        movie.setActive(false);
        movieRepository.save(movie);
    }
}

