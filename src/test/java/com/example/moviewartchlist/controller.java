package com.example.moviewatchlist.controller;

import com.example.moviewatchlist.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
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
        mockMvc.perform(get("/api/movies")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk());
    }
}