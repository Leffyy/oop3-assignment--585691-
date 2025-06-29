/**
 * Data Transfer Objects (DTOs) for the Movie Watchlist application.
 *
 * <p>This package contains simple Java classes used to transfer data between
 * the backend, external APIs (OMDb and TMDb), and the client. DTOs are used for:
 * <ul>
 *   <li>Representing movie data sent to and from the client (e.g., {@code MovieResponse})</li>
 *   <li>Mapping responses from OMDb and TMDb APIs (e.g., {@code OMDbResponse}, {@code TMDbSearchResponse}, {@code TMDbImagesResponse}, {@code TMDbSimilarResponse})</li>
 *   <li>Wrapping paginated results and related movie information (e.g., {@code PaginatedResponse})</li>
 * </ul>
 *
 * @since 1.0
 */
package com.example.moviewatchlist.dto;