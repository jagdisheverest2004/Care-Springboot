package org.example.care.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Handled for Login: Thrown when credentials don't match
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(BadCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage()));
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        // Log the actual exception locally for debugging
        ex.printStackTrace();
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred");
    }

    private ResponseEntity<Map<String, String>> buildError(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        body.put("status", String.valueOf(status.value()));
        return ResponseEntity.status(status).body(body);
    }
}