package com.example.moviewatchlist;

import com.example.moviewatchlist.service.MovieService;
import com.example.moviewatchlist.dto.MovieResponse;
import com.example.moviewatchlist.dto.PaginatedResponse;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.controller.MovieController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
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

@WebMvcTest(MovieController.class)
// disables security features for testing purposes
@AutoConfigureMockMvc(addFilters = false) 
public class MovieControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MovieService movieService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    public void testAddMovieWithEmptyTitle() throws Exception {
        Map<String, String> request = Map.of("title", "");
        
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void testGetMoviesWithPagination() throws Exception {
        // Mock the service response
        PaginatedResponse<MovieResponse> mockResponse = new PaginatedResponse<>(
            new ArrayList<>(), 0, 5, 0, 0
        );
        when(movieService.getMovies(0, 5)).thenReturn(mockResponse);
        
        mockMvc.perform(get("/api/movies")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testAddMovieSuccess() throws Exception {
        // test data set up
        Map<String, String> request = Map.of("title", "Inception");
        Movie mockMovie = new Movie("Inception", "2010", "Christopher Nolan", "Sci-Fi");
        mockMovie.setId(1L);


        // Mock the service to return the mock movie
        when(movieService.addMovieToWatchlist(anyString()))
            .thenReturn(CompletableFuture.completedFuture(mockMovie));

        // Perform the request and verify the response
        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Inception"));
    }
}

