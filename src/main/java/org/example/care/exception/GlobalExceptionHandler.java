package org.example.care.exception;

import lombok.extern.slf4j.Slf4j; // IMPORT SLF4J
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j // ADD THIS ANNOTATION
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(AccessDeniedException ex) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage()); // LOG RBAC FAILURES
        return buildError(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(BadCredentialsException ex) {
        log.warn("Failed login attempt due to bad credentials"); // LOG LOGIN FAILURES
        return buildError(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found requested: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        log.error("Unexpected internal server error: ", ex); // LOG FULL STACK TRACE FOR BUG FIXING
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred");
    }

    // ... Keep your other existing handlers ...
    private ResponseEntity<Map<String, String>> buildError(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        body.put("status", String.valueOf(status.value()));
        return ResponseEntity.status(status).body(body);
    }
}