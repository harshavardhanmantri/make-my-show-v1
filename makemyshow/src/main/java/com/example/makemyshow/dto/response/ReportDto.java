package com.example.makemyshow.dto.response;



import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class ReportDto {

    @Data
    public static class RevenueReportDto {
        private LocalDate startDate;
        private LocalDate endDate;
        private Double totalRevenue;
        private Map<String, Double> revenueByDay;
        private Map<String, Double> revenueByMovie;
        private Map<String, Double> revenueByTheater;
    }

    @Data
    public static class BookingsReportDto {
        private LocalDate startDate;
        private LocalDate endDate;
        private Long totalBookings;
        private Map<String, Long> bookingsByDay;
        private Map<String, Long> bookingsByMovie;
        private Map<String, Long> bookingsByTheater;
        private Map<String, Double> bookingCompletionRate;
    }

    @Data
    public static class PopularMoviesReportDto {
        private LocalDate startDate;
        private LocalDate endDate;
        private List<MovieStatsDto> popularMovies;

        @Data
        public static class MovieStatsDto {
            private Long movieId;
            private String title;
            private Long totalBookings;
            private Double totalRevenue;
            private Double averageOccupancy;
        }
    }

    @Data
    public static class TheaterPerformanceReportDto {
        private LocalDate startDate;
        private LocalDate endDate;
        private List<TheaterStatsDto> theaters;

        @Data
        public static class TheaterStatsDto {
            private Long theaterId;
            private String name;
            private String city;
            private Long totalBookings;
            private Double totalRevenue;
            private Double averageOccupancy;
            private List<ScreenStatsDto> screens;

            @Data
            public static class ScreenStatsDto {
                private Long screenId;
                private String name;
                private Long totalBookings;
                private Double averageOccupancy;
            }
        }
    }
}
