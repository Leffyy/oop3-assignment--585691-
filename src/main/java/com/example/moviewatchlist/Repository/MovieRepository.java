package com.example.moviewatchlist.repository;

import com.example.moviewatchlist.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

// This interface handles all database operations for movies
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    
    // Gets movies in pages (for pagination) - can't be null
    @NonNull
    Page<Movie> findAll(@NonNull Pageable pageable);
    
    // Checks if a movie with the same title and year already exists
    boolean existsByTitleAndReleaseYear(String title, String year);
}