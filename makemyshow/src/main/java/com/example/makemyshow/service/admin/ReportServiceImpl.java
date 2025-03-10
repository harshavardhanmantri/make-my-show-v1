package com.example.makemyshow.service.admin;

import com.example.makemyshow.dto.response.ReportDto;
import com.example.makemyshow.model.Booking;
import com.example.makemyshow.repository.BookingRepository;
import com.example.makemyshow.repository.MovieRepository;
import com.example.makemyshow.repository.ScreenRepository;
import com.example.makemyshow.repository.TheaterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.makemyshow.dto.response.ReportDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Override
    public ReportDto.RevenueReportDto generateRevenueReport(LocalDate startDate, LocalDate endDate, Long theaterId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<Booking> bookings;
        if (theaterId != null) {
            bookings = bookingRepository.findByBookingTimeBetweenAndShowScreenTheaterIdAndStatus(
                    startDateTime, endDateTime, theaterId, Booking.BookingStatus.CONFIRMED);
        } else {
            bookings = bookingRepository.findByBookingTimeBetweenAndStatus(
                    startDateTime, endDateTime, Booking.BookingStatus.CONFIRMED);
        }

        ReportDto.RevenueReportDto report = new ReportDto.RevenueReportDto();
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Calculate total revenue
        Double totalRevenue = bookings.stream()
                .mapToDouble(Booking::getTotalAmount)
                .sum();
        report.setTotalRevenue(totalRevenue);

        // Revenue by day
        Map<String, Double> revenueByDay = bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getBookingTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        Collectors.summingDouble(Booking::getTotalAmount)
                ));
        report.setRevenueByDay(revenueByDay);

        // Revenue by movie
        Map<String, Double> revenueByMovie = bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getShow().getMovie().getTitle(),
                        Collectors.summingDouble(Booking::getTotalAmount)
                ));
        report.setRevenueByMovie(revenueByMovie);

        // Revenue by theater
        Map<String, Double> revenueByTheater = bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getShow().getScreen().getTheater().getName(),
                        Collectors.summingDouble(Booking::getTotalAmount)
                ));
        report.setRevenueByTheater(revenueByTheater);

        return report;
    }

    //Error in this code
    @Override
    public ReportDto.BookingsReportDto generateBookingsReport(LocalDate startDate, LocalDate endDate, Long movieId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<Booking> bookings;
        if (movieId != null) {
            bookings = bookingRepository.findByBookingTimeBetweenAndShowMovieId(startDateTime, endDateTime, movieId);
        } else {
            bookings = bookingRepository.findByBookingTimeBetween(startDateTime, endDateTime);
        }

        ReportDto.BookingsReportDto report = new ReportDto.BookingsReportDto();
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Total bookings
        report.setTotalBookings((long) bookings.size());

        // Bookings by day
        Map<String, Long> bookingsByDay = bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getBookingTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        Collectors.counting()
                ));
        report.setBookingsByDay(bookingsByDay);

        // Bookings by movie
        Map<String, Long> bookingsByMovie = bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getShow().getMovie().getTitle(),
                        Collectors.counting()
                ));
        report.setBookingsByMovie(bookingsByMovie);

        // Bookings by theater
        Map<String, Long> bookingsByTheater = bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getShow().getScreen().getTheater().getName(),
                        Collectors.counting()
                ));
        report.setBookingsByTheater(bookingsByTheater);

        // Booking completion rate
        Map<String, Double> bookingCompletionRate = new HashMap<>();
        for (Map.Entry<String, Long> entry : bookingsByMovie.entrySet()) {
            String movieTitle = entry.getKey();
            Long totalBookings = entry.getValue();

            long confirmedBookings = bookings.stream()
                    .filter(booking -> booking.getShow().getMovie().getTitle().equals(movieTitle))
                    .filter(booking -> booking.getStatus() == Booking.BookingStatus.CONFIRMED)
                    .count();

            double completionRate = totalBookings > 0 ? (double) confirmedBookings / totalBookings * 100 : 0;
            bookingCompletionRate.put(movieTitle, completionRate);
        }
        report.setBookingCompletionRate(bookingCompletionRate);

        return report;
    }



    @Override
    public ReportDto.PopularMoviesReportDto generatePopularMoviesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<Booking> bookings = bookingRepository.findByBookingTimeBetweenAndStatus(
                startDateTime, endDateTime, Booking.BookingStatus.CONFIRMED);

        ReportDto.PopularMoviesReportDto report = new ReportDto.PopularMoviesReportDto();
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Group bookings by movie and calculate statistics
        Map<Long, List<Booking>> bookingsByMovieId = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getShow().getMovie().getId()));

        List<ReportDto.PopularMoviesReportDto.MovieStatsDto> movieStats = new ArrayList<>();

        bookingsByMovieId.forEach((movieId, movieBookings) -> {
            if (!movieBookings.isEmpty()) {
                Booking firstBooking = movieBookings.get(0);
                String movieTitle = firstBooking.getShow().getMovie().getTitle();
                Long totalBookings = (long) movieBookings.size();
                Double totalRevenue = movieBookings.stream()
                        .mapToDouble(Booking::getTotalAmount)
                        .sum();

                // Calculate average occupancy
                double totalSeats = 0;
                double totalBookedSeats = 0;
                for (Booking booking : movieBookings) {
                    int screenCapacity = booking.getShow().getScreen().getCapacity();
                    int bookedSeats = booking.getSeats().size();
                    totalSeats += screenCapacity;
                    totalBookedSeats += bookedSeats;
                }
                Double averageOccupancy = totalSeats > 0 ? (totalBookedSeats / totalSeats) * 100 : 0;

                ReportDto.PopularMoviesReportDto.MovieStatsDto movieStat = new ReportDto.PopularMoviesReportDto.MovieStatsDto();
                movieStat.setMovieId(movieId);
                movieStat.setTitle(movieTitle);
                movieStat.setTotalBookings(totalBookings);
                movieStat.setTotalRevenue(totalRevenue);
                movieStat.setAverageOccupancy(averageOccupancy);

                movieStats.add(movieStat);
            }
        });

        // Sort by total bookings in descending order
        movieStats.sort((a, b) -> b.getTotalBookings().compareTo(a.getTotalBookings()));

        report.setPopularMovies(movieStats);
        return report;
    }

    @Override
    public ReportDto.TheaterPerformanceReportDto generateTheaterPerformanceReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<Booking> bookings = bookingRepository.findByBookingTimeBetweenAndStatus(
                startDateTime, endDateTime, Booking.BookingStatus.CONFIRMED);

        ReportDto.TheaterPerformanceReportDto report = new ReportDto.TheaterPerformanceReportDto();
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Group bookings by theater
        Map<Long, List<Booking>> bookingsByTheaterId = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getShow().getScreen().getTheater().getId()));

        List<ReportDto.TheaterPerformanceReportDto.TheaterStatsDto> theaterStats = new ArrayList<>();

        bookingsByTheaterId.forEach((theaterId, theaterBookings) -> {
            if (!theaterBookings.isEmpty()) {
                Booking firstBooking = theaterBookings.get(0);
                String theaterName = firstBooking.getShow().getScreen().getTheater().getName();
                String theaterCity = firstBooking.getShow().getScreen().getTheater().getCity();

                Long totalBookings = (long) theaterBookings.size();
                Double totalRevenue = theaterBookings.stream()
                        .mapToDouble(Booking::getTotalAmount)
                        .sum();

                // Calculate average occupancy for theater
                double totalSeats = 0;
                double totalBookedSeats = 0;
                for (Booking booking : theaterBookings) {
                    int screenCapacity = booking.getShow().getScreen().getCapacity();
                    int bookedSeats = booking.getSeats().size();
                    totalSeats += screenCapacity;
                    totalBookedSeats += bookedSeats;
                }
                Double averageOccupancy = totalSeats > 0 ? (totalBookedSeats / totalSeats) * 100 : 0;

                // Group by screens
                Map<Long, List<Booking>> bookingsByScreenId = theaterBookings.stream()
                        .collect(Collectors.groupingBy(booking -> booking.getShow().getScreen().getId()));

                List<ReportDto.TheaterPerformanceReportDto.TheaterStatsDto.ScreenStatsDto> screenStats = new ArrayList<>();

                bookingsByScreenId.forEach((screenId, screenBookings) -> {
                    if (!screenBookings.isEmpty()) {
                        String screenName = screenBookings.get(0).getShow().getScreen().getName();
                        Long screenTotalBookings = (long) screenBookings.size();

                        // Calculate screen occupancy
                        double screenTotalSeats = 0;
                        double screenBookedSeats = 0;
                        for (Booking booking : screenBookings) {
                            int capacity = booking.getShow().getScreen().getCapacity();
                            int booked = booking.getSeats().size();
                            screenTotalSeats += capacity;
                            screenBookedSeats += booked;
                        }
                        Double screenAverageOccupancy = screenTotalSeats > 0 ?
                                (screenBookedSeats / screenTotalSeats) * 100 : 0;

                        ReportDto.TheaterPerformanceReportDto.TheaterStatsDto.ScreenStatsDto screenStat =
                                new ReportDto.TheaterPerformanceReportDto.TheaterStatsDto.ScreenStatsDto();
                        screenStat.setScreenId(screenId);
                        screenStat.setName(screenName);
                        screenStat.setTotalBookings(screenTotalBookings);
                        screenStat.setAverageOccupancy(screenAverageOccupancy);

                        screenStats.add(screenStat);
                    }
                });

                ReportDto.TheaterPerformanceReportDto.TheaterStatsDto theaterStat =
                        new ReportDto.TheaterPerformanceReportDto.TheaterStatsDto();
                theaterStat.setTheaterId(theaterId);
                theaterStat.setName(theaterName);
                theaterStat.setCity(theaterCity);
                theaterStat.setTotalBookings(totalBookings);
                theaterStat.setTotalRevenue(totalRevenue);
                theaterStat.setAverageOccupancy(averageOccupancy);
                theaterStat.setScreens(screenStats);

                theaterStats.add(theaterStat);
            }
        });

        // Sort by total revenue in descending order
        theaterStats.sort((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()));

        report.setTheaters(theaterStats);
        return report;
    }
}