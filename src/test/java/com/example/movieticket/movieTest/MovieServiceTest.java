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
import static org.mockito.ArgumentMatchers.anyLong;
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
                8,
                1999
        );

        updatedMovie = new Movie(
                1L,
                "The Matrix Reloaded",
                "Action",
                138,
                7,
                2003
        );
    }

    @Nested
    @DisplayName("addMovie() Tests")
    class AddMovieTests {

        @Test
        @DisplayName("Should successfully add a new movie")
        void shouldAddMovie_WhenValidMovieProvided() {
            when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

            movieService.addMovie(testMovie);

            verify(movieRepository, times(1)).save(testMovie);
        }

        @Test
        @DisplayName("Should call repository save method with correct movie object")
        void shouldCallRepositorySave_WithCorrectMovieObject() {
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
        void shouldReturnAllMovies_WhenMoviesExist() {
            Movie movie2 = new Movie(2L, "Inception", "Thriller", 148, 8, 2010);
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
        void shouldReturnEmptyList_WhenNoMoviesExist() {
            when(movieRepository.findAll()).thenReturn(List.of());

            List<Movie> actualMovies = movieService.getMovies();

            assertThat(actualMovies).isEmpty();
            verify(movieRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getMovieById() Tests")
    class GetMovieByIdTests {

        @Test
        @DisplayName("Should return movie when valid id is provided")
        void shouldReturnMovie_WhenValidIdProvided() {
            when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));

            Movie actualMovie = movieService.getMovieById(1L);

            assertThat(actualMovie)
                    .isNotNull()
                    .isEqualTo(testMovie);
            assertThat(actualMovie.getTitle()).isEqualTo("The Matrix");
            verify(movieRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when movie not found")
        void shouldThrowResourceNotFoundException_WhenMovieNotFound() {
            Long nonExistentId = 999L;
            when(movieRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.getMovieById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Movie with id " + nonExistentId + " does not exist.");
            verify(movieRepository, times(1)).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("updateMovie() Tests")
    class UpdateMovieTests {

        @Test
        @DisplayName("Should update movie successfully when valid id and details provided")
        void shouldUpdateMovie_WhenValidIdAndDetailsProvided() {
            when(movieRepository.existsById(1L)).thenReturn(true);
            when(movieRepository.save(any(Movie.class))).thenReturn(updatedMovie);

            Movie result = movieService.updateMovie(1L, updatedMovie);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("The Matrix Reloaded");
            verify(movieRepository, times(1)).existsById(1L);
            verify(movieRepository, times(1)).save(any(Movie.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent movie")
        void shouldThrowResourceNotFoundException_WhenUpdatingNonExistentMovie() {
            Long nonExistentId = 999L;
            when(movieRepository.existsById(nonExistentId)).thenReturn(false);

            assertThatThrownBy(() -> movieService.updateMovie(nonExistentId, updatedMovie))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Movie with id " + nonExistentId + " does not exist.");
            verify(movieRepository, times(1)).existsById(nonExistentId);
            verify(movieRepository, never()).save(any(Movie.class));
        }

        @Test
        @DisplayName("Should create new Movie object with updated details")
        void shouldCreateNewMovieObject_WithUpdatedDetails() {

            when(movieRepository.existsById(1L)).thenReturn(true);
            when(movieRepository.save(any(Movie.class))).thenReturn(updatedMovie);

            movieService.updateMovie(1L, updatedMovie);

            verify(movieRepository).save(argThat(movie ->
                    movie.getId().equals(1L) &&
                            movie.getTitle().equals("The Matrix Reloaded") &&
                            movie.getGenre().equals("Action") &&
                            movie.getDuration() == 138 &&
                            movie.getRating() == 7 &&
                            movie.getReleaseYear() == 2003
            ));
        }
    }

    @Nested
    @DisplayName("deleteMovie() Tests")
    class DeleteMovieTests {

        @Test
        @DisplayName("Should delete movie successfully when valid id provided")
        void shouldDeleteMovie_WhenValidIdProvided() {
            when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
            doNothing().when(movieRepository).delete(testMovie);

            movieService.deleteMovie(1L);


            verify(movieRepository, times(1)).findById(1L);
            verify(movieRepository, times(1)).delete(testMovie);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent movie")
        void shouldThrowResourceNotFoundException_WhenDeletingNonExistentMovie() {
            Long nonExistentId = 999L;
            when(movieRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.deleteMovie(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Movie with id " + nonExistentId + " does not exist.");
            verify(movieRepository, times(1)).findById(nonExistentId);
            verify(movieRepository, never()).delete(any(Movie.class));
        }
    }

    @Nested
    @DisplayName("validateMovieExists() Tests")
    class ValidateMovieExistsTests {

        @Test
        @DisplayName("Should not throw exception when movie exists")
        void shouldNotThrowException_WhenMovieExists() {
            when(movieRepository.existsById(1L)).thenReturn(true);

            assertThatCode(() -> movieService.validateMovieExists(1L))
                    .doesNotThrowAnyException();
            verify(movieRepository, times(1)).existsById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when movie does not exist")
        void shouldThrowResourceNotFoundException_WhenMovieDoesNotExist() {
            Long nonExistentId = 999L;
            when(movieRepository.existsById(nonExistentId)).thenReturn(false);

            assertThatThrownBy(() -> movieService.validateMovieExists(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Movie with id " + nonExistentId + " does not exist.");
            verify(movieRepository, times(1)).existsById(nonExistentId);
        }
    }
}