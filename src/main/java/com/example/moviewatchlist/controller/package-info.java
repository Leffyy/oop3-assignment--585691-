/**
 * Controller layer of the Movie Watchlist application.
 * 
 * <p>This package contains REST controllers that handle HTTP requests and responses.
 * All controllers follow the Spring MVC pattern and use proper HTTP status codes.</p>
 * 
 * <h2>Main Components:</h2>
 * <ul>
 *   <li>{@link com.example.moviewatchlist.controller.MovieController} - Handles all movie-related endpoints</li>
 *   <li>{@link com.example.moviewatchlist.controller.GlobalExceptionHandler} - Centralized exception handling</li>
 * </ul>
 * 
 * <h2>API Endpoints:</h2>
 * <ul>
 *   <li>POST /api/movies - Add a new movie to the watchlist</li>
 *   <li>GET /api/movies - Get paginated list of movies</li>
 *   <li>GET /api/movies/{id} - Get specific movie details</li>
 *   <li>PATCH /api/movies/{id}/rating - Update movie rating</li>
 *   <li>PATCH /api/movies/{id}/watched - Update watched status</li>
 *   <li>DELETE /api/movies/{id} - Remove movie from watchlist</li>
 * </ul>
 *  he
 * @since 1.0
 * @author Movie Watchlist Team
 */
package com.example.moviewatchlist.controller;
