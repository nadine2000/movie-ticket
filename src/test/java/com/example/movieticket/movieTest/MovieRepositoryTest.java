package com.example.movieticket.movieTest;

import com.example.movieticket.movie.Movie;
import com.example.movieticket.movie.MovieRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@DisplayName("Movie Repository Tests")
class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    private Movie defaultMovie;

    @BeforeEach
    void setUp() {
        defaultMovie = createMovie("Test Movie", "Action", 120, 5.0, 2023);
    }

    private Movie createMovie(String title, String genre, int duration, double rating, int releaseYear) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setGenre(genre);
        movie.setDuration(duration);
        movie.setRating(rating);
        movie.setReleaseYear(releaseYear);
        return movie;
    }

    @Test
    @DisplayName("Should save a movie to database and generate ID")
    void testSaveMovie() {
        Movie savedMovie = movieRepository.save(defaultMovie);

        assertThat(savedMovie.getId()).isNotNull();
        assertThat(savedMovie.getTitle()).isEqualTo("Test Movie");
        assertThat(savedMovie.getGenre()).isEqualTo("Action");
        assertThat(savedMovie.getDuration()).isEqualTo(120);
        assertThat(savedMovie.getRating()).isEqualTo(5.0);
        assertThat(savedMovie.getReleaseYear()).isEqualTo(2023);
    }

    @Test
    @DisplayName("Should find a movie by Title")
    void testFindMovieById() {
        Movie savedMovie = movieRepository.save(defaultMovie);

        Optional<Movie> found = movieRepository.findByTitle(savedMovie.getTitle());

        assertThat(found).isPresent().contains(savedMovie);
    }

    @Test
    @DisplayName("Should return empty Optional for non-existent movie")
    void testFindMovieByIdNotFound() {
        assertThat(movieRepository.findByTitle("NOT FOUND")).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve all movies")
    void testFindAllMovies() {
        Movie movie1 = createMovie("Movie 1", "Action", 100, 7.0, 2020);
        Movie movie2 = createMovie("Movie 2", "Drama", 110, 8.0, 2021);

        movieRepository.saveAll(List.of(movie1, movie2));

        List<Movie> allMovies = movieRepository.findAll();

        assertThat(allMovies).hasSize(2)
                .extracting(Movie::getTitle)
                .containsExactlyInAnyOrder("Movie 1", "Movie 2");
    }

    @Test
    @DisplayName("Should update a movie")
    void testUpdateMovie() {
        Movie savedMovie = movieRepository.save(defaultMovie);

        savedMovie.setTitle("Updated Title");
        savedMovie.setRating(9.0);

        Movie updated = movieRepository.save(savedMovie);

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getRating()).isEqualTo(9);
    }

    @Test
    @DisplayName("Should delete a movie")
    void testDeleteMovie() {
        Movie savedMovie = movieRepository.save(defaultMovie);
        String title = savedMovie.getTitle();

        movieRepository.delete(savedMovie);

        assertThat(movieRepository.findByTitle(title)).isEmpty();
    }

    @Test
    @DisplayName("Should handle invalid movie data")
    void testInvalidMovie() {
        Movie invalid = createMovie(null, "Action", -10, -1, 1800);
        assertThatThrownBy(() -> movieRepository.saveAndFlush(invalid))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
