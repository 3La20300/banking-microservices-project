package com.banking.userservice.service;

import com.banking.userservice.dto.UserLoginDto;
import com.banking.userservice.dto.UserRegistrationDto;
import com.banking.userservice.dto.UserResponseDto;
import com.banking.userservice.exception.InvalidCredentialsException;
import com.banking.userservice.exception.UserAlreadyExistsException;
import com.banking.userservice.exception.UserNotFoundException;
import com.banking.userservice.model.User;
import com.banking.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserResponseDto registerUser(UserRegistrationDto userRegistrationDto) {
        // Check if username already exists
        if(userRepository.existsByUsername(userRegistrationDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        // Check if email already exists
        if(userRepository.existsByEmail(userRegistrationDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(userRegistrationDto.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(userRegistrationDto.getPassword()));
        user.setEmail(userRegistrationDto.getEmail());
        user.setFirstName(userRegistrationDto.getFirstName());
        user.setLastName(userRegistrationDto.getLastName());

        // Save user to database
        User savedUser = userRepository.save(user);

        // Return success response
        return new UserResponseDto(savedUser.getId(), savedUser.getUsername(), "User registered successfully.");
    }

    public UserResponseDto loginUser(UserLoginDto userLoginDto) {
        // Find user by username
        User user = userRepository.findByUsername(userLoginDto.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        // Verify password
        if(!bCryptPasswordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Return success response
        return new UserResponseDto(user.getId(), user.getUsername());
    }

    public UserResponseDto getUserById(UUID userId) {
        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        // Return user profile response
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
