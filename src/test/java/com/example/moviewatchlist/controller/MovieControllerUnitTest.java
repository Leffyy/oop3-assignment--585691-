package com.example.moviewatchlist.controller;

import com.example.moviewatchlist.dto.MovieResponse;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
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
        when(mockService.addMovieByTitle(anyString()))
            .thenReturn(CompletableFuture.failedFuture(
                new IllegalArgumentException("Movie title is required")));
        
        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>();
        request.put("title", "");
        CompletableFuture<ResponseEntity<?>> responseFuture = controller.addMovie(request);
        ResponseEntity<?> response = responseFuture.join();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body.toString().contains("Movie title is required"));
        verify(mockService).addMovieByTitle("");
    }

    /**
     * Tests that adding a movie with a null title returns a Bad Request status.
     */
    @Test
    void testAddMovieWithNullTitle() {
        // Arrange
        MovieService mockService = mock(MovieService.class);
        when(mockService.addMovieByTitle(null))
            .thenReturn(CompletableFuture.failedFuture(
                new IllegalArgumentException("Movie title is required")));
        
        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>();
        request.put("title", null);
        CompletableFuture<ResponseEntity<?>> responseFuture = controller.addMovie(request);
        ResponseEntity<?> response = responseFuture.join();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body.toString().contains("Movie title is required"));
        verify(mockService).addMovieByTitle(null);
    }

    /**
     * Tests that adding a movie with a whitespace-only title returns a Bad Request status.
     */
    @Test
    void testAddMovieWithWhitespaceTitle() {
        // Arrange
        MovieService mockService = mock(MovieService.class);
        when(mockService.addMovieByTitle("   "))
            .thenReturn(CompletableFuture.failedFuture(
                new IllegalArgumentException("Movie title is required")));
        
        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>();
        request.put("title", "   ");
        CompletableFuture<ResponseEntity<?>> responseFuture = controller.addMovie(request);
        ResponseEntity<?> response = responseFuture.join();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body.toString().contains("Movie title is required"));
        verify(mockService).addMovieByTitle("   ");
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
        CompletableFuture<ResponseEntity<?>> responseFuture = controller.addMovie(request);
        ResponseEntity<?> response = responseFuture.join();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body.toString().contains("Movie title is required"));
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
        CompletableFuture<ResponseEntity<?>> responseFuture = controller.addMovie(request);
        ResponseEntity<?> response = responseFuture.join();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body.toString().contains("Movie already exists"));
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
        CompletableFuture<ResponseEntity<?>> responseFuture = controller.addMovie(request);
        ResponseEntity<?> response = responseFuture.join();

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body instanceof MovieResponse);
        assertEquals("Inception", ((MovieResponse) body).getTitle());
        verify(mockService).addMovieByTitle("Inception");
    }
}