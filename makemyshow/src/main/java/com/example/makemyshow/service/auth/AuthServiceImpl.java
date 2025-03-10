package com.example.makemyshow.service.auth;

import com.example.makemyshow.exception.ResourceNotFoundException;
import com.example.makemyshow.exception.UnauthorizedException;
import com.example.makemyshow.repository.RoleRepository;
import com.example.makemyshow.security.JwtTokenProvider;
import com.example.makemyshow.dto.request.LoginRequestDto;
import com.example.makemyshow.dto.request.UserRegistrationDto;
import com.example.makemyshow.dto.response.JwtResponseDto;
import com.example.makemyshow.model.user.Role;
import com.example.makemyshow.model.user.User;
import com.example.makemyshow.model.user.UserRole;
import com.example.makemyshow.repository.UserRepository;
import com.example.makemyshow.service.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private OtpService otpService;

    @Autowired
    private CacheService cacheService;

    @Override
    @Transactional
    public Object registerUser(UserRegistrationDto registrationDto, UserRole userRole) {
        // Check if user exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UnauthorizedException("Email is already in use");
        }

        // Create new user
        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFullName(registrationDto.getFullName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());

        // Set roles
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByName(userRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        roles.add(role);

        // For theater owners, add customer role as well
        if (userRole == UserRole.ROLE_THEATER_OWNER) {
            Role customerRole = roleRepository.findByName(UserRole.ROLE_CUSTOMER)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            roles.add(customerRole);
        }

        user.setRoles(roles);

        // Save user
        User savedUser = userRepository.save(user);

        // Send OTP for email verification
//        otpService.sendOtp(user.getEmail());

        return savedUser;
    }

    @Override
    public JwtResponseDto authenticateUser(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        org.springframework.security.core.userdetails.UserDetails userDetails =
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return JwtResponseDto.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .id(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    public JwtResponseDto refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String newAccessToken = jwtTokenProvider.generateToken(authentication);

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return JwtResponseDto.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken)
                .id(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public void verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isValid = otpService.validateOtp(email, otp);

        if (!isValid) {
            throw new UnauthorizedException("Invalid OTP");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
    }
}

