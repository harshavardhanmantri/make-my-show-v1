package com.example.makemyshow.controller.admin;



import com.example.makemyshow.dto.response.ReportDto;
import com.example.makemyshow.service.admin.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Admin Reports", description = "APIs for admins to generate reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/revenue")
    @Operation(summary = "Get revenue report")
    public ResponseEntity<ReportDto.RevenueReportDto> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long theaterId) {
        return ResponseEntity.ok(reportService.generateRevenueReport(startDate, endDate, theaterId));
    }

    @GetMapping("/bookings")
    @Operation(summary = "Get bookings report")
    public ResponseEntity<ReportDto.BookingsReportDto> getBookingsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long movieId) {
        return ResponseEntity.ok(reportService.generateBookingsReport(startDate, endDate, movieId));
    }

    @GetMapping("/movies")
    @Operation(summary = "Get popular movies report")
    public ResponseEntity<ReportDto.PopularMoviesReportDto> getPopularMoviesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generatePopularMoviesReport(startDate, endDate));
    }

    @GetMapping("/theaters")
    @Operation(summary = "Get theater performance report")
    public ResponseEntity<ReportDto.TheaterPerformanceReportDto> getTheaterPerformanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateTheaterPerformanceReport(startDate, endDate));
    }
}
