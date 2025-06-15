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
    
    @Value("${movie.images.path}")
    private String imagesPath;
    
    private final HttpClient httpClient;
    
    public ImageDownloadService() {
        this.httpClient = HttpClient.newHttpClient();
    }
    
    public CompletableFuture<List<String>> downloadImages(List<String> imagePaths, String movieTitle) {
        // Create directory if it doesn't exist
        try {
            Path directory = Paths.get(imagesPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create images directory", e);
        }
        
        List<CompletableFuture<String>> downloadTasks = new ArrayList<>();
        
        // Limit to 3 images as per requirements
        int imagesToDownload = Math.min(imagePaths.size(), 3);
        
        for (int i = 0; i < imagesToDownload; i++) {
            String imagePath = imagePaths.get(i);
            String imageUrl = "https://image.tmdb.org/t/p/w780" + imagePath;
            String fileName = sanitizeFileName(movieTitle) + "_" + i + getFileExtension(imagePath);
            String localPath = imagesPath + fileName;
            
            CompletableFuture<String> downloadTask = downloadImage(imageUrl, localPath);
            downloadTasks.add(downloadTask);
        }
        
        return CompletableFuture.allOf(downloadTasks.toArray(new CompletableFuture[0]))
                .thenApply(v -> downloadTasks.stream()
                        .map(CompletableFuture::join)
                        .filter(path -> path != null)
                        .toList());
    }
    
    private CompletableFuture<String> downloadImage(String imageUrl, String localPath) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    try {
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
    
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
    
    private String getFileExtension(String imagePath) {
        int lastDot = imagePath.lastIndexOf('.');
        return lastDot > 0 ? imagePath.substring(lastDot) : ".jpg";
    }
}