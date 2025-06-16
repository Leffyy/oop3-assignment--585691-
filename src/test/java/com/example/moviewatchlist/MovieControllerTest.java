package com.example.moviewatchlist;

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
// import java.util.List;
import java.util.ArrayList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// This tells Spring to only load the MovieController for testing
@WebMvcTest(MovieController.class)
// Disables security features for testing purposes
@AutoConfigureMockMvc(addFilters = false) 
@ExtendWith(MockitoExtension.class)
public class MovieControllerTest {
    
    // MockMvc lets us simulate HTTP requests without starting a real server
    @Autowired
    private MockMvc mockMvc;
    
    // Creates a fake MovieService that we can control in our tests
    @MockBean
    private MovieService movieService;
    
    // Helps convert objects to JSON and back
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    public void testAddMovieWithEmptyTitle() throws Exception {
        // Create a request with an empty title to test validation
        Map<String, String> request = Map.of("title", "");
        
        // Send a POST request and expect it to fail with Bad Request status
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void testGetMoviesWithPagination() throws Exception {
        // Create a fake response with empty movie list and pagination info
        PaginatedResponse<MovieResponse> mockResponse = new PaginatedResponse<>(
            new ArrayList<>(), 0, 5, 0, 0
        );
        // Tell our fake service what to return when getMovies is called
        when(movieService.getMovies(0, 5)).thenReturn(mockResponse);
        
        // Send a GET request with pagination parameters and expect success
        mockMvc.perform(get("/api/movies")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testAddMovieSuccess() throws Exception {
        // Create a request with a valid movie title
        Map<String, String> request = Map.of("title", "Inception");
        // Create a fake movie that our service will pretend to return
        Movie mockMovie = new Movie("Inception", "2010", "Christopher Nolan", "Sci-Fi");
        mockMovie.setId(1L);

        // Tell our fake service to return the mock movie when addMovieToWatchlist is called
        when(movieService.addMovieToWatchlist(anyString()))
            .thenReturn(CompletableFuture.completedFuture(mockMovie));

        // Send a POST request and check that we get a successful response with the right movie title
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Inception"));
    }
}
