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

/**
 * Service for downloading and saving movie images from TMDb.
 * Downloads up to 3 images per movie and stores them locally.
 */
@Service
public class ImageDownloadService {

    /** Directory path for saving downloaded images, loaded from application properties. */
    @Value("${movie.images.path}")
    private String imagesPath;

    private final HttpClient httpClient;

    public ImageDownloadService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Downloads up to 3 images for a movie and returns their local file paths.
     *
     * @param imagePaths List of TMDb image paths
     * @param movieTitle The movie title (used for filenames)
     * @return CompletableFuture with list of local file paths
     */
    public CompletableFuture<List<String>> downloadImages(List<String> imagePaths, String movieTitle) {
        ensureImagesDirectoryExists();

        List<CompletableFuture<String>> downloadTasks = createDownloadTasks(imagePaths, movieTitle);

        return CompletableFuture.allOf(downloadTasks.toArray(new CompletableFuture[0]))
                .thenApply(v -> downloadTasks.stream()
                        .map(CompletableFuture::join)
                        .filter(path -> path != null)
                        .toList());
    }

    /** Ensures the images directory exists, creating it if necessary. */
    private void ensureImagesDirectoryExists() {
        try {
            Path directory = Paths.get(imagesPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create images directory", e);
        }
    }

    /** Creates download tasks for up to 3 images. */
    private List<CompletableFuture<String>> createDownloadTasks(List<String> imagePaths, String movieTitle) {
        List<CompletableFuture<String>> downloadTasks = new ArrayList<>();
        int imagesToDownload = Math.min(imagePaths.size(), 3);

        for (int i = 0; i < imagesToDownload; i++) {
            downloadTasks.add(createDownloadTask(imagePaths.get(i), movieTitle, i));
        }
        return downloadTasks;
    }

    private CompletableFuture<String> createDownloadTask(String imagePath, String movieTitle, int index) {
        String imageUrl = "https://image.tmdb.org/t/p/w780" + imagePath;
        String safeTitle = sanitizeFileName(movieTitle);
        String imageName = index + getFileExtension(imagePath);
        String safeImageName = sanitizeFileName(imageName);
        String fileName = safeTitle + "_" + safeImageName;
        String localPath = Paths.get(imagesPath, fileName).toString();
        return downloadImage(imageUrl, localPath);
    }

    /**
     * Downloads a single image from URL and saves it to local path.
     *
     * @param imageUrl  The image URL
     * @param localPath The local file path to save the image
     * @return CompletableFuture with the local file path, or null if failed
     */
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

    /** Removes special characters from filename to avoid file system issues. */
    private String sanitizeFileName(String fileName) {
        // Replace all forbidden characters (\ / : * ? " < > |) with _
        String sanitized = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        // Then replace anything not a-z, A-Z, 0-9, dash, underscore, dot with _
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "_");
        // Replace all dots except the last one with _
        int lastDot = sanitized.lastIndexOf('.');
        if (lastDot > 0) {
            String name = sanitized.substring(0, lastDot).replace(".", "_");
            String ext = sanitized.substring(lastDot);
            return name + ext;
        }
        return sanitized;
    }

    /** Extracts file extension from image path, defaults to .jpg if none found. */
    private String getFileExtension(String imagePath) {
        int lastDot = imagePath.lastIndexOf('.');
        return lastDot > 0 ? imagePath.substring(lastDot) : ".jpg";
    }
}