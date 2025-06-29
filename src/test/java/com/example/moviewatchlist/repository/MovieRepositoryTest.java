package com.example.moviewatchlist.repository;

import com.example.moviewatchlist.model.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MovieRepository.
 * Uses @DataJpaTest to configure an in-memory database for testing.
 */
@DataJpaTest
public class MovieRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MovieRepository movieRepository;

    private Movie movie1;
    private Movie movie2;

    @BeforeEach
    void setUp() {
        // Create test movies
        movie1 = new Movie("Inception", "2010", "Christopher Nolan", "Sci-Fi");
        movie1.setPlot("A thief who enters dreams");
        movie1.setImdbRating("8.8");

        movie2 = new Movie("The Dark Knight", "2008", "Christopher Nolan", "Action");
        movie2.setPlot("Batman faces the Joker");
        movie2.setImdbRating("9.0");
    }

    /**
     * Tests saving a movie to the repository.
     */
    @Test
    void testSaveMovie() {
        Movie savedMovie = movieRepository.save(movie1);
        assertNotNull(savedMovie);
        assertNotNull(savedMovie.getId());
        assertEquals("Inception", savedMovie.getTitle());
        assertEquals("2010", savedMovie.getReleaseYear());
    }

    /**
     * Tests finding a movie by its ID.
     */
    @Test
    void testFindById() {
        Movie savedMovie = entityManager.persistAndFlush(movie1);
        Movie foundMovie = movieRepository.findById(savedMovie.getId()).orElse(null);
        assertNotNull(foundMovie);
        assertEquals(savedMovie.getId(), foundMovie.getId());
        assertEquals("Inception", foundMovie.getTitle());
    }

    /**
     * Tests existence check by title and release year.
     */
    @Test
    void testExistsByTitleAndReleaseYear() {
        entityManager.persistAndFlush(movie1);
        assertTrue(movieRepository.existsByTitleAndReleaseYear("Inception", "2010"));
        assertFalse(movieRepository.existsByTitleAndReleaseYear("Inception", "2011"));
        assertFalse(movieRepository.existsByTitleAndReleaseYear("Interstellar", "2010"));
    }

    /**
     * Tests paginated retrieval of movies.
     */
    @Test
    void testFindAllWithPagination() {
        entityManager.persistAndFlush(movie1);
        entityManager.persistAndFlush(movie2);
        Pageable pageable = PageRequest.of(0, 1);
        Page<Movie> page = movieRepository.findAll(pageable);
        assertNotNull(page);
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
    }

    /**
     * Tests deleting a movie by its ID.
     */
    @Test
    void testDeleteMovie() {
        Movie savedMovie = entityManager.persistAndFlush(movie1);
        Long movieId = savedMovie.getId();
        movieRepository.deleteById(movieId);
        entityManager.flush();
        assertFalse(movieRepository.existsById(movieId));
    }

    /**
     * Tests updating a movie's watched status and rating.
     */
    @Test
    void testUpdateMovie() {
        Movie savedMovie = entityManager.persistAndFlush(movie1);
        savedMovie.setWatched(true);
        savedMovie.setRating(5);
        Movie updatedMovie = movieRepository.save(savedMovie);
        assertEquals(savedMovie.getId(), updatedMovie.getId());
        assertTrue(updatedMovie.isWatched());
        assertEquals(5, updatedMovie.getRating());
    }

    /**
     * Tests retrieving all movies from the repository.
     */
    @Test
    void testFindAllMovies() {
        entityManager.persistAndFlush(movie1);
        entityManager.persistAndFlush(movie2);
        var movies = movieRepository.findAll();
        assertEquals(2, movies.size());
        assertTrue(movies.stream().anyMatch(m -> m.getTitle().equals("Inception")));
        assertTrue(movies.stream().anyMatch(m -> m.getTitle().equals("The Dark Knight")));
    }

    /**
     * Tests saving and retrieving movies with collection fields.
     */
    @Test
    void testMovieWithCollections() {
        movie1.setImagePaths(Arrays.asList("/path/to/image1.jpg", "/path/to/image2.jpg"));
        movie1.setSimilarMovies(Arrays.asList("Interstellar", "The Prestige"));
        Movie savedMovie = movieRepository.save(movie1);
        entityManager.flush();
        entityManager.clear(); // Clear cache to force reload from DB
        Movie foundMovie = movieRepository.findById(savedMovie.getId()).orElse(null);
        assertNotNull(foundMovie);
        assertNotNull(foundMovie.getImagePaths());
        assertEquals(2, foundMovie.getImagePaths().size());
        assertNotNull(foundMovie.getSimilarMovies());
        assertEquals(2, foundMovie.getSimilarMovies().size());
    }
}