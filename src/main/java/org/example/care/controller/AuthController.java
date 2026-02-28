package org.example.care.controller;

import java.time.Duration;
import java.util.Objects;
import org.example.care.dto.auth.AuthResponse;
import org.example.care.dto.auth.LoginRequest;
import org.example.care.dto.auth.RegisterRequest;
import org.example.care.model.User;
import org.example.care.security.JwtProperties;
import org.example.care.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@SuppressWarnings("null")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Validated @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        AuthResponse body = AuthResponse.builder()
                .message("User registered successfully")
                .username(user.getUsername())
                .role(user.getRole())
                .build();
        return ResponseEntity.ok(body);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Validated @RequestBody LoginRequest request) {
        AuthService.AuthResult authResult = authService.login(request);

        ResponseCookie jwtCookie = ResponseCookie.from(
                Objects.requireNonNull(jwtProperties.getCookieName()),
                Objects.requireNonNull(authResult.token()))
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofMillis(jwtProperties.getExpirationMs()))
                .build();

        AuthResponse body = AuthResponse.builder()
                .message("Login successful")
                .username(authResult.user().getUsername())
                .role(authResult.user().getRole())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        // Create a cookie with the same name, but maxAge = 0 to delete it
        ResponseCookie deleteCookie = ResponseCookie.from(
                        Objects.requireNonNull(jwtProperties.getCookieName()),
                        "")
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .path("/")
                .sameSite("Strict")
                .maxAge(0) // Expire the cookie immediately
                .build();

        AuthResponse body = AuthResponse.builder()
                .message("Logout successful")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(body);
    }
}
