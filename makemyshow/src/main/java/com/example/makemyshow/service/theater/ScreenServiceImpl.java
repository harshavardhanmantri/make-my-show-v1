package com.example.makemyshow.service.theater;


import com.example.makemyshow.dto.request.ScreenRequestDto;
import com.example.makemyshow.dto.response.ScreenResponseDto;
import com.example.makemyshow.exception.ResourceNotFoundException;
import com.example.makemyshow.exception.UnauthorizedException;
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
import java.util.List;
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

        Screen savedScreen = screenRepository.save(screen);

        // Create seats based on configurations
        List<Seat> seats = new ArrayList<>();
        for (ScreenRequestDto.SeatConfigDto config : screenRequest.getSeatConfigs()) {
            for (int i = 1; i <= config.getSeatCount(); i++) {
                Seat seat = new Seat();
                seat.setRowName(config.getRowName());
                seat.setSeatNumber(String.valueOf(i));
                seat.setSeatType(Seat.SeatType.valueOf(config.getSeatType()));
                seat.setScreen(savedScreen);
                seats.add(seat);
            }
        }

        seats = seatRepository.saveAll(seats);
        savedScreen.setSeats(new java.util.HashSet<>(seats));

        return ScreenResponseDto.fromScreen(savedScreen);
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
        return screens.stream()
                .map(ScreenResponseDto::fromScreen)
                .collect(Collectors.toList());
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

        return ScreenResponseDto.fromScreen(screen);
    }
}
