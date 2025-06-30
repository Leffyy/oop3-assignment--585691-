package com.example.moviewatchlist.controller;

import com.example.moviewatchlist.dto.MovieResponse;
import com.example.moviewatchlist.dto.PaginatedResponse;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.Optional;

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
    public DeferredResult<ResponseEntity<?>> searchMovies(@RequestParam String query) {
        DeferredResult<ResponseEntity<?>> output = new DeferredResult<>();
        movieService.searchMovies(query)
            .thenAccept(result -> output.setResult(ResponseEntity.ok(result)))
            .exceptionally(ex -> {
                output.setErrorResult(buildInternalServerErrorResponse(ex));
                return null;
            });
        return output;
    }

    private ResponseEntity<?> buildInternalServerErrorResponse(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(Map.of("error", cause.getMessage()));
    }

    /**
     * Add a new movie to the watchlist.
     *
     * @param request Request body containing movie title
     * @return The created movie or error response
     */
    @PostMapping
    public DeferredResult<ResponseEntity<?>> addMovie(@RequestBody Map<String, String> request) {
        DeferredResult<ResponseEntity<?>> output = new DeferredResult<>();
        String title = request.get("title");
        if (title == null || title.trim().isEmpty()) {
            output.setResult(buildBadRequestResponse("Movie title is required"));
            return output;
        }
        movieService.addMovieByTitle(title)
            .thenAccept(movie -> output.setResult(ResponseEntity.status(HttpStatus.CREATED).body(new MovieResponse(movie))))
            .exceptionally(ex -> {
                output.setResult(buildBadRequestResponse(ex));
                return null;
            });
        return output;
    }

    private ResponseEntity<?> buildBadRequestResponse(String message) {
        return ResponseEntity
                .badRequest()
                .header("Content-Type", "application/json")
                .body(Map.of("error", message));
    }

    private ResponseEntity<?> buildBadRequestResponse(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return buildBadRequestResponse(cause.getMessage());
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
            @RequestBody Map<String, Object> request) {
        Boolean watched = extractWatched(request.get("watched"));
        return watchedUpdateResponse(id, watched);
    }

    private ResponseEntity<?> watchedUpdateResponse(Long id, Boolean watched) {
        if (watched == null) {
            return watchedBadRequest();
        }
        return handleWatchedUpdate(id, watched);
    }

    private ResponseEntity<?> watchedBadRequest() {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Watched status is required and must be true or false"));
    }

    private ResponseEntity<?> handleWatchedUpdate(Long id, Boolean watched) {
        try {
            Optional<Movie> updatedMovie = movieService.updateWatchedStatus(id, watched);
            return watchedUpdateResult(updatedMovie);
        } catch (IllegalArgumentException e) {
            return watchedUpdateError(e);
        }
    }

    private ResponseEntity<?> watchedUpdateResult(Optional<Movie> updatedMovie) {
        return updatedMovie
                .map(movie -> ResponseEntity.ok(new MovieResponse(movie)))
                .orElse(ResponseEntity.notFound().build());
    }

    private ResponseEntity<?> watchedUpdateError(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    private Boolean extractWatched(Object watchedObj) {
        if (watchedObj instanceof Boolean) {
            return (Boolean) watchedObj;
        }
        if (watchedObj instanceof String) {
            String str = ((String) watchedObj).trim().toLowerCase();
            if ("true".equals(str) || "false".equals(str)) {
                return Boolean.parseBoolean(str);
            }
        }
        return null;
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
            @RequestBody Map<String, Object> request) {
        Integer rating = extractRating(request.get("rating"));
        return ratingUpdateResponse(id, rating);
    }

    private ResponseEntity<?> ratingUpdateResponse(Long id, Integer rating) {
        if (rating == null) {
            return ratingBadRequest();
        }
        return handleRatingUpdate(id, rating);
    }

    private ResponseEntity<?> ratingBadRequest() {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Rating is required and must be an integer"));
    }

    private ResponseEntity<?> handleRatingUpdate(Long id, Integer rating) {
        try {
            Optional<Movie> updatedMovie = movieService.updateRating(id, rating);
            return ratingUpdateResult(updatedMovie);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private ResponseEntity<?> ratingUpdateResult(Optional<Movie> updatedMovie) {
        return updatedMovie
                .map(movie -> ResponseEntity.ok(new MovieResponse(movie)))
                .orElse(ResponseEntity.notFound().build());
    }

    private Integer extractRating(Object ratingObj) {
        if (ratingObj instanceof Integer) {
            return (Integer) ratingObj;
        } else if (ratingObj instanceof Double) {
            return ((Double) ratingObj).intValue();
        } else if (ratingObj instanceof String) {
            try {
                return Integer.parseInt((String) ratingObj);
            } catch (NumberFormatException ignored) {}
        }
        return null;
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