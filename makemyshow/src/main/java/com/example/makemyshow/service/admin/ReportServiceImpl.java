package com.example.makemyshow.service.admin;

import com.example.makemyshow.model.Booking;
import com.example.makemyshow.repository.BookingRepository;
import com.example.makemyshow.repository.MovieRepository;
import com.example.makemyshow.repository.ScreenRepository;
import com.example.makemyshow.repository.TheaterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        return report;setStartDate(startDate);
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
        return null;
    }
}