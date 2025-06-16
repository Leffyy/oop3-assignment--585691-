package com.example.moviewatchlist.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// Response class for TMDb API images endpoint
@JsonIgnoreProperties(ignoreUnknown = true)
public class TMDbImagesResponse {
    private List<ImageData> backdrops;
    private List<ImageData> posters;
    
    // Get the list of backdrop images
    public List<ImageData> getBackdrops() { return backdrops; }
    public void setBackdrops(List<ImageData> backdrops) { this.backdrops = backdrops; }
    
    // Get the list of poster images
    public List<ImageData> getPosters() { return posters; }
    public void setPosters(List<ImageData> posters) { this.posters = posters; }
    
    // Represents individual image data from TMDb
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageData {
        private String file_path;
        private Double vote_average;
        
        // File path to the image on TMDb servers
        public String getFile_path() { return file_path; }
        public void setFile_path(String file_path) { this.file_path = file_path; }
        
        // Average rating for this image
        public Double getVote_average() { return vote_average; }
        public void setVote_average(Double vote_average) { this.vote_average = vote_average; }
    }
}