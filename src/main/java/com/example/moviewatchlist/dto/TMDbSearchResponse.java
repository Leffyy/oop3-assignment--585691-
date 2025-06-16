package com.example.moviewatchlist.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// This class represents the response we get from TMDb API when searching for movies
@JsonIgnoreProperties(ignoreUnknown = true)
public class TMDbSearchResponse {
    // The "results" field from the JSON response contains a list of movies
    @JsonProperty("results")
    private List<TMDbMovie> results;
    
    public List<TMDbMovie> getResults() { return results; }
    public void setResults(List<TMDbMovie> results) { this.results = results; }
    
    // This represents a single movie from the TMDb search results
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TMDbMovie {
        // Basic movie information
        private Integer id;
        private String title;
        private String overview;
        
        // TMDb sends this as "release_date" in JSON, so we map it to our field
        @JsonProperty("release_date")
        private String releaseDate;
        
        // TMDb sends this as "vote_average" in JSON for the movie rating
        @JsonProperty("vote_average")
        private Double voteAverage;
        
        // Standard getters and setters for all fields
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