package com.example.moviewatchlist.repository;

import com.example.moviewatchlist.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

/**
 * Repository interface for Movie entities.
 * Handles all database operations for movies, including pagination and existence checks.
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Retrieves a paginated list of movies.
     *
     * @param pageable pagination information
     * @return a page of movies
     */
    @NonNull
    Page<Movie> findAll(@NonNull Pageable pageable);

    /**
     * Checks if a movie with the given title and release year already exists.
     *
     * @param title the movie title
     * @param year the release year
     * @return true if a movie with the same title and year exists, false otherwise
     */
    boolean existsByTitleAndReleaseYear(String title, String year);
}       