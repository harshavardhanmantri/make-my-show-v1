package com.example.makemyshow.service.movie;

import com.example.makemyshow.exception.ResourceNotFoundException;
import com.example.makemyshow.model.Movie;
import com.example.makemyshow.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Override
    public Page<MovieResponseDto> getAllMovies(Pageable pageable) {
        return movieRepository.findByIsActiveTrue(pageable)
                .map(MovieResponseDto::fromMovie);
    }

    @Override
    public MovieResponseDto getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
        return MovieResponseDto.fromMovie(movie);
    }

    @Override
    public List<MovieResponseDto> getNowShowingMovies() {
        LocalDate today = LocalDate.now();
        return movieRepository.findByIsActiveTrueAndReleaseDateLessThanEqual(today)
                .stream()
                .map(MovieResponseDto::fromMovie)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieResponseDto> getUpcomingMovies() {
        LocalDate today = LocalDate.now();
        return movieRepository.findByIsActiveTrueAndReleaseDateGreaterThan(today)
                .stream()
                .map(MovieResponseDto::fromMovie)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieResponseDto> searchMovies(String title, String genre, String language) {
        Specification<Movie> spec = Specification.where(null);

        if (title != null && !title.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }

        if (genre != null && !genre.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("genre")), "%" + genre.toLowerCase() + "%"));
        }

        if (language != null && !language.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("language")), language.toLowerCase()));
        }

        spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), true));

        return movieRepository.findAll(spec)
                .stream()
                .map(MovieResponseDto::fromMovie)
                .collect(Collectors.toList());
    }
}
