package com.example.moviewatchlist.repository;

import com.example.moviewatchlist.model.Movie;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for MovieRepository using JUnit and Mockito.
 */
class MovieRepositoryUnitTest {

    @Test
    void testFindById_Mockito() {
        // Arrange
        MovieRepository mockRepo = mock(MovieRepository.class);
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");
        when(mockRepo.findById(1L)).thenReturn(Optional.of(movie));

        // Act
        Optional<Movie> result = mockRepo.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Inception", result.get().getTitle());
        verify(mockRepo).findById(1L);
    }

    @Test
    void testExistsByTitleAndReleaseYear_Mockito() {
        // Arrange
        MovieRepository mockRepo = mock(MovieRepository.class);
        when(mockRepo.existsByTitleAndReleaseYear("Inception", "2010")).thenReturn(true);

        // Act
        boolean exists = mockRepo.existsByTitleAndReleaseYear("Inception", "2010");

        // Assert
        assertTrue(exists);
        verify(mockRepo).existsByTitleAndReleaseYear("Inception", "2010");
    }
}