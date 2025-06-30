package com.example.moviewatchlist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @SuppressWarnings("null")
    @Test
    void handleRuntimeException_returnsInternalServerError() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RuntimeException ex = new RuntimeException("fail");
        ResponseEntity<?> response = handler.handleRuntimeException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("fail"));
    }

    @SuppressWarnings("null")
    @Test
    void handleIllegalArgumentException_returnsBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        IllegalArgumentException ex = new IllegalArgumentException("bad arg");
        ResponseEntity<?> response = handler.handleIllegalArgumentException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("bad arg"));
    }

    @SuppressWarnings("null")
    @Test
    void handleGenericException_returnsInternalServerErrorWithGenericMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception ex = new Exception("should not be exposed");
        ResponseEntity<?> response = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("unexpected error"));
    }
}
