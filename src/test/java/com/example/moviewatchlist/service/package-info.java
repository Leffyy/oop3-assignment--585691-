/**
 * Service layer of the Movie Watchlist application.
 * 
 * <p>This package contains business logic and external API integrations.
 * Services are designed to be stateless and use async operations where appropriate.</p>
 * 
 * <h2>Main Services:</h2>
 * <ul>
 *   <li>{@link com.example.moviewatchlist.service.MovieService} - Core business logic for movie operations</li>
 *   <li>{@link com.example.moviewatchlist.service.OMDbService} - Integration with OMDb API for movie data</li>
 *   <li>{@link com.example.moviewatchlist.service.TMDbService} - Integration with TMDb API for images and similar movies</li>
 *   <li>{@link com.example.moviewatchlist.service.ImageDownloadService} - Handles async image downloads and storage</li>
 * </ul>
 * 
 * <h2>Design Patterns Used:</h2>
 * <ul>
 *   <li>Service Layer Pattern - Encapsulates business logic</li>
 *   <li>Async/Await Pattern - Uses CompletableFuture for non-blocking operations</li>
 *   <li>Repository Pattern - Data access abstraction via Spring Data JPA</li>
 * </ul>
 * 
 * @since 1.0
 * @author Movie Watchlist Team
 */
package com.example.moviewatchlist.service;