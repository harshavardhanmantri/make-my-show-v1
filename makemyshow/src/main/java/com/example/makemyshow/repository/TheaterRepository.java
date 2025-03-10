package com.example.makemyshow.repository;

import com.example.makemyshow.model.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {
    List<Theater> findByCityAndIsActiveTrue(String city);
    List<String> findDistinctCityByIsActiveTrue(); // Adding this method for getAllCities()
}
