package com.example.moviewatchlist.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// Response object for TMDb API similar movies endpoint
@JsonIgnoreProperties(ignoreUnknown = true)
public class TMDbSimilarResponse {
    // List of similar movies returned by the API
    private List<SimilarMovie> results;
    
    public List<SimilarMovie> getResults() { return results; }
    public void setResults(List<SimilarMovie> results) { this.results = results; }
    
    // Individual movie data within the similar movies response
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SimilarMovie {
        // Unique movie ID from TMDb
        private int id;
        // Movie title
        private String title;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}