package com.example.moviewatchlist.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

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

    /**
     * Tests creation of images directory if it doesn't exist.
     */
    @Test
    void ensureImagesDirectoryExists_createsDirectory() throws Exception {
        ImageDownloadService service = new ImageDownloadService();
        ReflectionTestUtils.setField(service, "imagesPath", "test-images");

        Path dir = Path.of("test-images");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(dir)).thenReturn(false);
            filesMock.when(() -> Files.createDirectories(dir)).thenReturn(dir);

            // Call private method via reflection or public method that triggers it
            Method m = ImageDownloadService.class.getDeclaredMethod("ensureImagesDirectoryExists");
            m.setAccessible(true);
            m.invoke(service);

            filesMock.verify(() -> Files.createDirectories(dir));
        }
    }

    /**
     * Tests that an exception is thrown if directory creation fails.
     */
    @Test
    void ensureImagesDirectoryExists_throwsRuntimeExceptionOnIOException() throws Exception {
        ImageDownloadService service = new ImageDownloadService();
        ReflectionTestUtils.setField(service, "imagesPath", "test-images");

        Path dir = Path.of("test-images");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(dir)).thenReturn(false);
            filesMock.when(() -> Files.createDirectories(dir)).thenThrow(new IOException("fail"));

            Method m = ImageDownloadService.class.getDeclaredMethod("ensureImagesDirectoryExists");
            m.setAccessible(true);

            InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> m.invoke(service));
            Throwable real = ex.getCause();
            assertTrue(real instanceof RuntimeException);
            assertTrue(real.getCause() instanceof IOException);
            assertEquals("Failed to create images directory", real.getMessage());
        }
    }

    /**
     * Tests that downloadImage returns null when an IOException occurs.
     */
    @Test
    @SuppressWarnings("unchecked")
    void downloadImage_returnsNullOnIOException() throws Exception {
        ImageDownloadService service = new ImageDownloadService();
        ReflectionTestUtils.setField(service, "imagesPath", "test-images");

        String imageUrl = "https://image.tmdb.org/t/p/w780/test.jpg";
        String localPath = "test-images/test.jpg";

        // Mock HttpClient and response
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<byte[]> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(new byte[]{1, 2, 3});
        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        ReflectionTestUtils.setField(service, "httpClient", mockClient);

        // Mock Files.write to throw IOException
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.write(any(Path.class), any(byte[].class)))
                .thenThrow(new IOException("disk full"));

            Method m = ImageDownloadService.class.getDeclaredMethod("downloadImage", String.class, String.class);
            m.setAccessible(true);
            CompletableFuture<String> future = (CompletableFuture<String>) m.invoke(service, imageUrl, localPath);
            String result = future.join();
            assertNull(result);
        }
    }

    /**
     * Tests getting file extension from image path.
     */
    @Test
    void getFileExtension_returnsExtensionOrDefault() throws Exception {
        ImageDownloadService service = new ImageDownloadService();

        Method m = ImageDownloadService.class.getDeclaredMethod("getFileExtension", String.class);
        m.setAccessible(true);

        // Case 1: imagePath has extension
        String ext1 = (String) m.invoke(service, "poster.png");
        assertEquals(".png", ext1);

        // Case 2: imagePath has no extension
        String ext2 = (String) m.invoke(service, "poster");
        assertEquals(".jpg", ext2);
    }

    // Helper method for type-safe BodyHandler
    @SuppressWarnings("unchecked")
    private static BodyHandler<byte[]> anyBodyHandler() {
        return (BodyHandler<byte[]>) any(BodyHandler.class);
    }
}