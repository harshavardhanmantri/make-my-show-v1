package com.example.makemyshow.service.admin;

import java.time.LocalDate;


public interface ReportService {
    ReportDto.RevenueReportDto generateRevenueReport(LocalDate startDate, LocalDate endDate, Long theaterId);
    ReportDto.BookingsReportDto generateBookingsReport(LocalDate startDate, LocalDate endDate, Long movieId);
    ReportDto.PopularMoviesReportDto generatePopularMoviesReport(LocalDate startDate, LocalDate endDate);
    ReportDto.TheaterPerformanceReportDto generateTheaterPerformanceReport(LocalDate startDate, LocalDate endDate);
}