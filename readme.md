Movie Watchlist Backend Application

A Spring Boot application that allows users to create and manage a movie watchlist by integrating with OMDb and TMDB APIs.


Features-

Add movies: Search and add movies to your watchlist using movie titles
Rich movie data: Combines data from OMDb (basic info) and TMDB (images, similar movies)
Image downloads: Automatically downloads and saves movie posters/backdrops
Watchlist management: Mark movies as watched, rate them, and delete them
Pagination: Efficiently browse large collections of movies
RESTful API: Clean REST endpoints for all operations

Architecture Overview
User Request → Controller → Service → External APIs (OMDb/TMDB) → Database
                    ↓
              Image Download Service → File System


Project Structure
src/
├── main/
│   ├── java/com/example/moviewatchlist/
│   │   ├── MovieWatchlistApplication.java     # Main application class
│   │   ├── config/
│   │   │   └── AsyncConfig.java               # Async configuration
│   │   ├── controller/
│   │   │   ├── MovieController.java           # REST endpoints
│   │   │   └── GlobalExceptionHandler.java    # Error handling
│   │   ├── dto/                               # Data Transfer Objects
│   │   │   ├── MovieResponse.java
│   │   │   ├── OMDbResponse.java
│   │   │   ├── TMDbSearchResponse.java
│   │   │   ├── TMDbImagesResponse.java
│   │   │   ├── TMDbSimilarResponse.java
│   │   │   └── PaginatedResponse.java
│   │   ├── model/
│   │   │   └── Movie.java                     # JPA Entity
│   │   ├── repository/
│   │   │   └── MovieRepository.java           # Data access layer
│   │   └── service/
│   │       ├── MovieService.java              # Main business logic
│   │       ├── OMDbService.java               # OMDb API integration
│   │       ├── TMDbService.java               # TMDB API integration
│   │       └── ImageDownloadService.java      # Image handling
│   └── resources/
│       └── application.properties             # Configuration
└── test/                                      # Unit tests


requirements:

Configure API Keys
set OMDB_API_KEY=your_omdb_key_here
set TMDB_API_KEY=your_tmdb_key_here

