package com.example.moviewatchlist.dto;

import java.util.List;

public class TMDbSimilarResponse {
    private List<SimilarMovie> results;
    
    public List<SimilarMovie> getResults() { return results; }
    public void setResults(List<SimilarMovie> results) { this.results = results; }
    
    public static class SimilarMovie {
        private String title;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}