package com.example.moviewatchlist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TMDbSearchResponse {
    private List<TMDbMovie> results;
    
    public List<TMDbMovie> getResults() { return results; }
    public void setResults(List<TMDbMovie> results) { this.results = results; }
    
    public static class TMDbMovie {
        private Integer id;
        private String title;
        private String overview;
        
        @JsonProperty("release_date")
        private String releaseDate;
        
        @JsonProperty("vote_average")
        private Double voteAverage;
        
        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getOverview() { return overview; }
        public void setOverview(String overview) { this.overview = overview; }
        
        public String getReleaseDate() { return releaseDate; }
        public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
        
        public Double getVoteAverage() { return voteAverage; }
        public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }
    }
}