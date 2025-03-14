package com.example.makemyshow.repository;

import com.example.makemyshow.model.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {
    @Query("SELECT DISTINCT t.city FROM Theater t WHERE t.isActive = true")
    List<String> findDistinctCityByIsActiveTrue();
    List<Theater> findByCityAndIsActiveTrue(String city);
    List<Theater> findByOwnerId(Long ownerId);
}
