package com.example.moviewatchlist.controller;

import com.example.moviewatchlist.dto.MovieResponse;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.service.MovieService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
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
        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>();
        request.put("title", "");
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        if (body instanceof Map<?, ?> map) {
            assertTrue(map.get("error").toString().contains("Movie title is required"));
        } else {
            fail("Expected error response as a Map");
        }
        verifyNoInteractions(mockService);
    }

    /**
     * Tests that adding a movie with a null title returns a Bad Request status.
     */
    @Test
    void testAddMovieWithNullTitle() {
        // Arrange
        MovieService mockService = mock(MovieService.class);
        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>();
        request.put("title", null);
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        if (body instanceof Map<?, ?> map) {
            assertTrue(map.get("error").toString().contains("Movie title is required"));
        } else {
            fail("Expected error response as a Map");
        }
        verifyNoInteractions(mockService);
    }

    /**
     * Tests that adding a movie with a whitespace-only title returns a Bad Request status.
     */
    @Test
    void testAddMovieWithWhitespaceTitle() {
        // Arrange
        MovieService mockService = mock(MovieService.class);
        MovieController controller = new MovieController(mockService);

        // Act
        Map<String, String> request = new HashMap<>();
        request.put("title", "   ");
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        if (body instanceof Map<?, ?> map) {
            assertTrue(map.get("error").toString().contains("Movie title is required"));
        } else {
            fail("Expected error response as a Map");
        }
        verifyNoInteractions(mockService);
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
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        if (body instanceof Map<?, ?> map) {
            assertTrue(map.get("error").toString().contains("Movie title is required"));
        } else {
            fail("Expected error response as a Map");
        }
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
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);

        // Wait for async result
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        if (body instanceof Map<?, ?> map) {
            assertTrue(map.get("error").toString().contains("Movie already exists"));
        } else {
            fail("Expected error response as a Map");
        }
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
        DeferredResult<ResponseEntity<?>> result = controller.addMovie(request);

        // Wait for async result
        ResponseEntity<?> response = getResult(result);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Object body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body instanceof MovieResponse);
        assertEquals("Inception", ((MovieResponse) body).getTitle());
        verify(mockService).addMovieByTitle("Inception");
    }

    /**
     * Tests that getting a movie by ID returns the correct movie when found.
     */
    @Test
    void getMovie_returnsMovie_whenFound() {
        // Arrange
        MovieService movieService = mock(MovieService.class);
        MovieResponse movieResponse = new MovieResponse(
            1L,
            "Inception",
            "2010",
            "Christopher Nolan",
            "Sci-Fi",
            "A thief who steals corporate secrets",
            "148 min",
            "8.8",
            "A mind-bending thriller",
            "2010-07-16",
            8.8,
            List.of("image1.jpg", "image2.jpg"),
            List.of("Similar Movie 1", "Similar Movie 2"),
            true,
            5
        );
        when(movieService.getMovieById(1L)).thenReturn(Optional.of(movieResponse));
        MovieController controller = new MovieController(movieService);

        // Act
        ResponseEntity<?> response = controller.getMovie(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(movieResponse, response.getBody());
    }

    /**
     * Tests that getting a movie by ID returns a Not Found status when the movie is missing.
     */
    @Test
    void getMovie_returnsNotFound_whenMissing() {
        // Arrange
        MovieService movieService = mock(MovieService.class);
        when(movieService.getMovieById(2L)).thenReturn(Optional.empty());
        MovieController controller = new MovieController(movieService);

        // Act
        ResponseEntity<?> response = controller.getMovie(2L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Nested
    class ExtractWatchedTests {
        MovieController controller = new MovieController(null);

        @Test
        void returnsBoolean_whenBooleanInput() {
            assertEquals(Boolean.TRUE, controllerTestExtractWatched(true));
            assertEquals(Boolean.FALSE, controllerTestExtractWatched(false));
        }

        @Test
        void returnsBoolean_whenStringTrueOrFalse() {
            assertEquals(Boolean.TRUE, controllerTestExtractWatched("true"));
            assertEquals(Boolean.FALSE, controllerTestExtractWatched("false"));
            assertEquals(Boolean.TRUE, controllerTestExtractWatched(" TRUE "));
            assertEquals(Boolean.FALSE, controllerTestExtractWatched(" False "));
        }

        @Test
        void returnsNull_whenStringInvalid() {
            assertNull(controllerTestExtractWatched("yes"));
            assertNull(controllerTestExtractWatched("1"));
            assertNull(controllerTestExtractWatched(""));
        }

        @Test
        void returnsNull_whenNullOrOtherType() {
            assertNull(controllerTestExtractWatched(null));
            assertNull(controllerTestExtractWatched(1));
            assertNull(controllerTestExtractWatched(new Object()));
        }

        // Helper to access private method via reflection
        private Boolean controllerTestExtractWatched(Object input) {
            try {
                var m = MovieController.class.getDeclaredMethod("extractWatched", Object.class);
                m.setAccessible(true);
                return (Boolean) m.invoke(controller, input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    class ExtractRatingTests {
        MovieController controller = new MovieController(null);

        @Test
        void returnsInteger_whenIntegerInput() {
            assertEquals(5, controllerTestExtractRating(5));
            assertEquals(0, controllerTestExtractRating(0));
        }

        @Test
        void returnsInteger_whenDoubleInput() {
            assertEquals(5, controllerTestExtractRating(5.0));
            assertEquals(7, controllerTestExtractRating(7.9));
        }

        @Test
        void returnsInteger_whenStringInput() {
            assertEquals(8, controllerTestExtractRating("8"));
            assertEquals(0, controllerTestExtractRating("0"));
        }

        @Test
        void returnsNull_whenStringInvalid() {
            assertNull(controllerTestExtractRating("notanumber"));
            assertNull(controllerTestExtractRating(""));
        }

        @Test
        void returnsNull_whenNullOrOtherType() {
            assertNull(controllerTestExtractRating(null));
            assertNull(controllerTestExtractRating(true));
            assertNull(controllerTestExtractRating(new Object()));
        }

        // Helper to access private method via reflection
        private Integer controllerTestExtractRating(Object input) {
            try {
                var m = MovieController.class.getDeclaredMethod("extractRating", Object.class);
                m.setAccessible(true);
                return (Integer) m.invoke(controller, input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    class WatchedUpdateResponseTests {
        MovieService mockService = mock(MovieService.class);
        MovieController controller = new MovieController(mockService);

        @SuppressWarnings({ "ConstantConditions", "null" })
        @Test
        void returnsBadRequest_whenWatchedNull() {
            ResponseEntity<?> response = callWatchedUpdateResponse(null);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().toString().contains("Watched status is required"));
        }

        @Test
        void returnsNotFound_whenServiceReturnsEmpty() {
            when(mockService.updateWatchedStatus(1L, true)).thenReturn(Optional.empty());
            ResponseEntity<?> response = callWatchedUpdateResponse(true);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void returnsOk_whenServiceReturnsMovie() {
            Movie movie = Movie.builder().id(1L).title("Test").build();
            when(mockService.updateWatchedStatus(1L, true)).thenReturn(Optional.of(movie));
            ResponseEntity<?> response = callWatchedUpdateResponse(true);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof MovieResponse);
        }

        @SuppressWarnings("null")
        @Test
        void returnsBadRequest_whenServiceThrows() {
            when(mockService.updateWatchedStatus(1L, true)).thenThrow(new IllegalArgumentException("fail"));
            ResponseEntity<?> response = callWatchedUpdateResponse(true);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().toString().contains("fail"));
        }

        private ResponseEntity<?> callWatchedUpdateResponse(Boolean watched) {
            try {
                var m = MovieController.class.getDeclaredMethod("watchedUpdateResponse", Long.class, Boolean.class);
                m.setAccessible(true);
                return (ResponseEntity<?>) m.invoke(controller, 1L, watched);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    class RatingUpdateResponseTests {
        MovieService mockService = mock(MovieService.class);
        MovieController controller = new MovieController(mockService);

        @SuppressWarnings("null")
        @Test
        void returnsBadRequest_whenRatingNull() {
            ResponseEntity<?> response = callRatingUpdateResponse(null);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().toString().contains("Rating is required"));
        }

        @Test
        void returnsNotFound_whenServiceReturnsEmpty() {
            when(mockService.updateRating(1L, 5)).thenReturn(Optional.empty());
            ResponseEntity<?> response = callRatingUpdateResponse(5);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void returnsOk_whenServiceReturnsMovie() {
            Movie movie = Movie.builder().id(1L).title("Test").build();
            when(mockService.updateRating(1L, 5)).thenReturn(Optional.of(movie));
            ResponseEntity<?> response = callRatingUpdateResponse(5);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof MovieResponse);
        }

        @SuppressWarnings("null")
        @Test
        void returnsBadRequest_whenServiceThrows() {
            when(mockService.updateRating(1L, 5)).thenThrow(new IllegalArgumentException("fail"));
            ResponseEntity<?> response = callRatingUpdateResponse(5);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().toString().contains("fail"));
        }

        private ResponseEntity<?> callRatingUpdateResponse(Integer rating) {
            try {
                var m = MovieController.class.getDeclaredMethod("ratingUpdateResponse", Long.class, Integer.class);
                m.setAccessible(true);
                return (ResponseEntity<?>) m.invoke(controller, 1L, rating);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Utility method to wait for DeferredResult (with timeout)
    private ResponseEntity<?> getResult(DeferredResult<ResponseEntity<?>> result) {
        for (int i = 0; i < 100; i++) {
            Object value = result.getResult();
            if (value != null) return (ResponseEntity<?>) value;
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        }
        fail("DeferredResult did not complete in time");
        return null;
    }

    @Test
    void updateWatchedStatus_returnsBadRequest_whenWatchedNull() {
        MovieService mockService = mock(MovieService.class);
        MovieController controller = new MovieController(mockService);
        Map<String, Object> request = new HashMap<>(); // no "watched" key
        ResponseEntity<?> response = controller.updateWatchedStatus(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateWatchedStatus_returnsNotFound_whenServiceReturnsEmpty() {
        MovieService mockService = mock(MovieService.class);
        when(mockService.updateWatchedStatus(1L, true)).thenReturn(Optional.empty());
        MovieController controller = new MovieController(mockService);
        Map<String, Object> request = Map.of("watched", true);
        ResponseEntity<?> response = controller.updateWatchedStatus(1L, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateWatchedStatus_returnsOk_whenServiceReturnsMovie() {
        MovieService mockService = mock(MovieService.class);
        Movie movie = Movie.builder().id(1L).title("Test").build();
        when(mockService.updateWatchedStatus(1L, true)).thenReturn(Optional.of(movie));
        MovieController controller = new MovieController(mockService);
        Map<String, Object> request = Map.of("watched", true);
        ResponseEntity<?> response = controller.updateWatchedStatus(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof MovieResponse);
    }

    @Test
    void updateRating_returnsBadRequest_whenRatingNull() {
        MovieService mockService = mock(MovieService.class);
        MovieController controller = new MovieController(mockService);
        Map<String, Object> request = new HashMap<>(); // no "rating" key
        ResponseEntity<?> response = controller.updateRating(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateRating_returnsNotFound_whenServiceReturnsEmpty() {
        MovieService mockService = mock(MovieService.class);
        when(mockService.updateRating(1L, 5)).thenReturn(Optional.empty());
        MovieController controller = new MovieController(mockService);
        Map<String, Object> request = Map.of("rating", 5);
        ResponseEntity<?> response = controller.updateRating(1L, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateRating_returnsOk_whenServiceReturnsMovie() {
        MovieService mockService = mock(MovieService.class);
        Movie movie = Movie.builder().id(1L).title("Test").build();
        when(mockService.updateRating(1L, 5)).thenReturn(Optional.of(movie));
        MovieController controller = new MovieController(mockService);
        Map<String, Object> request = Map.of("rating", 5);
        ResponseEntity<?> response = controller.updateRating(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof MovieResponse);
    }

    @SuppressWarnings("null")
    @Test
    void deleteMovie_returnsOk_whenDeleted() {
        MovieService mockService = mock(MovieService.class);
        when(mockService.deleteMovie(1L)).thenReturn(true);
        MovieController controller = new MovieController(mockService);
        ResponseEntity<?> response = controller.deleteMovie(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Movie deleted successfully"));
    }

    @Test
    void deleteMovie_returnsNotFound_whenNotDeleted() {
        MovieService mockService = mock(MovieService.class);
        when(mockService.deleteMovie(1L)).thenReturn(false);
        MovieController controller = new MovieController(mockService);
        ResponseEntity<?> response = controller.deleteMovie(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}