package com.example.moviewatchlist.dto;

import java.util.List;

public class TMDbImagesResponse {
    private List<ImageData> backdrops;
    private List<ImageData> posters;
    
    public List<ImageData> getBackdrops() { return backdrops; }
    public void setBackdrops(List<ImageData> backdrops) { this.backdrops = backdrops; }
    
    public List<ImageData> getPosters() { return posters; }
    public void setPosters(List<ImageData> posters) { this.posters = posters; }
    
    public static class ImageData {
        private String file_path;
        private Double vote_average;
        
        public String getFile_path() { return file_path; }
        public void setFile_path(String file_path) { this.file_path = file_path; }
        
        public Double getVote_average() { return vote_average; }
        public void setVote_average(Double vote_average) { this.vote_average = vote_average; }
    }
}