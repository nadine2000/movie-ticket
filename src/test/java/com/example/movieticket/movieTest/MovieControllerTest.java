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
    @DisplayName("POST /movies")
    class AddMovieTests {
        @Test
        void shouldAddMovieReturn200() throws Exception {
            doNothing().when(movieService).addMovie(any(Movie.class));

            mockMvc.perform(post("/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testMovie)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("The Matrix"))
                    .andExpect(jsonPath("$.genre").value("Sci-Fi"))
                    .andExpect(jsonPath("$.duration").value(136))
                    .andExpect(jsonPath("$.rating").value(8.0))
                    .andExpect(jsonPath("$.releaseYear").value(1999));

            verify(movieService).addMovie(any(Movie.class));
        }

        @Test
        @DisplayName("POST /movies - should return 400 Bad Request when invalid data is sent")
        void shouldReturnBadRequestWhenInvalidMovieData() throws Exception {
            Movie invalidMovie = new Movie(null, "", "", -10, 15.0, 1800);

            mockMvc.perform(post("/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidMovie)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message.title").exists())
                    .andExpect(jsonPath("$.message.genre").exists())
                    .andExpect(jsonPath("$.message.duration").exists())
                    .andExpect(jsonPath("$.message.rating").exists())
                    .andExpect(jsonPath("$.message.releaseYear").exists())
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.status").value(400));

        }
    }

    @Nested
    @DisplayName("GET /movies/all")
    class GetMoviesTests {

        @Test
        void shouldReturnAllMovies() throws Exception {
            Movie movie2 = new Movie(2L, "Inception", "Thriller", 148, 8.0, 2010);
            when(movieService.getMovies()).thenReturn(Arrays.asList(testMovie, movie2));

            mockMvc.perform(get("/movies/all"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(movieService).getMovies();
        }

        @Test
        void shouldReturnEmptyList() throws Exception {
            when(movieService.getMovies()).thenReturn(List.of());

            mockMvc.perform(get("/movies/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(movieService).getMovies();
        }
    }

    @Nested
    @DisplayName("PUT /movies/update/{movieTitle}")
    class UpdateMovieTests {

        @Test
        void shouldUpdateMovie() throws Exception {
            when(movieService.updateMovie(eq("The Matrix"), any(Movie.class)))
                    .thenReturn(updatedMovie);

            mockMvc.perform(put("/movies/update/The Matrix")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedMovie)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", is("The Matrix Reloaded")))
                    .andExpect(jsonPath("$.rating", is(7.0)));

            verify(movieService).updateMovie(eq("The Matrix"), any(Movie.class));
        }

        @Test
        void shouldReturn404WhenMovieNotFound() throws Exception {
            when(movieService.updateMovie(eq("NonExistent"), any(Movie.class)))
                    .thenThrow(new ResourceNotFoundException("not found"));

            mockMvc.perform(put("/movies/update/NonExistent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedMovie)))
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    @DisplayName("DELETE /movies/{movieTitle}")
    class DeleteMovieTests {

        @Test
        void shouldDeleteMovie() throws Exception {
            doNothing().when(movieService).deleteMovie("The Matrix");

            mockMvc.perform(delete("/movies/The Matrix"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("Movie with title The Matrix was deleted successfully."));

            verify(movieService).deleteMovie("The Matrix");
        }

        @Test
        void shouldReturn404WhenDeletingNonExistentMovie() throws Exception {
            doThrow(new ResourceNotFoundException("not found"))
                    .when(movieService).deleteMovie("NonExistent");

            mockMvc.perform(delete("/movies/NonExistent"))
                    .andExpect(status().isNotFound());
        }
    }
}
