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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    @PersistenceContext
    private EntityManager entityManager;

    // Then update the createBooking method to use our new method:
// Replace the booking creation and save code with:


    private static final String SEAT_LOCK_KEY_PREFIX = "SEAT_LOCK:";
    private static final long LOCK_TIMEOUT = 10; // 10 minutes

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingRequest, String email) {
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

            // Get selected seats BUT without loading their relationships
            List<Seat> selectedSeats = new ArrayList<>();
            for (Long seatId : bookingRequest.getSeatIds()) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatId));

                // Validate seats belong to the correct screen
                if (!seat.getScreen().getId().equals(show.getScreen().getId())) {
                    throw new ValidationException("Seat doesn't belong to the show's screen: " + seatId);
                }
                selectedSeats.add(seat);
            }

            // Check for seat availability using a direct query
            List<Long> bookedSeatIds = seatRepository.findBookedSeatIdsByShowId(show.getId());

            // Find seats that are already booked
            List<Long> unavailableSeats = bookingRequest.getSeatIds().stream()
                    .filter(bookedSeatIds::contains)
                    .collect(Collectors.toList());

            if (!unavailableSeats.isEmpty()) {
                throw new ValidationException("The following seats are not available: " + unavailableSeats);
            }

            // Use Redis for temporary locking
            String lockKey = SEAT_LOCK_KEY_PREFIX + show.getId();
            Set<Long> lockedSeatIds = cacheService.get(lockKey, HashSet.class);

            if (lockedSeatIds == null) {
                lockedSeatIds = new HashSet<>();
            }

            // Check for seats that are being processed by other users
            List<Long> lockedSelectedSeats = bookingRequest.getSeatIds().stream()
                    .filter(lockedSeatIds::contains)
                    .collect(Collectors.toList());

            if (!lockedSelectedSeats.isEmpty()) {
                throw new ValidationException("The following seats are currently being booked by another user: " + lockedSelectedSeats);
            }

            // Lock the seats temporarily
            Set<Long> updatedLockedSeats = new HashSet<>(lockedSeatIds);
            updatedLockedSeats.addAll(bookingRequest.getSeatIds());
            cacheService.put(lockKey, updatedLockedSeats, LOCK_TIMEOUT, TimeUnit.MINUTES);

            try {
                // Calculate total amount
                double totalAmount = calculateTotalAmount(show, selectedSeats);

                // Create booking
                // With this:
                Booking savedBooking = saveBookingWithSeats(
                        user.getId(),
                        show.getId(),
                        bookingRequest.getSeatIds(),
                        totalAmount,
                        Booking.BookingStatus.PENDING
                );


                // Construct response DTO
                BookingResponseDto dto = new BookingResponseDto();
                dto.setId(savedBooking.getId());
                dto.setBookingNumber(savedBooking.getBookingNumber());
                dto.setShowId(savedBooking.getShow().getId());
                dto.setMovieTitle(savedBooking.getShow().getMovie().getTitle());
                dto.setTheaterName(savedBooking.getShow().getScreen().getTheater().getName());
                dto.setScreenName(savedBooking.getShow().getScreen().getName());
                dto.setShowTime(savedBooking.getShow().getStartTime());
                dto.setTotalAmount(savedBooking.getTotalAmount());
                dto.setStatus(savedBooking.getStatus());
                dto.setBookingTime(savedBooking.getBookingTime());

                // Get seat labels directly
                List<String> seatLabels = selectedSeats.stream()
                        .map(seat -> seat.getRowName() + seat.getSeatNumber())
                        .collect(Collectors.toList());
                dto.setSeats(seatLabels);

                return dto;
            } catch (Exception e) {
                // If anything fails, release the seat locks
                updatedLockedSeats.removeAll(bookingRequest.getSeatIds());
                cacheService.put(lockKey, updatedLockedSeats, LOCK_TIMEOUT, TimeUnit.MINUTES);
                throw e;
            }
        } catch (Exception e) {
            // Log the error
            System.err.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            // Re-throw the exception to be handled by the controller
            throw e;
        }
    }

    @Transactional
    public Booking saveBookingWithSeats(Long userId, Long showId, List<Long> seatIds, Double totalAmount, Booking.BookingStatus status) {
        // Use direct JPA queries to avoid circular reference issues
        Booking booking = new Booking();
        booking.setBookingNumber(generateBookingNumber());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(status);
        booking.setBookingTime(LocalDateTime.now());

        // Set references by ID only
        User user = entityManager.getReference(User.class, userId);
        booking.setUser(user);

        Show show = entityManager.getReference(Show.class, showId);
        booking.setShow(show);

        // Save booking first
        entityManager.persist(booking);
        entityManager.flush();

        // Now add seats using a join table operation
        for (Long seatId : seatIds) {
            // Add booking-seat relationship using native query to avoid loading the entire object
            entityManager.createNativeQuery(
                            "INSERT INTO booking_seats (booking_id, seat_id) VALUES (?, ?)")
                    .setParameter(1, booking.getId())
                    .setParameter(2, seatId)
                    .executeUpdate();
        }

        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getUserBookings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingTimeDesc(user.getId());

        return bookings.stream()
                .map(booking -> {
                    List<String> seatLabels = seatRepository.findSeatDetailsByBookingId(booking.getId());
                    return createBookingResponseDto(booking, seatLabels);
                })
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

        List<String> seatLabels = seatRepository.findSeatDetailsByBookingId(booking.getId());
        return createBookingResponseDto(booking, seatLabels);
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
        String lockKey = SEAT_LOCK_KEY_PREFIX + booking.getShow().getId();
        Set<Long> lockedSeatIds = cacheService.get(lockKey, HashSet.class);

        if (lockedSeatIds != null) {
            List<Long> seatIds = new ArrayList<>();
            for (Seat seat : booking.getSeats()) {
                seatIds.add(seat.getId());
                lockedSeatIds.remove(seat.getId());
            }
            cacheService.put(lockKey, lockedSeatIds, LOCK_TIMEOUT, TimeUnit.MINUTES);
        }

        List<String> seatLabels = seatRepository.findSeatDetailsByBookingId(updatedBooking.getId());
        return createBookingResponseDto(updatedBooking, seatLabels);
    }

    // Helper method to calculate total amount
    private double calculateTotalAmount(Show show, List<Seat> seats) {
        Map<Seat.SeatType, Double> seatPrices = show.getSeatPrices();
        double totalAmount = 0.0;

        for (Seat seat : seats) {
            Double price = seatPrices.get(seat.getSeatType());
            if (price == null) {
                throw new ValidationException("Price not defined for seat type: " + seat.getSeatType());
            }
            totalAmount += price;
        }

        return totalAmount;
    }

    // Helper method to create booking response DTO safely
    private BookingResponseDto createBookingResponseDto(Booking booking, List<String> seatLabels) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setBookingNumber(booking.getBookingNumber());
        dto.setShowId(booking.getShow().getId());
        dto.setMovieTitle(booking.getShow().getMovie().getTitle());
        dto.setTheaterName(booking.getShow().getScreen().getTheater().getName());
        dto.setScreenName(booking.getShow().getScreen().getName());
        dto.setShowTime(booking.getShow().getStartTime());
        dto.setSeats(seatLabels);
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStatus(booking.getStatus());
        dto.setBookingTime(booking.getBookingTime());

        if (booking.getPayment() != null) {
            dto.setPayment(PaymentResponseDto.fromPayment(booking.getPayment()));
        }

        return dto;
    }

    // Generate a unique booking number
    private String generateBookingNumber() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}