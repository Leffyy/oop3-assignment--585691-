package com.example.moviewatchlist.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageDownloadService {
    
    // Path where we'll save downloaded images, loaded from application properties
    @Value("${movie.images.path}")
    private String imagesPath;
    
    private final HttpClient httpClient;
    
    public ImageDownloadService() {
        // Create a single HTTP client to use for all downloads
        this.httpClient = HttpClient.newHttpClient();
    }
    
    // Downloads up to 3 images for a movie and returns their local file paths
    public CompletableFuture<List<String>> downloadImages(List<String> imagePaths, String movieTitle) {
        // Make sure the images directory exists before downloading
        try {
            Path directory = Paths.get(imagesPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create images directory", e);
        }
        
        List<CompletableFuture<String>> downloadTasks = new ArrayList<>();
        
        // Only download the first 3 images to avoid too many files
        int imagesToDownload = Math.min(imagePaths.size(), 3);
        
        for (int i = 0; i < imagesToDownload; i++) {
            String imagePath = imagePaths.get(i);
            // Build the full TMDB image URL
            String imageUrl = "https://image.tmdb.org/t/p/w780" + imagePath;
            // Create a unique filename using movie title and index
            String fileName = sanitizeFileName(movieTitle) + "_" + i + getFileExtension(imagePath);
            String localPath = imagesPath + fileName;
            
            // Start downloading this image asynchronously
            CompletableFuture<String> downloadTask = downloadImage(imageUrl, localPath);
            downloadTasks.add(downloadTask);
        }
        
        // Wait for all downloads to finish and return successful paths
        return CompletableFuture.allOf(downloadTasks.toArray(new CompletableFuture[0]))
                .thenApply(v -> downloadTasks.stream()
                        .map(CompletableFuture::join)
                        .filter(path -> path != null)
                        .toList());
    }
    
    // Downloads a single image from URL and saves it to local path
    private CompletableFuture<String> downloadImage(String imageUrl, String localPath) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    try {
                        // Only save if we got a successful response
                        if (response.statusCode() == 200) {
                            Files.write(Paths.get(localPath), response.body());
                            return localPath;
                        }
                        return null;
                    } catch (IOException e) {
                        System.err.println("Failed to save image: " + e.getMessage());
                        return null;
                    }
                });
    }
    
    // Removes special characters from filename to avoid file system issues
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
    
    // Extracts file extension from image path, defaults to .jpg if none found
    private String getFileExtension(String imagePath) {
        int lastDot = imagePath.lastIndexOf('.');
        return lastDot > 0 ? imagePath.substring(lastDot) : ".jpg";
    }
}