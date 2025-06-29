package com.example.moviewatchlist.controller;

import com.example.moviewatchlist.dto.MovieResponse;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for MovieController using JUnit and Mockito.
 * Tests controller logic in isolation without Spring context.
 */
class MovieControllerUnitTest {

    /**
     * Tests that adding a movie with an empty title returns a Bad Request status.
     */
    @Test
    void testAddMovieWithEmptyTitle() {
        // Arrange
        MovieService mockService = mock(MovieService.class);
        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>();
        request.put("title", "");
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        if (body instanceof Map<?, ?> map) {
            assertTrue(map.get("error").toString().contains("Movie title is required"));
        } else {
            fail("Expected error response as a Map");
        }
        verifyNoInteractions(mockService);
    }

    /**
     * Tests that adding a movie with a null title returns a Bad Request status.
     */
    @Test
    void testAddMovieWithNullTitle() {
        // Arrange
        MovieService mockService = mock(MovieService.class);
        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>();
        request.put("title", null);
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        if (body instanceof Map<?, ?> map) {
            assertTrue(map.get("error").toString().contains("Movie title is required"));
        } else {
            fail("Expected error response as a Map");
        }
        verifyNoInteractions(mockService);
    }

    /**
     * Tests that adding a movie with a whitespace-only title returns a Bad Request status.
     */
    @Test
    void testAddMovieWithWhitespaceTitle() {
        // Arrange
        MovieService mockService = mock(MovieService.class);
        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>();
        request.put("title", "   ");
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        if (body instanceof Map<?, ?> map) {
            assertTrue(map.get("error").toString().contains("Movie title is required"));
        } else {
            fail("Expected error response as a Map");
        }
        verifyNoInteractions(mockService);
    }

    /**
     * Tests that adding a movie without a title key returns a Bad Request status.
     */
    @Test
    void testAddMovieWithMissingTitleKey() {
        // Arrange
        MovieService mockService = mock(MovieService.class);
        when(mockService.addMovieByTitle(null))
            .thenReturn(CompletableFuture.failedFuture(
                new IllegalArgumentException("Movie title is required")));
        
        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>(); // No "title" key
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        if (body instanceof Map<?, ?> map) {
            assertTrue(map.get("error").toString().contains("Movie title is required"));
        } else {
            fail("Expected error response as a Map");
        }
    }

    /**
     * Tests that the controller properly handles when the service throws an exception.
     */
    @Test
    void testAddMovieServiceThrowsException() {
        // Arrange
        MovieService mockService = mock(MovieService.class);
        when(mockService.addMovieByTitle("Inception"))
            .thenReturn(CompletableFuture.failedFuture(
                new RuntimeException("Movie already exists")));

        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>();
        request.put("title", "Inception");
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);

        // Wait for async result
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        if (body instanceof Map<?, ?> map) {
            assertTrue(map.get("error").toString().contains("Movie already exists"));
        } else {
            fail("Expected error response as a Map");
        }
        verify(mockService).addMovieByTitle("Inception");
    }

    /**
     * Tests successful movie addition through the controller.
     */
    @Test
    void testAddMovieSuccess() {
        // Arrange
        MovieService mockService = mock(MovieService.class);
        Movie mockMovie = Movie.builder()
            .title("Inception")
            .releaseYear("2010")
            .director("Christopher Nolan")
            .genre("Sci-Fi")
            .id(1L)
            .build();

        when(mockService.addMovieByTitle("Inception"))
            .thenReturn(CompletableFuture.completedFuture(mockMovie));

        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>();
        request.put("title", "Inception");
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);

        // Wait for async result
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body instanceof MovieResponse);
        assertEquals("Inception", ((MovieResponse) body).getTitle());
        verify(mockService).addMovieByTitle("Inception");
    }

    // Utility method to wait for DeferredResult (with timeout)
    private ResponseEntity<?> getResult(DeferredResult<ResponseEntity<?>> result) {
        for (int i = 0; i < 100; i++) {
            Object value = result.getResult();
            if (value != null) return (ResponseEntity<?>) value;
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        }
        fail("DeferredResult did not complete in time");
        return null;
    }
}