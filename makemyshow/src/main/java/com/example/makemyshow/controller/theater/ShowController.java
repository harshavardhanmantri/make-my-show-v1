package com.example.makemyshow.controller.theater;


import com.example.makemyshow.dto.request.ShowRequestDto;
import com.example.makemyshow.dto.response.ShowResponseDto;
import com.example.makemyshow.service.theater.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/theater/shows")
@PreAuthorize("hasRole('THEATER_OWNER')")
@Tag(name = "Show Management", description = "APIs for theater owners to manage shows")
public class ShowController {

    @Autowired
    private ShowService showService;

    @PostMapping
    @Operation(summary = "Add a new show")
    public ResponseEntity<ShowResponseDto> addShow(
            @Valid @RequestBody ShowRequestDto showRequest,
            Principal principal) {
        return ResponseEntity.ok(showService.addShow(showRequest, principal.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update show details")
    public ResponseEntity<ShowResponseDto> updateShow(
            @PathVariable Long id,
            @Valid @RequestBody ShowRequestDto showRequest,
            Principal principal) {
        return ResponseEntity.ok(showService.updateShow(id, showRequest, principal.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a show")
    public ResponseEntity<?> deleteShow(@PathVariable Long id, Principal principal) {
        showService.deleteShow(id, principal.getName());
        return ResponseEntity.ok().body("Show deleted successfully");
    }

    @GetMapping("/theater/{theaterId}")
    @Operation(summary = "Get all shows for a theater")
    public ResponseEntity<List<ShowResponseDto>> getShowsByTheater(
            @PathVariable Long theaterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Principal principal) {
        return ResponseEntity.ok(showService.getShowsByTheaterAndDate(theaterId, date, principal.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get show details by ID")
    public ResponseEntity<ShowResponseDto> getShowById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(showService.getShowByIdForOwner(id, principal.getName()));
    }
}
