package com.example.moviewatchlist.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ImageDownloadService using JUnit and Mockito.
 * Tests image download functionality in isolation.
 */
@ExtendWith(MockitoExtension.class)
class ImageDownloadServiceTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<byte[]> mockResponse;

    @TempDir
    Path tempDir;

    private ImageDownloadService imageDownloadService;

    @BeforeEach
    void setUp() {
        imageDownloadService = new ImageDownloadService();
        ReflectionTestUtils.setField(imageDownloadService, "httpClient", mockHttpClient);
        ReflectionTestUtils.setField(imageDownloadService, "imagesPath", tempDir.toString() + "/");
    }

    /**
     * Tests successful download of multiple images.
     */
    @Test
    void testDownloadImages_Success() throws Exception {
        // Arrange
        List<String> imagePaths = Arrays.asList("/poster1.jpg", "/poster2.jpg", "/backdrop.jpg");
        String movieTitle = "Inception";
        byte[] fakeImageData = "fake image data".getBytes();

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(fakeImageData);
        when(mockHttpClient.<byte[]>sendAsync(
                any(HttpRequest.class),
                anyBodyHandler()
        )).thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        CompletableFuture<List<String>> future = imageDownloadService.downloadImages(imagePaths, movieTitle);
        List<String> downloadedPaths = future.join();

        // Assert
        assertNotNull(downloadedPaths);
        assertEquals(3, downloadedPaths.size());
        
        // Verify files were created
        for (String path : downloadedPaths) {
            assertTrue(Files.exists(Path.of(path)));
        }
        
        // Verify HTTP client was called 3 times
        verify(mockHttpClient, times(3)).sendAsync(any(HttpRequest.class), anyBodyHandler());
    }

    /**
     * Tests handling of failed image downloads (404 response).
     */
    @Test
    void testDownloadImages_FailedDownload() throws Exception {
        // Arrange
        List<String> imagePaths = Arrays.asList("/notfound.jpg");
        String movieTitle = "Test Movie";

        when(mockResponse.statusCode()).thenReturn(404);
        when(mockHttpClient.<byte[]>sendAsync(
                any(HttpRequest.class),
                anyBodyHandler()
        )).thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        CompletableFuture<List<String>> future = imageDownloadService.downloadImages(imagePaths, movieTitle);
        List<String> downloadedPaths = future.join();

        // Assert
        assertNotNull(downloadedPaths);
        assertTrue(downloadedPaths.isEmpty());
        verify(mockHttpClient).sendAsync(any(HttpRequest.class), anyBodyHandler());
    }

    /**
     * Tests downloading with empty image paths list.
     */
    @Test
    void testDownloadImages_EmptyList() {
        // Arrange
        List<String> imagePaths = Arrays.asList();
        String movieTitle = "Empty Movie";

        // Act
        CompletableFuture<List<String>> future = imageDownloadService.downloadImages(imagePaths, movieTitle);
        List<String> downloadedPaths = future.join();

        // Assert
        assertNotNull(downloadedPaths);
        assertTrue(downloadedPaths.isEmpty());
        verifyNoInteractions(mockHttpClient);
    }

    /**
     * Tests that only first 3 images are downloaded when more are provided.
     */
    @Test
    void testDownloadImages_LimitsToThreeImages() throws Exception {
        // Arrange
        List<String> imagePaths = Arrays.asList("/img1.jpg", "/img2.jpg", "/img3.jpg", "/img4.jpg", "/img5.jpg");
        String movieTitle = "Many Images Movie";
        byte[] fakeImageData = "fake image".getBytes();

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(fakeImageData);
        when(mockHttpClient.<byte[]>sendAsync(
                any(HttpRequest.class),
                anyBodyHandler()
        )).thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        CompletableFuture<List<String>> future = imageDownloadService.downloadImages(imagePaths, movieTitle);
        List<String> downloadedPaths = future.join();

        // Assert
        assertEquals(3, downloadedPaths.size());
        verify(mockHttpClient, times(3)).sendAsync(any(HttpRequest.class), anyBodyHandler());
    }

    /**
     * Tests filename sanitization for special characters.
     */
    @Test
    void testDownloadImages_SanitizesFilename() throws Exception {
        // Arrange
        List<String> imagePaths = Arrays.asList("/poster.jpg");
        String movieTitle = "Movie: With Special/Characters?";
        byte[] fakeImageData = "image".getBytes();

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(fakeImageData);
        when(mockHttpClient.<byte[]>sendAsync(
                any(HttpRequest.class),
                anyBodyHandler()
        )).thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        CompletableFuture<List<String>> future = imageDownloadService.downloadImages(imagePaths, movieTitle);
        List<String> downloadedPaths = future.join();

        // Assert
        assertEquals(1, downloadedPaths.size());
        String downloadedPath = downloadedPaths.get(0);
        String fileName = Path.of(downloadedPath).getFileName().toString();
        assertFalse(fileName.contains(":"));
        assertFalse(fileName.contains("/"));
        assertFalse(fileName.contains("?"));
        assertTrue(fileName.contains("Movie_"));
    }

    /**
     * Tests handling of HTTP client throwing exception.
     */
    @Test
    void testDownloadImages_HttpClientException() {
        // Arrange
        List<String> imagePaths = Arrays.asList("/error.jpg");
        String movieTitle = "Error Movie";

        when(mockHttpClient.<byte[]>sendAsync(
                any(HttpRequest.class),
                anyBodyHandler()
        )).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Network error")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            imageDownloadService.downloadImages(imagePaths, movieTitle).join();
        });
    }

    // Helper method for type-safe BodyHandler
    @SuppressWarnings("unchecked")
    private static BodyHandler<byte[]> anyBodyHandler() {
        return (BodyHandler<byte[]>) any(BodyHandler.class);
    }
}