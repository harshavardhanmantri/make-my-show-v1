package com.example.makemyshow.controller.theater;


import com.example.makemyshow.dto.request.TheaterRequestDto;
import com.example.makemyshow.dto.response.TheaterResponseDto;
import com.example.makemyshow.service.theater.TheaterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/theater/theaters")
@PreAuthorize("hasRole('THEATER_OWNER')")
@Tag(name = "Theater Management", description = "APIs for theater owners to manage theaters")
public class TheaterController {

    @Autowired
    private TheaterService theaterService;

    @PostMapping
    @Operation(summary = "Add a new theater")
    public ResponseEntity<TheaterResponseDto> addTheater(
            @Valid @RequestBody TheaterRequestDto theaterRequest,
            Principal principal) {
        return ResponseEntity.ok(theaterService.addTheater(theaterRequest, principal.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update theater details")
    public ResponseEntity<TheaterResponseDto> updateTheater(
            @PathVariable Long id,
            @Valid @RequestBody TheaterRequestDto theaterRequest,
            Principal principal) {
        return ResponseEntity.ok(theaterService.updateTheater(id, theaterRequest, principal.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a theater")
    public ResponseEntity<?> deleteTheater(@PathVariable Long id, Principal principal) {
        theaterService.deleteTheater(id, principal.getName());
        return ResponseEntity.ok().body("Theater deleted successfully");
    }

    @GetMapping
    @Operation(summary = "Get all theaters owned by the current user")
    public ResponseEntity<List<TheaterResponseDto>> getMyTheaters(Principal principal) {
        return ResponseEntity.ok(theaterService.getTheatersByOwner(principal.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get theater details by ID")
    public ResponseEntity<TheaterResponseDto> getTheaterById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(theaterService.getTheaterByIdForOwner(id, principal.getName()));
    }
}


