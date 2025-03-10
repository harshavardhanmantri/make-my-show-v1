package com.example.makemyshow.service.booking;
import com.example.makemyshow.dto.request.BookingRequestDto;
import com.example.makemyshow.dto.response.BookingResponseDto;
import com.example.makemyshow.exception.ResourceNotFoundException;
import com.example.makemyshow.exception.UnauthorizedException;
import com.example.makemyshow.model.Booking;
import com.example.makemyshow.model.Seat;
import com.example.makemyshow.model.Show;
import com.example.makemyshow.model.user.User;
import com.example.makemyshow.repository.BookingRepository;
import com.example.makemyshow.repository.SeatRepository;
import com.example.makemyshow.repository.ShowRepository;
import com.example.makemyshow.repository.UserRepository;
import com.example.makemyshow.service.cache.CacheService;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private CacheService cacheService;

    private static final String SEAT_LOCK_KEY_PREFIX = "SEAT_LOCK:";
    private static final long LOCK_TIMEOUT = 10; // 10 minutes

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingRequest, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Show show = showRepository.findById(bookingRequest.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // Check if show time is in the past
        if (show.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Cannot book tickets for past shows");
        }

        // Get selected seats
        List<Seat> selectedSeats = seatRepository.findAllById(bookingRequest.getSeatIds());
        if (selectedSeats.size() != bookingRequest.getSeatIds().size()) {
            throw new ResourceNotFoundException("One or more seats not found");
        }

        // Validate seats belong to the correct screen
        if (selectedSeats.stream().anyMatch(seat -> !seat.getScreen().getId().equals(show.getScreen().getId()))) {
            throw new ValidationException("One or more seats do not belong to the show's screen");
        }

        // Check for seat availability using optimistic locking
        String showSeatLockKey = SEAT_LOCK_KEY_PREFIX + show.getId();
        Set<Long> lockedSeatIds = cacheService.get(showSeatLockKey, HashSet.class);

        if (lockedSeatIds == null) {
            lockedSeatIds = new HashSet<>();
        }

        // Get already booked seats for this show
        List<Booking> existingBookings = bookingRepository.findByShowIdAndStatusNot(
                show.getId(), Booking.BookingStatus.CANCELLED);

        Set<Long> bookedSeatIds = existingBookings.stream()
                .flatMap(booking -> booking.getSeats().stream())
                .map(Seat::getId)
                .collect(Collectors.toSet());

        // Check if any selected seat is already booked or locked
        Set<Long> unavailableSeats = new HashSet<>();
        for (Seat seat : selectedSeats) {
            if (bookedSeatIds.contains(seat.getId()) || lockedSeatIds.contains(seat.getId())) {
                unavailableSeats.add(seat.getId());
            }
        }

        if (!unavailableSeats.isEmpty()) {
            throw new ValidationException("The following seats are not available: " + unavailableSeats);
        }

        // Lock the seats temporarily
        for (Seat seat : selectedSeats) {
            lockedSeatIds.add(seat.getId());
        }
        cacheService.put(showSeatLockKey, lockedSeatIds, LOCK_TIMEOUT, TimeUnit.MINUTES);

        // Calculate total amount
        double totalAmount = calculateTotalAmount(show, selectedSeats);

        // Create booking
        Booking booking = new Booking();
        booking.setBookingNumber(generateBookingNumber());
        booking.setUser(user);
        booking.setShow(show);
        booking.setSeats(new HashSet<>(selectedSeats));
        booking.setTotalAmount(totalAmount);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setBookingTime(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        return BookingResponseDto.fromBooking(savedBooking);
    }

    @Override
    @Transactional
    public List<BookingResponseDto> getUserBookings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingTimeDesc(user.getId());

        return bookings.stream()
                .map(BookingResponseDto::fromBooking)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponseDto getBookingById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Security check - only the booking owner can view it
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to view this booking");
        }

        return BookingResponseDto.fromBooking(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto cancelBooking(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Security check - only the booking owner can cancel it
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to cancel this booking");
        }

        // Check if booking is already cancelled
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new ValidationException("Booking is already cancelled");
        }

        // Check if show time is in the past
        if (booking.getShow().getStartTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Cannot cancel booking for past shows");
        }

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);

        // Release the locked seats
        String showSeatLockKey = SEAT_LOCK_KEY_PREFIX + booking.getShow().getId();
        Set<Long> lockedSeatIds = cacheService.get(showSeatLockKey, HashSet.class);

        if (lockedSeatIds != null) {
            booking.getSeats().forEach(seat -> lockedSeatIds.remove(seat.getId()));
            cacheService.put(showSeatLockKey, lockedSeatIds, LOCK_TIMEOUT, TimeUnit.MINUTES);
        }

        return BookingResponseDto.fromBooking(updatedBooking);
    }

    /**
     * Calculate total amount for booking based on seat types and pricing
     */
    private double calculateTotalAmount(Show show, List<Seat> seats) {
        double totalAmount = 0.0;
        Map<Seat.SeatType, Double> seatPrices = show.getSeatPrices();

        for (Seat seat : seats) {
            Double price = seatPrices.get(seat.getSeatType());
            if (price == null) {
                throw new ValidationException("Price not defined for seat type: " + seat.getSeatType());
            }
            totalAmount += price;
        }

        return totalAmount;
    }

    /**
     * Generate a unique booking number
     */
    private String generateBookingNumber() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
