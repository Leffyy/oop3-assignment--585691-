package com.example.moviewatchlist.controller;

import com.example.moviewatchlist.dto.MovieResponse;
import com.example.moviewatchlist.dto.PaginatedResponse;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for movie-related endpoints.
 * Delegates all business logic to MovieService.
 */
@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "*")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * Search for movies using TMDb API.
     *
     * @param query Search query
     * @return List of movie search results
     */
    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<?>> searchMovies(@RequestParam String query) {
        return movieService.searchMovies(query)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", ex.getMessage())));
    }

    /**
     * Add a new movie to the watchlist.
     *
     * @param request Request body containing movie title
     * @return The created movie or error response
     */
    @PostMapping
    public CompletableFuture<ResponseEntity<?>> addMovie(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        if (title == null) {
            // Immediately return a failed future for missing title
            CompletableFuture<ResponseEntity<?>> failed = new CompletableFuture<>();
            failed.complete(ResponseEntity.badRequest().body(Map.of("error", "Movie title is required")));
            return failed;
        }
        return movieService.addMovieByTitle(title)
                .<ResponseEntity<?>>thenApply(movie -> ResponseEntity.status(HttpStatus.CREATED).body(new MovieResponse(movie)))
                .exceptionally(this::handleAddMovieException);
    }

    private ResponseEntity<?> handleAddMovieException(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest().body(Map.of("error", cause.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", cause.getMessage()));
    }

    /**
     * Get paginated list of movies.
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @return Paginated movie list
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<MovieResponse>> getMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PaginatedResponse<MovieResponse> response = movieService.getMovies(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific movie by ID.
     *
     * @param id Movie ID
     * @return Movie details or 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMovie(@PathVariable Long id) {
        Optional<MovieResponse> movie = movieService.getMovieById(id);
        return movie.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update watched status of a movie.
     *
     * @param id Movie ID
     * @param request Request body with watched status
     * @return Updated movie or error response
     */
    @PatchMapping("/{id}/watched")
    public ResponseEntity<?> updateWatchedStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {

        Boolean watched = request.get("watched");
        try {
            Optional<Movie> updatedMovie = movieService.updateWatchedStatus(id, watched);
            return updatedMovie.map(movie -> ResponseEntity.ok(new MovieResponse(movie)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update movie rating.
     *
     * @param id Movie ID
     * @param request Request body with rating
     * @return Updated movie or error response
     */
    @PatchMapping("/{id}/rating")
    public ResponseEntity<?> updateRating(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {

        Integer rating = request.get("rating");
        try {
            Optional<Movie> updatedMovie = movieService.updateRating(id, rating);
            return updatedMovie.map(movie -> ResponseEntity.ok(new MovieResponse(movie)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a movie from the watchlist.
     *
     * @param id Movie ID
     * @return Success message or 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        boolean deleted = movieService.deleteMovie(id);

        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Movie deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}