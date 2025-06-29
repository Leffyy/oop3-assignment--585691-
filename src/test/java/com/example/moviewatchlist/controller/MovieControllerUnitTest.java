package com.example.moviewatchlist.controller;

import com.example.moviewatchlist.dto.MovieResponse;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for MovieController using JUnit and Mockito.
 * No Spring context or MockMvc is used.
 */
class MovieControllerUnitTest {

    @Test
    void testAddMovieWithEmptyTitle() {
        MovieService mockService = mock(MovieService.class);
        MovieController controller = new MovieController(mockService);

        Map<String, String> request = Map.of("title", "");
        CompletableFuture<ResponseEntity<?>> responseFuture = controller.addMovie(request);
        ResponseEntity<?> response = responseFuture.join();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body.toString().contains("Movie title is required"));
    }

    @Test
    void testAddMovieSuccess() {
        MovieService mockService = mock(MovieService.class);
        MovieController controller = new MovieController(mockService);

        Movie mockMovie = new Movie("Inception", "2010", "Christopher Nolan", "Sci-Fi");
        mockMovie.setId(1L);

        when(mockService.addMovieByTitle("Inception"))
            .thenReturn(CompletableFuture.completedFuture(mockMovie));

        Map<String, String> request = Map.of("title", "Inception");
        CompletableFuture<ResponseEntity<?>> responseFuture = controller.addMovie(request);
        ResponseEntity<?> response = responseFuture.join();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body instanceof MovieResponse);
        assertEquals("Inception", ((MovieResponse) body).getTitle());
    }
}