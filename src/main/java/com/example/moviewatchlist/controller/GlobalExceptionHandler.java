package com.example.moviewatchlist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Map;

// This class handles all exceptions that happen in our controllers
// Instead of each controller handling its own errors, we centralize it here
@ControllerAdvice
public class GlobalExceptionHandler {
    
    // Handles runtime exceptions (like null pointer errors)
    // Returns a 500 error with the actual error message
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
    
    // Handles cases where someone passes invalid arguments
    // Returns a 400 bad request error with the error message
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
    
    // Catches any other exception we didn't specifically handle above
    // Returns a generic 500 error without exposing internal details
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
    }
}