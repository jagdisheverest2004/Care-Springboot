package org.example.care.service;

import org.example.care.dto.LoginRequest;
import org.example.care.dto.RegisterRequest;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.Role;
import org.example.care.model.User;
import org.example.care.repository.UserRepository;
import org.example.care.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {
        // Use IllegalStateException for conflicts (mapped to 409 in GlobalHandler)
        // or a custom ConflictException.
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered");
        }

        Role role = request.getRole() == null ? Role.PATIENT : request.getRole();

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(role)
                .build();

        return userRepository.save(user);
    }

    public AuthResult login(LoginRequest request) {
        // authenticationManager.authenticate() automatically throws
        // BadCredentialsException if the password or username is wrong.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Use your custom ResourceNotFoundException if the user doesn't exist
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        String token = jwtService.generateToken(user);
        return new AuthResult(user, token);
    }

    public record AuthResult(User user, String token) {
    }
}