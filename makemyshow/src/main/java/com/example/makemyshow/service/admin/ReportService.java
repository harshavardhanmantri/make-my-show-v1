package com.example.makemyshow.service.admin;

import com.example.makemyshow.dto.response.ReportDto;

import java.time.LocalDate;


public interface ReportService {
    ReportDto.RevenueReportDto generateRevenueReport(LocalDate startDate, LocalDate endDate, Long theaterId);
    ReportDto.BookingsReportDto generateBookingsReport(LocalDate startDate, LocalDate endDate, Long movieId);
    ReportDto.PopularMoviesReportDto generatePopularMoviesReport(LocalDate startDate, LocalDate endDate);
    ReportDto.TheaterPerformanceReportDto generateTheaterPerformanceReport(LocalDate startDate, LocalDate endDate);
}