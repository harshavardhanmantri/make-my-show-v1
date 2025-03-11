package com.example.makemyshow.service.theater;


import com.example.makemyshow.dto.request.ScreenRequestDto;
import com.example.makemyshow.dto.response.ScreenResponseDto;
import com.example.makemyshow.exception.ResourceNotFoundException;
import com.example.makemyshow.exception.UnauthorizedException;
import com.example.makemyshow.exception.ValidationException;
import com.example.makemyshow.model.Screen;
import com.example.makemyshow.model.Seat;
import com.example.makemyshow.model.Theater;
import com.example.makemyshow.model.user.User;
import com.example.makemyshow.repository.ScreenRepository;
import com.example.makemyshow.repository.SeatRepository;
import com.example.makemyshow.repository.TheaterRepository;
import com.example.makemyshow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScreenServiceImpl implements ScreenService {

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;


    @Override
    @Transactional
    public ScreenResponseDto addScreen(ScreenRequestDto screenRequest, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Theater theater = theaterRepository.findById(screenRequest.getTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found"));

        // Verify ownership
        if (!theater.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to add screens to this theater");
        }

        // Create screen
        Screen screen = new Screen();
        screen.setName(screenRequest.getName());
        screen.setTheater(theater);
        screen.setCapacity(screenRequest.getCapacity());
        screen.setActive(true);

        // Save the screen first to get an ID
        Screen savedScreen = screenRepository.save(screen);

        // Create and save seats
        List<Seat> seats = new ArrayList<>();
        for (ScreenRequestDto.SeatConfigDto config : screenRequest.getSeatConfigs()) {
            for (int i = 1; i <= config.getSeatCount(); i++) {
                Seat seat = new Seat();
                seat.setRowName(config.getRowName());
                seat.setSeatNumber(String.valueOf(i));
                try {
                    seat.setSeatType(Seat.SeatType.valueOf(config.getSeatType()));
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Invalid seat type: " + config.getSeatType());
                }
                seat.setScreen(savedScreen);
                seats.add(seat);
            }
        }

        // Save all seats at once
        List<Seat> savedSeats = seatRepository.saveAll(seats);

        // Important: Fetch the screen again with fresh data to avoid concurrent modification
        Screen refreshedScreen = screenRepository.findById(savedScreen.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));

        // Create DTO directly without relying on the screen's seats collection
        return createScreenResponseDto(refreshedScreen, savedSeats);
    }

    // Helper method to create ScreenResponseDto without relying on screen.getSeats()
    private ScreenResponseDto createScreenResponseDto(Screen screen, List<Seat> seats) {
        ScreenResponseDto dto = new ScreenResponseDto();
        dto.setId(screen.getId());
        dto.setName(screen.getName());
        dto.setTheaterId(screen.getTheater().getId());
        dto.setTheaterName(screen.getTheater().getName());
        dto.setCapacity(screen.getCapacity());
        dto.setActive(screen.isActive());

        // Group seats by row manually
        Map<String, List<Seat>> seatsByRow = seats.stream()
                .collect(Collectors.groupingBy(Seat::getRowName));

        List<ScreenResponseDto.SeatRowDto> seatRows = new ArrayList<>();
        for (Map.Entry<String, List<Seat>> entry : seatsByRow.entrySet()) {
            ScreenResponseDto.SeatRowDto rowDto = new ScreenResponseDto.SeatRowDto();
            rowDto.setRowName(entry.getKey());

            List<ScreenResponseDto.SeatDto> seatDtos = entry.getValue().stream()
                    .map(seat -> {
                        ScreenResponseDto.SeatDto seatDto = new ScreenResponseDto.SeatDto();
                        seatDto.setId(seat.getId());
                        seatDto.setSeatNumber(seat.getSeatNumber());
                        seatDto.setSeatType(seat.getSeatType().name());
                        return seatDto;
                    })
                    .collect(Collectors.toList());

            rowDto.setSeats(seatDtos);
            seatRows.add(rowDto);
        }

        dto.setSeatRows(seatRows);

        return dto;
    }

    @Override
    @Transactional
    public ScreenResponseDto updateScreen(Long id, ScreenRequestDto screenRequest, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));

        // Verify ownership
        if (!screen.getTheater().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to update this screen");
        }

        // Cannot change theater for an existing screen
        if (!screen.getTheater().getId().equals(screenRequest.getTheaterId())) {
            throw new UnauthorizedException("Cannot change theater for an existing screen");
        }

        // Update basic info
        screen.setName(screenRequest.getName());
        screen.setCapacity(screenRequest.getCapacity());

        // For simplicity, we're not updating seats here
        // In a real app, you'd need to handle seat updates carefully

        Screen updatedScreen = screenRepository.save(screen);
        return ScreenResponseDto.fromScreen(updatedScreen);
    }

    @Override
    @Transactional
    public void deleteScreen(Long id, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));

        // Verify ownership
        if (!screen.getTheater().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this screen");
        }

        // Check if screen has shows
        if (!screen.getShows().isEmpty()) {
            // Soft delete - set isActive to false
            screen.setActive(false);
            screenRepository.save(screen);
        } else {
            // Hard delete if no shows are associated
            screenRepository.delete(screen);
        }
    }

    @Override
    public List<ScreenResponseDto> getScreensByTheater(Long theaterId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found"));

        // Verify ownership
        if (!theater.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to view screens for this theater");
        }

        List<Screen> screens = screenRepository.findByTheaterId(theaterId);

        // For each screen, fetch its seats separately to avoid concurrent modification
        List<ScreenResponseDto> result = new ArrayList<>();
        for (Screen screen : screens) {
            // Fetch seats for this screen
            List<Seat> seats = seatRepository.findByScreenId(screen.getId());

            // Create DTO manually
            result.add(createScreenResponseDto(screen, seats));
        }

        return result;
    }

    @Override
    public ScreenResponseDto getScreenById(Long id, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));

        // Verify ownership
        if (!screen.getTheater().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to view this screen");
        }

        // Fetch seats separately to avoid concurrent modification
        List<Seat> seats = seatRepository.findByScreenId(screen.getId());

        // Create response DTO using the helper method
        return createScreenResponseDto(screen, seats);
    }
}
