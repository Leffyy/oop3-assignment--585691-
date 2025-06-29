package com.example.moviewatchlist.controller;

import com.example.moviewatchlist.service.MovieService;
import com.example.moviewatchlist.dto.MovieResponse;
import com.example.moviewatchlist.dto.PaginatedResponse;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.controller.MovieController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the MovieController class.
 * Uses MockMvc to simulate HTTP requests and Mockito to mock the MovieService.
 */
@WebMvcTest(MovieController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class MovieControllerTest {

    /** MockMvc lets us simulate HTTP requests without starting a real server. */
    @Autowired
    private MockMvc mockMvc;

    /** Mocked MovieService for controlling service responses in tests. */
    @MockBean
    private MovieService movieService;

    /** Helps convert objects to JSON and back. */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests that adding a movie with an empty title returns a Bad Request status.
     */
    @Test
    public void testAddMovieWithEmptyTitle() throws Exception {
        Map<String, String> request = Map.of("title", "");
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests retrieving movies with pagination parameters returns an OK status.
     */
    @Test
    public void testGetMoviesWithPagination() throws Exception {
        PaginatedResponse<MovieResponse> mockResponse = new PaginatedResponse<>(
            new ArrayList<>(), 0, 5, 0, 0
        );
        when(movieService.getMovies(0, 5)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/movies")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk());
    }

    /**
     * Tests successfully adding a movie returns a Created status and correct movie title.
     */
    @Test
    public void testAddMovieSuccess() throws Exception {
        Map<String, String> request = Map.of("title", "Inception");
        Movie mockMovie = new Movie("Inception", "2010", "Christopher Nolan", "Sci-Fi");
        mockMovie.setId(1L);

        when(movieService.addMovieToWatchlist(anyString()))
            .thenReturn(CompletableFuture.completedFuture(mockMovie));

        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Inception"));
    }
}
