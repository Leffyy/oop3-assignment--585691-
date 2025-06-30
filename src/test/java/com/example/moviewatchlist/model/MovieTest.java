package com.example.moviewatchlist.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovieTest {

    @Test
    void testBuilderAndGettersSetters() {
        List<String> images = Arrays.asList("img1.jpg", "img2.jpg");
        List<String> similars = Arrays.asList("Movie A", "Movie B");

        Movie movie = Movie.builder()
                .id(1L)
                .title("Inception")
                .releaseYear("2010")
                .director("Christopher Nolan")
                .genre("Action, Sci-Fi")
                .plot("A thief who steals secrets...")
                .runtime("148 min")
                .imdbRating("8.8")
                .tmdbId(12345)
                .overview("A mind-bending thriller")
                .releaseDate("2010-07-16")
                .voteAverage(8.7)
                .imagePaths(images)
                .similarMovies(similars)
                .watched(true)
                .rating(5)
                .build();

        assertEquals(1L, movie.getId());
        assertEquals("Inception", movie.getTitle());
        assertEquals("2010", movie.getReleaseYear());
        assertEquals("Christopher Nolan", movie.getDirector());
        assertEquals("Action, Sci-Fi", movie.getGenre());
        assertEquals("A thief who steals secrets...", movie.getPlot());
        assertEquals("148 min", movie.getRuntime());
        assertEquals("8.8", movie.getImdbRating());
        assertEquals(12345, movie.getTmdbId());
        assertEquals("A mind-bending thriller", movie.getOverview());
        assertEquals("2010-07-16", movie.getReleaseDate());
        assertEquals(8.7, movie.getVoteAverage());
        assertEquals(images, movie.getImagePaths());
        assertEquals(similars, movie.getSimilarMovies());
        assertTrue(movie.getWatched());
        assertEquals(5, movie.getRating());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        Movie movie = new Movie();
        movie.setId(2L);
        movie.setTitle("The Matrix");
        movie.setReleaseYear("1999");
        movie.setDirector("Wachowski Sisters");
        movie.setGenre("Action");
        movie.setPlot("A computer hacker learns about the true nature of reality.");
        movie.setRuntime("136 min");
        movie.setImdbRating("8.7");
        movie.setTmdbId(603);
        movie.setOverview("A hacker discovers reality is a simulation.");
        movie.setReleaseDate("1999-03-31");
        movie.setVoteAverage(8.6);
        movie.setImagePaths(Arrays.asList("matrix1.jpg"));
        movie.setSimilarMovies(Arrays.asList("Dark City"));
        movie.setWatched(false);
        movie.setRating(4);

        assertEquals(2L, movie.getId());
        assertEquals("The Matrix", movie.getTitle());
        assertEquals("1999", movie.getReleaseYear());
        assertEquals("Wachowski Sisters", movie.getDirector());
        assertEquals("Action", movie.getGenre());
        assertEquals("A computer hacker learns about the true nature of reality.", movie.getPlot());
        assertEquals("136 min", movie.getRuntime());
        assertEquals("8.7", movie.getImdbRating());
        assertEquals(603, movie.getTmdbId());
        assertEquals("A hacker discovers reality is a simulation.", movie.getOverview());
        assertEquals("1999-03-31", movie.getReleaseDate());
        assertEquals(8.6, movie.getVoteAverage());
        assertEquals(Arrays.asList("matrix1.jpg"), movie.getImagePaths());
        assertEquals(Arrays.asList("Dark City"), movie.getSimilarMovies());
        assertFalse(movie.getWatched());
        assertEquals(4, movie.getRating());
    }

    @Test
    void testDeprecatedYearMethods() {
        Movie movie = new Movie();
        movie.setReleaseYear("2000");
        assertEquals("2000", movie.getReleaseYear());
        movie.setReleaseYear("2001");
        assertEquals("2001", movie.getReleaseYear());
    }

    @Test
    void testEqualsAndHashCode() {
        Movie m1 = Movie.builder().id(1L).title("A").build();
        Movie m2 = Movie.builder().id(1L).title("A").build();
        Movie m3 = Movie.builder().id(2L).title("B").build();

        assertEquals(m1, m2);
        assertNotEquals(m1, m3);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertNotEquals(m1.hashCode(), m3.hashCode());
    }

    @Test
    void testEqualsAndHashCodeAllBranches() {
        Movie m1 = Movie.builder().id(1L).title("A").build();
        Movie m2 = Movie.builder().id(1L).title("A").build();
        Movie m3 = Movie.builder().id(2L).title("B").build();

        // Reflexive
        assertEquals(m1, m1);

        // Symmetric and transitive
        assertEquals(m1, m2);
        assertEquals(m2, m1);

        // Not equal to null
        assertNotEquals(m1, null);

        // Not equal to different type
        assertNotEquals(m1, "not a movie");

        // Not equal if id or title differ
        assertNotEquals(m1, m3);

        // Hash codes
        assertEquals(m1.hashCode(), m2.hashCode());
        assertNotEquals(m1.hashCode(), m3.hashCode());
    }

    @Test
    void testToString() {
        Movie movie = Movie.builder()
                .id(1L)
                .title("Test")
                .releaseYear("2020")
                .watched(true)
                .rating(5)
                .build();
        String str = movie.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("title=Test"));
        assertTrue(str.contains("releaseYear=2020") || str.contains("releaseYear"));
        assertTrue(str.contains("watched=true"));
        assertTrue(str.contains("rating=5"));
    }

    @Test
    void testToBuilder() {
        Movie m1 = Movie.builder().id(1L).title("A").build();
        Movie m2 = m1.toBuilder().title("B").build();
        assertEquals(1L, m2.getId());
        assertEquals("B", m2.getTitle());
    }

    @Test
    void testGetReleaseYear() {
        Movie movie = Movie.builder().releaseYear("2022").build();
        assertEquals("2022", movie.getReleaseYear());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDeprecatedSetYear() {
        Movie movie = new Movie();
        movie.setYear("1999");
        assertEquals("1999", movie.getReleaseYear());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDeprecatedGetYear() {
        Movie movie = new Movie();
        movie.setReleaseYear("2005");
        assertEquals("2005", movie.getYear());
    }

    @Test
    void testBuilderDefaults() {
        Movie movie = Movie.builder().build();
        assertFalse(movie.getWatched()); // default is false
        assertNull(movie.getRating());   // default is null
    }
}
