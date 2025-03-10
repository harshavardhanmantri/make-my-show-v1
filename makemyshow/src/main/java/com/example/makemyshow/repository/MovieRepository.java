package com.example.makemyshow.repository;

import com.example.makemyshow.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {
    Page<Movie> findByIsActiveTrue(Pageable pageable);
    List<Movie> findByIsActiveTrueAndReleaseDateLessThanEqual(LocalDate today);
    List<Movie> findByIsActiveTrueAndReleaseDateGreaterThan(LocalDate today);
}
