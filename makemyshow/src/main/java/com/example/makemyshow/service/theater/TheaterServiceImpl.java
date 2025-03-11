package com.example.makemyshow.service.theater;

import com.example.makemyshow.dto.request.TheaterRequestDto;
import com.example.makemyshow.dto.response.TheaterResponseDto;
import com.example.makemyshow.exception.ResourceNotFoundException;
import com.example.makemyshow.exception.UnauthorizedException;
import com.example.makemyshow.model.Theater;
import com.example.makemyshow.model.user.User;
import com.example.makemyshow.model.user.UserRole;
import com.example.makemyshow.repository.TheaterRepository;
import com.example.makemyshow.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TheaterServiceImpl implements TheaterService {

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<TheaterResponseDto> getTheatersByCity(String city) {
        return theaterRepository.findByCityAndIsActiveTrue(city)
                .stream()
                .map(TheaterResponseDto::fromTheater)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCities() {
        return theaterRepository.findDistinctCityByIsActiveTrue();
    }

    @Override
    @Transactional
    public TheaterResponseDto addTheater(TheaterRequestDto theaterRequest, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify that the user has the THEATER_OWNER role
        if (!owner.hasRole(UserRole.ROLE_THEATER_OWNER)) {
            throw new UnauthorizedException("Only theater owners can add theaters");
        }

        Theater theater = new Theater();
        theater.setName(theaterRequest.getName());
        theater.setAddress(theaterRequest.getAddress());
        theater.setCity(theaterRequest.getCity());
        theater.setState(theaterRequest.getState());
        theater.setPincode(theaterRequest.getPincode());
        theater.setContactNumber(theaterRequest.getContactNumber());
        theater.setActive(true);
        theater.setOwner(owner);

        Theater savedTheater = theaterRepository.save(theater);
        return TheaterResponseDto.fromTheater(savedTheater);
    }

    @Override
    @Transactional
    public TheaterResponseDto updateTheater(Long id, TheaterRequestDto theaterRequest, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found"));

        // Verify ownership
        if (!theater.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to update this theater");
        }

        // Update theater details
        theater.setName(theaterRequest.getName());
        theater.setAddress(theaterRequest.getAddress());
        theater.setCity(theaterRequest.getCity());
        theater.setState(theaterRequest.getState());
        theater.setPincode(theaterRequest.getPincode());
        theater.setContactNumber(theaterRequest.getContactNumber());

        Theater updatedTheater = theaterRepository.save(theater);
        return TheaterResponseDto.fromTheater(updatedTheater);
    }

    @Override
    @Transactional
    public void deleteTheater(Long id, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found"));

        // Verify ownership
        if (!theater.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this theater");
        }

        // Check if theater has any screens
        if (!theater.getScreens().isEmpty()) {
            // If screens exist, soft delete by setting isActive to false
            theater.setActive(false);
            theaterRepository.save(theater);
        } else {
            // No screens, perform hard delete
            theaterRepository.delete(theater);
        }
    }

    @Override
    public List<TheaterResponseDto> getTheatersByOwner(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Theater> theaters = theaterRepository.findByOwnerId(owner.getId());
        return theaters.stream()
                .map(TheaterResponseDto::fromTheater)
                .collect(Collectors.toList());
    }

    @Override
    public TheaterResponseDto getTheaterByIdForOwner(Long id, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found"));

        // Verify ownership
        if (!theater.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You don't have permission to view this theater");
        }

        return TheaterResponseDto.fromTheater(theater);
    }
}