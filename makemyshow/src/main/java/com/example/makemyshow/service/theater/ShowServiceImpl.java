package com.example.makemyshow.service.theater;

import com.example.makemyshow.dto.request.ShowRequestDto;
import com.example.makemyshow.dto.response.MovieResponseDto;
import com.example.makemyshow.dto.response.ShowResponseDto;
import com.example.makemyshow.dto.response.TheaterResponseDto;
import com.example.makemyshow.exception.ResourceNotFoundException;
import com.example.makemyshow.exception.UnauthorizedException;
import com.example.makemyshow.exception.ValidationException;
import com.example.makemyshow.model.*;
import com.example.makemyshow.model.user.User;
import com.example.makemyshow.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShowServiceImpl implements ShowService {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private SeatRepository seatRepository;



    @Override
    public List<ShowResponseDto> searchShows(Long movieId, LocalDate date, String city) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Show> shows;

        if (city != null && !city.isEmpty()) {
            shows = showRepository.findByMovieIdAndStartTimeBetweenAndScreenTheaterCityAndIsActiveTrue(
                    movieId, startOfDay, endOfDay, city);
        } else {
            shows = showRepository.findByMovieIdAndStartTimeBetweenAndIsActiveTrue(
                    movieId, startOfDay, endOfDay);
        }

        return shows.stream()
                .map(ShowResponseDto::fromShow)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public ShowResponseDto addShow(ShowRequestDto showRequest, String ownerEmail) {
        // Fetch user
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fetch screen
        Screen screen = screenRepository.findById(showRequest.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));

        // Verify ownership
        if (!screen.getTheater().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to add shows to this screen");
        }

        // Fetch movie
        Movie movie = movieRepository.findById(showRequest.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        // Validate movie is active
        if (!movie.isActive()) {
            throw new ValidationException("Cannot create show for inactive movie");
        }

        // Calculate end time
        LocalDateTime endTime = showRequest.getStartTime()
                .plusMinutes(movie.getDurationMinutes() + 30); // 30 min buffer for ads/trailers

        // Check if show time is in the past
        if (showRequest.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Show time cannot be in the past");
        }

        // Check for overlapping shows
        List<Show> overlappingShows = showRepository.findByScreenIdAndStartTimeBetweenOrEndTimeBetween(
                screen.getId(),
                showRequest.getStartTime().minusMinutes(30), // Buffer before
                endTime.plusMinutes(30), // Buffer after
                showRequest.getStartTime().minusMinutes(30),
                endTime.plusMinutes(30)
        );

        if (!overlappingShows.isEmpty()) {
            throw new ValidationException("Show time overlaps with existing shows");
        }

        // Create show
        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setStartTime(showRequest.getStartTime());
        show.setEndTime(endTime);
        show.setActive(true);

        // Set seat prices - Important: Fetch seat types separately to avoid concurrent modification
        Map<Seat.SeatType, Double> seatPrices = new HashMap<>();
        try {
            for (Map.Entry<String, Double> entry : showRequest.getSeatPrices().entrySet()) {
                Seat.SeatType seatType = Seat.SeatType.valueOf(entry.getKey());
                seatPrices.put(seatType, entry.getValue());
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid seat type provided");
        }

        // Instead of using screen.getSeats() which might cause concurrent modification,
        // query the database directly for seat types
        List<Seat> seats = seatRepository.findByScreenId(screen.getId());
        Set<Seat.SeatType> screenSeatTypes = seats.stream()
                .map(Seat::getSeatType)
                .collect(Collectors.toSet());

        for (Seat.SeatType seatType : screenSeatTypes) {
            if (!seatPrices.containsKey(seatType)) {
                throw new ValidationException("Price not defined for seat type: " + seatType);
            }
        }

        show.setSeatPrices(seatPrices);
        Show savedShow = showRepository.save(show);

        // Reload the show to get a fresh entity with all associations
        Show refreshedShow = showRepository.findById(savedShow.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        return createShowResponseDto(refreshedShow);
    }

    // Helper method to create show response DTO safely
    private ShowResponseDto createShowResponseDto(Show show) {
        ShowResponseDto dto = new ShowResponseDto();
        dto.setId(show.getId());

        // Set movie details
        MovieResponseDto movieDto = MovieResponseDto.fromMovie(show.getMovie());
        dto.setMovie(movieDto);

        // Set theater details
        TheaterResponseDto theaterDto = new TheaterResponseDto();
        theaterDto.setId(show.getScreen().getTheater().getId());
        theaterDto.setName(show.getScreen().getTheater().getName());
        theaterDto.setAddress(show.getScreen().getTheater().getAddress());
        theaterDto.setCity(show.getScreen().getTheater().getCity());
        theaterDto.setState(show.getScreen().getTheater().getState());
        theaterDto.setPincode(show.getScreen().getTheater().getPincode());
        theaterDto.setActive(show.getScreen().getTheater().isActive());
        dto.setTheater(theaterDto);

        dto.setScreenName(show.getScreen().getName());
        dto.setStartTime(show.getStartTime());
        dto.setEndTime(show.getEndTime());
        dto.setActive(show.isActive());

        // Convert seat prices safely
        Map<String, Double> seatPricesDto = new HashMap<>();
        for (Map.Entry<Seat.SeatType, Double> entry : show.getSeatPrices().entrySet()) {
            seatPricesDto.put(entry.getKey().name(), entry.getValue());
        }
        dto.setSeatPrices(seatPricesDto);

        return dto;
    }

    @Override
    @Transactional
    public ShowResponseDto updateShow(Long id, ShowRequestDto showRequest, String ownerEmail) {
        // Fetch user
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fetch show
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // Verify ownership
        if (!show.getScreen().getTheater().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to update this show");
        }

        // Check if show has any bookings
        if (!show.getBookings().isEmpty()) {
            throw new ValidationException("Cannot update show with existing bookings");
        }

        // Cannot change the screen or movie for an existing show
        if (!show.getScreen().getId().equals(showRequest.getScreenId())) {
            throw new ValidationException("Cannot change screen for an existing show");
        }

        Movie movie = movieRepository.findById(showRequest.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        if (!show.getMovie().getId().equals(movie.getId())) {
            throw new ValidationException("Cannot change movie for an existing show");
        }

        // Calculate end time
        LocalDateTime endTime = showRequest.getStartTime()
                .plusMinutes(movie.getDurationMinutes() + 30);

        // Check if show time is in the past
        if (showRequest.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Show time cannot be in the past");
        }

        // Check for overlapping shows (excluding this show)
        List<Show> overlappingShows = showRepository.findByScreenIdAndStartTimeBetweenOrEndTimeBetweenAndIdNot(
                show.getScreen().getId(),
                showRequest.getStartTime().minusMinutes(30),
                endTime.plusMinutes(30),
                showRequest.getStartTime().minusMinutes(30),
                endTime.plusMinutes(30),
                id
        );

        if (!overlappingShows.isEmpty()) {
            throw new ValidationException("Show time overlaps with existing shows");
        }

        // Update show details
        show.setStartTime(showRequest.getStartTime());
        show.setEndTime(endTime);

        // Update seat prices
        Map<Seat.SeatType, Double> seatPrices = new HashMap<>();
        try {
            for (Map.Entry<String, Double> entry : showRequest.getSeatPrices().entrySet()) {
                Seat.SeatType seatType = Seat.SeatType.valueOf(entry.getKey());
                seatPrices.put(seatType, entry.getValue());
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid seat type provided");
        }

        // Verify all seat types have prices
        Set<Seat.SeatType> screenSeatTypes = show.getScreen().getSeats().stream()
                .map(Seat::getSeatType)
                .collect(Collectors.toSet());

        for (Seat.SeatType seatType : screenSeatTypes) {
            if (!seatPrices.containsKey(seatType)) {
                throw new ValidationException("Price not defined for seat type: " + seatType);
            }
        }

        show.setSeatPrices(seatPrices);
        Show updatedShow = showRepository.save(show);

        return ShowResponseDto.fromShow(updatedShow);
    }

    @Override
    @Transactional
    public void deleteShow(Long id, String ownerEmail) {
        // Fetch user
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fetch show
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // Verify ownership
        if (!show.getScreen().getTheater().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this show");
        }

        // Check if show has any confirmed bookings
        List<Booking> bookings = bookingRepository.findByShowId(id);

        // Check if show has any confirmed bookings
        boolean hasConfirmedBookings = bookings.stream()
                .anyMatch(booking -> booking.getStatus() == Booking.BookingStatus.CONFIRMED);

        if (hasConfirmedBookings) {
            // Soft delete - set isActive to false
            show.setActive(false);
            showRepository.save(show);
        } else {
            // If all bookings are cancelled or no bookings exist, perform hard delete
            // First cancel any pending bookings
            for (Booking booking : bookings) {
                if (booking.getStatus() == Booking.BookingStatus.PENDING) {
                    booking.setStatus(Booking.BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                }
            }
            showRepository.delete(show);
        }
    }

    @Override
    public List<ShowResponseDto> getShowsByTheaterAndDate(Long theaterId, LocalDate date, String ownerEmail) {
        // Fetch user
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fetch theater
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found"));

        // Verify ownership
        if (!theater.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to view shows for this theater");
        }

        // Calculate start and end of day
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Get all screens for the theater
        List<Screen> screens = screenRepository.findByTheaterId(theaterId);
        List<Long> screenIds = screens.stream().map(Screen::getId).collect(Collectors.toList());

        // Get shows for these screens on the given date
        List<Show> shows = showRepository.findByScreenIdInAndStartTimeBetweenOrderByStartTime(
                screenIds, startOfDay, endOfDay);

        return shows.stream()
                .map(ShowResponseDto::fromShow)
                .collect(Collectors.toList());
    }

    @Override
    public ShowResponseDto getShowByIdForOwner(Long id, String ownerEmail) {
        // Fetch user
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fetch show
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // Verify ownership
        if (!show.getScreen().getTheater().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to view this show");
        }

        return ShowResponseDto.fromShow(show);
    }
}