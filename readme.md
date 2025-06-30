[![codecov](https://codecov.io/gh/Leffyy/oop3-assignment--585691-/branch/main/graph/badge.svg?token=2FJYZO688V)](https://codecov.io/gh/Leffyy/oop3-assignment--585691-)

![Codecov Icicle Graph](https://codecov.io/gh/Leffyy/oop3-assignment--585691-/graphs/icicle.svg?token=2FJYZO688V)
# Movie Watchlist Backend Application

This Spring Boot application allows users to build and manage a movie watchlist.  
It integrates with OMDb and TMDb APIs to provide rich movie data, images, and similar movie recommendations.

---

## Features

- **Add movies:** Search and add movies to your watchlist using movie titles.
- **Movie data:** Combines data from OMDb (basic info) and TMDb (images, similar movies).
- **Image downloads:** Automatically downloads and saves movie posters/backdrops.
- **Watchlist management:** Mark movies as watched, rate them, and delete them.
- **Pagination:**  Browse large collections of movies.
- **RESTful API:** Clean REST endpoints for all operations.
- **Async processing:** Operations for external API calls and image downloads.

---

## Architecture Overview

```
User Request
    ↓
Controller → Service → External APIs (OMDb/TMDb) → Database
    ↓
Image Download Service → File System
```

---

## API Endpoints

- `POST   /api/movies`                – Add a new movie to the watchlist
- `GET    /api/movies`                – Get paginated list of movies
- `GET    /api/movies/{id}`           – Get specific movie details
- `PATCH  /api/movies/{id}/rating`    – Update movie rating
- `PATCH  /api/movies/{id}/watched`   – Update watched status
- `DELETE /api/movies/{id}`           – Remove movie from watchlist
- `GET    /api/movies/search?query=`  – Search for movies by title

---

## Technology Stack

- **Framework:** Spring Boot 3.3.0
- **Language:** Java 17
- **Database:** H2 (in-memory for development)
- **Build Tool:** Maven
- **External APIs:** OMDb API, The Movie Database (TMDb) API
- **Testing:** JUnit 5, Mockito, MockMvc
- **Coverage:** JaCoCo 
- **Architecture:** RESTful web services with async processing

---

## Requirements

- Java 17+
- Maven 3.8+
- OMDb and TMDb API keys

---

## Database Access

- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: *(leave blank)*

---

## Running the Application

```sh
# Clone the repository
git clone <repository-url>
cd movie-watchlist

# Run with Maven
mvn clean spring-boot:run
```

The application will start on [http://localhost:8080](http://localhost:8080)

---

## Testing & Coverage

```sh
# Run all tests
mvn test
```

- Unit and integration tests are written with JUnit 5 and Mockito.
- Code coverage is measured with JaCoCo (**currently 100%**).
- Test reports are generated in `target/site/jacoco/index.html`.

---

## API Key Setup

**For development:**

Set your API keys as environment variables or in `src/main/resources/application.properties`:

```
OMDB_API_KEY=your_omdb_key_here
TMDB_API_KEY=your_tmdb_key_here
```

**For tests:**

You can add these keys to `src/test/resources/application.properties` for test runs:

```
OMDB_API_KEY=dummy
TMDB_API_KEY=dummy
```

---

## Project Structure

```
src/
  main/
    java/com/example/moviewatchlist/
      controller/   # REST controllers
      service/      # Business logic and API integrations
      dto/          # Data Transfer Objects
      model/        # JPA entities
      repository/   # Spring Data JPA repositories
      config/       # Spring configuration classes
    resources/
      application.properties
  test/
    java/com/example/moviewatchlist/
      ...           # Unit and integration tests
```

---

## Frontend

A sample frontend is provided in the `Frontend/` directory.  
Open `Frontend/index.html` in your browser and connect to the backend at `http://localhost:8080`.

---






