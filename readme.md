<!-- Movie Watchlist Backend Application -->
This application allows users to build and manage their movie watchlist 


A Spring Boot application that allows users to create and manage a movie watchlist by integrating with OMDb and TMDB APIs.


<!-- Features: -->

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

<!-- API Endpoints: -->

POST /api/movies - Create movie
GET /api/movies - List with pagination
GET /api/movies/{id} - Get specific movie
PATCH /api/movies/{id}/rating - Update rating
PATCH /api/movies/{id}/watched - Update watched status
DELETE /api/movies/{id} - Delete movie

<!-- Technology Stack -->

- **Framework**: Spring Boot 3.3.0
- **Language**: Java 17
- **Database**: H2 (in-memory for development)
- **Build Tool**: Maven
- **External APIs**: OMDb API, The Movie Database (TMDb) API
- **Architecture**: RESTful web services with async processing

<!-- requirements: -->

Configure API Keys
set OMDB_API_KEY=your_omdb_key_here
set TMDB_API_KEY=your_tmdb_key_here

<!-- Running the Application -->

# Clone the repository
git clone <repository-url>
cd movie-watchlist

# Run with Maven
mvn clean spring-boot:run

# The application will start on http://localhost:8080


<!-- Database access: -->

http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (leave blank) 