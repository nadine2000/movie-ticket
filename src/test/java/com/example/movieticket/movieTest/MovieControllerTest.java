package com.example.movieticket.movieTest;

import com.example.movieticket.exception.GlobalExceptionHandler;
import com.example.movieticket.movie.*;
import com.example.movieticket.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieController Tests")
class MovieControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    private Movie testMovie;
    private Movie updatedMovie;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(movieController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

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
    @DisplayName("POST /movies")
    class AddMovieTests {

        @Test
        void shouldAddMovie_Return201() throws Exception {
            doNothing().when(movieService).addMovie(any(Movie.class));

            mockMvc.perform(post("/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testMovie)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().string("Movie was added successfully."));

            verify(movieService).addMovie(any(Movie.class));
        }

        @Test
        void shouldCallServiceWithCorrectMovieData() throws Exception {
            doNothing().when(movieService).addMovie(any(Movie.class));

            mockMvc.perform(post("/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testMovie)))
                    .andExpect(status().isCreated());

            verify(movieService).addMovie(argThat(movie ->
                    movie.getTitle().equals("The Matrix") &&
                            movie.getGenre().equals("Sci-Fi") &&
                            movie.getDuration() == 136 &&
                            movie.getRating() == 8 &&
                            movie.getReleaseYear() == 1999
            ));
        }
    }

    @Nested
    @DisplayName("GET /movies")
    class GetMoviesTests {

        @Test
        void shouldReturnAllMovies() throws Exception {
            Movie movie2 = new Movie(2L, "Inception", "Thriller", 148, 8, 2010);
            when(movieService.getMovies()).thenReturn(Arrays.asList(testMovie, movie2));

            mockMvc.perform(get("/movies"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(movieService).getMovies();
        }

        @Test
        void shouldReturnEmptyList() throws Exception {
            when(movieService.getMovies()).thenReturn(List.of());

            mockMvc.perform(get("/movies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(movieService).getMovies();
        }
    }

    @Nested
    @DisplayName("PUT /movies/{id}")
    class UpdateMovieTests {

        @Test
        void shouldUpdateMovie() throws Exception {
            when(movieService.updateMovie(eq(1L), any(Movie.class)))
                    .thenReturn(updatedMovie);

            mockMvc.perform(put("/movies/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedMovie)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", is("The Matrix Reloaded")))
                    .andExpect(jsonPath("$.rating", is(7)));

            verify(movieService).updateMovie(eq(1L), any(Movie.class));
        }

        @Test
        void shouldReturn404_WhenMovieNotFound() throws Exception {
            when(movieService.updateMovie(eq(999L), any(Movie.class)))
                    .thenThrow(new ResourceNotFoundException("not found"));

            mockMvc.perform(put("/movies/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedMovie)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /movies/{id}")
    class DeleteMovieTests {

        @Test
        void shouldDeleteMovie() throws Exception {
            doNothing().when(movieService).deleteMovie(1L);

            mockMvc.perform(delete("/movies/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("Movie with id 1 was deleted successfully."));

            verify(movieService).deleteMovie(1L);
        }

        @Test
        void shouldReturn404_WhenDeletingNonExistentMovie() throws Exception {
            doThrow(new ResourceNotFoundException("not found"))
                    .when(movieService).deleteMovie(999L);

            mockMvc.perform(delete("/movies/999"))
                    .andExpect(status().isNotFound());
        }
    }
}
