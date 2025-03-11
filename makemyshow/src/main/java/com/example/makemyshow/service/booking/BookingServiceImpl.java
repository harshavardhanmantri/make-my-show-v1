package com.example.makemyshow.service.booking;

import com.example.makemyshow.dto.request.BookingRequestDto;
import com.example.makemyshow.dto.response.BookingResponseDto;
import com.example.makemyshow.dto.response.PaymentResponseDto;
import com.example.makemyshow.exception.ResourceNotFoundException;
import com.example.makemyshow.exception.UnauthorizedException;
import com.example.makemyshow.exception.ValidationException;
import com.example.makemyshow.model.Booking;
import com.example.makemyshow.model.Seat;
import com.example.makemyshow.model.Show;
import com.example.makemyshow.model.user.User;
import com.example.makemyshow.repository.BookingRepository;
import com.example.makemyshow.repository.SeatRepository;
import com.example.makemyshow.repository.ShowRepository;
import com.example.makemyshow.repository.UserRepository;
import com.example.makemyshow.service.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
        String showSeatLockKey = SEAT_LOCK_KEY_PREFIX + bookingRequest.getShowId();
        Set<Long> lockedSeatIds = null;

        try {
            // Get user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Get show
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
            Long screenId = show.getScreen().getId();
            if (selectedSeats.stream().anyMatch(seat -> !seat.getScreen().getId().equals(screenId))) {
                throw new ValidationException("One or more seats do not belong to the show's screen");
            }

            // Get already booked seats for this show
            List<Booking> existingBookings = bookingRepository.findByShowIdAndStatusNot(
                    show.getId(), Booking.BookingStatus.CANCELLED);

            // Create a set of already booked seat IDs
            Set<Long> bookedSeatIds = existingBookings.stream()
                    .flatMap(booking -> seatRepository.findSeatsByBookingId(booking.getId()).stream())
                    .map(Seat::getId)
                    .collect(Collectors.toSet());

            // Check if any selected seat is already booked
            Set<Long> unavailableSeats = selectedSeats.stream()
                    .filter(seat -> bookedSeatIds.contains(seat.getId()))
                    .map(Seat::getId)
                    .collect(Collectors.toSet());

            if (!unavailableSeats.isEmpty()) {
                throw new ValidationException("The following seats are not available: " + unavailableSeats);
            }

            // Lock the seats temporarily using Redis
            lockedSeatIds = cacheService.get(showSeatLockKey, ConcurrentHashMap.KeySetView.class);
            if (lockedSeatIds == null) {
                lockedSeatIds = ConcurrentHashMap.newKeySet();
            }

            // Create a local copy of lockedSeatIds for thread-safe operations
            Set<Long> finalLockedSeatIds = new HashSet<>(lockedSeatIds);

            // Check if any selected seat is locked
            Set<Long> lockedSeats = selectedSeats.stream()
                    .filter(seat -> finalLockedSeatIds.contains(seat.getId()))
                    .map(Seat::getId)
                    .collect(Collectors.toSet());

            if (!lockedSeats.isEmpty()) {
                throw new ValidationException("The following seats are being booked by someone else: " + lockedSeats);
            }

            // Lock the seats (thread-safe operation)
            synchronized (this) {
                lockedSeatIds.addAll(selectedSeats.stream().map(Seat::getId).collect(Collectors.toSet()));
                cacheService.put(showSeatLockKey, lockedSeatIds, LOCK_TIMEOUT, TimeUnit.MINUTES);
            }

            // Calculate total amount based on seat types and pricing
            double totalAmount = selectedSeats.stream()
                    .mapToDouble(seat -> {
                        Double price = show.getSeatPrices().get(seat.getSeatType());
                        if (price == null) {
                            throw new ValidationException("Price not defined for seat type: " + seat.getSeatType());
                        }
                        return price;
                    })
                    .sum();

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

            // Create and return response DTO
            return createBookingResponseDto(savedBooking);

        } catch (Exception e) {
            // Release locks if booking fails (thread-safe operation)
            synchronized (this) {
                if (lockedSeatIds != null) {
                    lockedSeatIds.removeAll(bookingRequest.getSeatIds());
                    cacheService.put(showSeatLockKey, lockedSeatIds, LOCK_TIMEOUT, TimeUnit.MINUTES);
                }
            }
            throw e; // Re-throw the exception
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getUserBookings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingTimeDesc(user.getId());

        // Convert each booking to DTO
        return bookings.stream()
                .map(this::createBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Security check - only the booking owner can view it
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to view this booking");
        }

        return createBookingResponseDto(booking);
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

        // Release the locked seats in cache
        String showSeatLockKey = SEAT_LOCK_KEY_PREFIX + booking.getShow().getId();
        Set<Long> lockedSeatIds = cacheService.get(showSeatLockKey, ConcurrentHashMap.KeySetView.class);

        if (lockedSeatIds != null) {
            // Get the seat IDs for this booking
            List<Seat> bookingSeats = seatRepository.findSeatsByBookingId(booking.getId());
            for (Seat seat : bookingSeats) {
                lockedSeatIds.remove(seat.getId());
            }
            cacheService.put(showSeatLockKey, lockedSeatIds, LOCK_TIMEOUT, TimeUnit.MINUTES);
        }

        return createBookingResponseDto(updatedBooking);
    }

    // Helper method to create booking response DTO
    private BookingResponseDto createBookingResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setBookingNumber(booking.getBookingNumber());
        dto.setShowId(booking.getShow().getId());
        dto.setMovieTitle(booking.getShow().getMovie().getTitle());
        dto.setTheaterName(booking.getShow().getScreen().getTheater().getName());
        dto.setScreenName(booking.getShow().getScreen().getName());
        dto.setShowTime(booking.getShow().getStartTime());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStatus(booking.getStatus());
        dto.setBookingTime(booking.getBookingTime());

        // Get seats using dedicated query
        List<Seat> seats = seatRepository.findSeatsByBookingId(booking.getId());
        List<String> seatLabels = seats.stream()
                .map(seat -> seat.getRowName() + seat.getSeatNumber())
                .collect(Collectors.toList());
        dto.setSeats(seatLabels);

        // Set payment if present
        if (booking.getPayment() != null) {
            dto.setPayment(PaymentResponseDto.fromPayment(booking.getPayment()));
        }

        return dto;
    }

    /**
     * Generate a unique booking number
     */
    private String generateBookingNumber() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}