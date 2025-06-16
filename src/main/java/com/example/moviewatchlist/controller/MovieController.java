package com.example.moviewatchlist.controller;

import com.example.moviewatchlist.dto.MovieResponse;
import com.example.moviewatchlist.dto.PaginatedResponse;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.service.MovieService;
import com.example.moviewatchlist.service.TMDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

// This controller handles all movie-related web requests
@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "*")
public class MovieController {
    
    // Inject the movie service to handle business logic
    @Autowired
    private MovieService movieService;
    
    // Inject TMDb service for search functionality
    @Autowired
    private TMDbService tmdbService;
    
    // Search for movies (for autocomplete)
    @GetMapping("/search")
    public ResponseEntity<?> searchMovies(@RequestParam String query) {
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
        
        // Call the TMDb service to search for movies
        return tmdbService.searchMovie(query.trim())
                .thenApply(response -> {
                    if (response.getResults() == null) {
                        return ResponseEntity.ok(new ArrayList<>());
                    }
                    
                    // Convert to a simpler format for the frontend
                    List<Map<String, Object>> results = response.getResults().stream()
                            .limit(10) // Limit to 10 results
                            .map(movie -> {
                                Map<String, Object> simplified = new HashMap<>();
                                simplified.put("id", movie.getId());
                                simplified.put("title", movie.getTitle());
                                simplified.put("releaseDate", movie.getReleaseDate());
                                simplified.put("voteAverage", movie.getVoteAverage());
                                simplified.put("posterPath", movie.getPosterPath());
                                return simplified;
                            })
                            .collect(Collectors.<Map<String, Object>>toList());
                    
                    return ResponseEntity.ok(results);
                })
                .exceptionally(ex -> {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ArrayList<>());
                })
                .join();
    }
    
    // Add a new movie to the watchlist
    @PostMapping
    public CompletableFuture<ResponseEntity<?>> addMovie(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        
        // Make sure the title isn't empty or missing
        if (title == null || title.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body(Map.of("error", "Movie title is required"))
            );
        }
        
        // Add the movie and return the result
        return movieService.addMovieToWatchlist(title.trim())
                .<ResponseEntity<?>>thenApply(movie -> ResponseEntity.status(HttpStatus.CREATED).body(new MovieResponse(movie)))
                .exceptionally(ex -> {
                    // Handle any errors that happen during movie creation
                    String errorMessage = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    return ResponseEntity.badRequest().body(Map.of("error", errorMessage));
                });
    }
    
    // Get a list of movies with pagination
    @GetMapping
    public ResponseEntity<PaginatedResponse<MovieResponse>> getMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Make sure page and size values are reasonable
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 10;
        
        PaginatedResponse<MovieResponse> response = movieService.getMovies(page, size);
        return ResponseEntity.ok(response);
    }
    
    // Get details for a specific movie by its ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMovie(@PathVariable Long id) {
        Optional<MovieResponse> movie = movieService.getMovieById(id);
        
        // Return the movie if found, otherwise return 404
        if (movie.isPresent()) {
            return ResponseEntity.ok(movie.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // Mark a movie as watched or unwatched
    @PatchMapping("/{id}/watched")
    public ResponseEntity<?> updateWatchedStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> request) {
        
        Boolean watched = request.get("watched");
        
        // Make sure watched status is provided
        if (watched == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Watched status is required"));
        }
        
        // Update the movie's watched status
        Optional<Movie> updatedMovie = movieService.updateWatchedStatus(id, watched);
        if (updatedMovie.isPresent()) {
            return ResponseEntity.ok(new MovieResponse(updatedMovie.get()));
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update a movie's rating
    @PatchMapping("/{id}/rating")
    public ResponseEntity<?> updateRating(
            @PathVariable Long id, 
            @RequestBody Map<String, Integer> request) {
        
        Integer rating = request.get("rating");
        
        try {
            // Try to update the rating
            Optional<Movie> updatedMovie = movieService.updateRating(id, rating);
            if (updatedMovie.isPresent()) {
                return ResponseEntity.ok(new MovieResponse(updatedMovie.get()));
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            // Handle invalid rating values (like negative numbers or too high)
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Remove a movie from the watchlist
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        boolean deleted = movieService.deleteMovie(id);
        
        // Confirm deletion or return 404 if movie wasn't found
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Movie deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}