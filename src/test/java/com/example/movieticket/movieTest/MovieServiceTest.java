package com.example.movieticket.movieTest;


import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.movie.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Tests")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;
    private Movie updatedMovie;

    @BeforeEach
    void setUp() {
        testMovie = new Movie(
                1L,
                "The Matrix",
                "Sci-Fi",
                136,
                8.0,
                1999
        );

        updatedMovie = new Movie(
                1L,
                "The Matrix Reloaded",
                "Action",
                138,
                7.0,
                2003
        );
    }

    @Nested
    @DisplayName("addMovie() Tests")
    class AddMovieTests {

        @Test
        @DisplayName("Should successfully add a new movie")
        void shouldAddMovieWhenValidMovieProvided() {
            when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

            movieService.addMovie(testMovie);

            verify(movieRepository, times(1)).save(testMovie);
        }

        @Test
        @DisplayName("Should call repository save method with correct movie object")
        void shouldCallRepositorySaveWithCorrectMovieObject() {
            when(movieRepository.save(testMovie)).thenReturn(testMovie);

            movieService.addMovie(testMovie);

            verify(movieRepository).save(argThat(movie ->
                    movie.getTitle().equals("The Matrix") &&
                            movie.getGenre().equals("Sci-Fi") &&
                            movie.getDuration() == 136
            ));
        }
    }

    @Nested
    @DisplayName("getMovies() Tests")
    class GetMoviesTests {

        @Test
        @DisplayName("Should return list of all movies")
        void shouldReturnAllMoviesWhenMoviesExist() {
            Movie movie2 = new Movie(2L, "Inception", "Thriller", 148, 8.0, 2010);
            List<Movie> expectedMovies = Arrays.asList(testMovie, movie2);
            when(movieRepository.findAll()).thenReturn(expectedMovies);

            List<Movie> actualMovies = movieService.getMovies();

            assertThat(actualMovies)
                    .isNotNull()
                    .hasSize(2)
                    .containsExactlyElementsOf(expectedMovies);
            verify(movieRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no movies exist")
        void shouldReturnEmptyListWhenNoMoviesExist() {
            when(movieRepository.findAll()).thenReturn(List.of());

            List<Movie> actualMovies = movieService.getMovies();

            assertThat(actualMovies).isEmpty();
            verify(movieRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getMovieByTitle() Tests")
    class GetMovieByIdTests {

        @Test
        @DisplayName("Should return movie when valid title is provided")
        void shouldReturnMovieWhenValidTitleProvided() {
            when(movieRepository.findByTitle("The Matrix")).thenReturn(Optional.of(testMovie));

            Movie actualMovie = movieService.getMovieByTitle("The Matrix");

            assertThat(actualMovie)
                    .isNotNull()
                    .isEqualTo(testMovie);
            assertThat(actualMovie.getTitle()).isEqualTo("The Matrix");
            verify(movieRepository, times(1)).findByTitle("The Matrix");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when movie not found")
        void shouldThrowResourceNotFoundExceptionWhenMovieNotFound() {
            String nonExistentTitle = "NOT EXISTING MOVIE";
            when(movieRepository.findByTitle(nonExistentTitle)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.getMovieByTitle(nonExistentTitle))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Movie with title " + nonExistentTitle + " does not exist.");
            verify(movieRepository, times(1)).findByTitle(nonExistentTitle);
        }
    }

    @Nested
    @DisplayName("updateMovie() Tests")
    class UpdateMovieTests {

        @Test
        @DisplayName("Should update movie successfully when valid title and details provided")
        void shouldUpdateMovieWhenValidTitleAndDetailsProvided() {
            when(movieRepository.findByTitle("The Matrix")).thenReturn(Optional.of(testMovie));
            when(movieRepository.save(any(Movie.class))).thenReturn(updatedMovie);

            Movie result = movieService.updateMovie("The Matrix", updatedMovie);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("The Matrix Reloaded");
            verify(movieRepository, times(1)).findByTitle("The Matrix");
            verify(movieRepository, times(1)).save(any(Movie.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent movie")
        void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentMovie() {
            String nonExistentTitle = "NOT EXISTING MOVIE";
            when(movieRepository.findByTitle(nonExistentTitle)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.updateMovie(nonExistentTitle, updatedMovie))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Movie with title "+ nonExistentTitle +" does not exist.");
            verify(movieRepository, times(1)).findByTitle(nonExistentTitle);
            verify(movieRepository, never()).save(any(Movie.class));
        }

        @Test
        @DisplayName("Should create new Movie object with updated details")
        void shouldCreateNewMovieObjectWithUpdatedDetails() {
            when(movieRepository.findByTitle("The Matrix")).thenReturn(Optional.of(testMovie));
            when(movieRepository.save(any(Movie.class))).thenReturn(updatedMovie);

            movieService.updateMovie("The Matrix", updatedMovie);

            verify(movieRepository).save(argThat(movie ->
                    movie.getId().equals(1L) &&
                            movie.getTitle().equals("The Matrix Reloaded") &&
                            movie.getGenre().equals("Action") &&
                            movie.getDuration() == 138 &&
                            movie.getRating() == 7.0 &&
                            movie.getReleaseYear() == 2003
            ));
        }

    }

    @Nested
    @DisplayName("deleteMovie() Tests")
    class DeleteMovieTests {

        @Test
        @DisplayName("Should delete movie successfully when valid title provided")
        void shouldDeleteMovieWhenValidTitleProvided() {
            when(movieRepository.findByTitle("The Matrix")).thenReturn(Optional.of(testMovie));
            doNothing().when(movieRepository).delete(testMovie);

            movieService.deleteMovie("The Matrix");

            verify(movieRepository, times(1)).findByTitle("The Matrix");
            verify(movieRepository, times(1)).delete(testMovie);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent movie")
        void shouldThrowResourceNotFoundException_WhenDeletingNonExistentMovie() {
            String nonExistentTitle = "NOT EXISTING MOVIE";
            when(movieRepository.findByTitle(nonExistentTitle)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.deleteMovie(nonExistentTitle))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Movie with title "+ nonExistentTitle +" does not exist.");
            verify(movieRepository, times(1)).findByTitle(nonExistentTitle);
            verify(movieRepository, never()).delete(any(Movie.class));
        }
    }
}