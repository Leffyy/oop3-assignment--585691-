package com.example.moviewatchlist.controller;

import com.example.moviewatchlist.dto.MovieResponse;
import com.example.moviewatchlist.dto.PaginatedResponse;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "*")
public class MovieController {
    
    @Autowired
    private MovieService movieService;
    
    @PostMapping
    public CompletableFuture<ResponseEntity<?>> addMovie(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        if (title == null || title.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body(Map.of("error", "Movie title is required"))
            );
        }
        
        return movieService.addMovieToWatchlist(title.trim())
                .thenApply(movie -> (ResponseEntity<?>) ResponseEntity.status(HttpStatus.CREATED).body(new MovieResponse(movie)))
                .exceptionally(ex -> {
                    // Get the root cause of the exception
                    Throwable cause = ex;
                    while (cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                    String errorMessage = cause.getMessage();
                    return ResponseEntity.badRequest().body(Map.of("error", errorMessage));
                });
    }
    
    @GetMapping
    public ResponseEntity<PaginatedResponse<MovieResponse>> getMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 10;
        
        PaginatedResponse<MovieResponse> response = movieService.getMovies(page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getMovie(@PathVariable Long id) {
        Optional<MovieResponse> movie = movieService.getMovieById(id);
        if (movie.isPresent()) {
            return ResponseEntity.ok(movie.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    @PatchMapping("/{id}/watched")
    public ResponseEntity<?> updateWatchedStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> request) {
        
        Boolean watched = request.get("watched");
        if (watched == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Watched status is required"));
        }
        
        Optional<Movie> updatedMovie = movieService.updateWatchedStatus(id, watched);
        if (updatedMovie.isPresent()) {
            return ResponseEntity.ok(new MovieResponse(updatedMovie.get()));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PatchMapping("/{id}/rating")
    public ResponseEntity<?> updateRating(
            @PathVariable Long id, 
            @RequestBody Map<String, Integer> request) {
        
        Integer rating = request.get("rating");
        
        try {
            Optional<Movie> updatedMovie = movieService.updateRating(id, rating);
            if (updatedMovie.isPresent()) {
                return ResponseEntity.ok(new MovieResponse(updatedMovie.get()));
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        boolean deleted = movieService.deleteMovie(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Movie deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}