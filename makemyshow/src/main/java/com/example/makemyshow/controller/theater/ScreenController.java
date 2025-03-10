package com.example.makemyshow.controller.theater;


import com.example.makemyshow.dto.request.ScreenRequestDto;
import com.example.makemyshow.dto.response.ScreenResponseDto;
import com.example.makemyshow.service.theater.ScreenService;
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
@RequestMapping("/api/v1/theater/screens")
@PreAuthorize("hasRole('THEATER_OWNER')")
@Tag(name = "Screen Management", description = "APIs for theater owners to manage screens")
public class ScreenController {

    @Autowired
    private ScreenService screenService;

    @PostMapping
    @Operation(summary = "Add a new screen to a theater")
    public ResponseEntity<ScreenResponseDto> addScreen(
            @Valid @RequestBody ScreenRequestDto screenRequest,
            Principal principal) {
        return ResponseEntity.ok(screenService.addScreen(screenRequest, principal.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update screen details")
    public ResponseEntity<ScreenResponseDto> updateScreen(
            @PathVariable Long id,
            @Valid @RequestBody ScreenRequestDto screenRequest,
            Principal principal) {
        return ResponseEntity.ok(screenService.updateScreen(id, screenRequest, principal.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a screen")
    public ResponseEntity<?> deleteScreen(@PathVariable Long id, Principal principal) {
        screenService.deleteScreen(id, principal.getName());
        return ResponseEntity.ok().body("Screen deleted successfully");
    }

    @GetMapping("/theater/{theaterId}")
    @Operation(summary = "Get all screens for a theater")
    public ResponseEntity<List<ScreenResponseDto>> getScreensByTheater(
            @PathVariable Long theaterId,
            Principal principal) {
        return ResponseEntity.ok(screenService.getScreensByTheater(theaterId, principal.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get screen details by ID")
    public ResponseEntity<ScreenResponseDto> getScreenById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(screenService.getScreenById(id, principal.getName()));
    }
}
