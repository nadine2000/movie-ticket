package com.example.movieticket.showtimeTest;

import com.example.movieticket.exception.GlobalExceptionHandler;
import com.example.movieticket.showtime.*;

import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShowtimeController Tests")
class ShowtimeControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ShowtimeService showtimeService;

    @InjectMocks
    private ShowtimeController showtimeController;

    private Showtime testShowtime;
    private Showtime updatedShowtime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(showtimeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        startTime = LocalDateTime.of(2024, 1, 15, 19, 0);
        endTime = LocalDateTime.of(2024, 1, 15, 21, 30);

        testShowtime = new Showtime(
                1L,
                1L,
                "Theater A",
                startTime,
                endTime,
                12.50F
        );

        updatedShowtime = new Showtime(
                1L,
                1L,
                "Theater B",
                LocalDateTime.of(2024, 1, 16, 20, 0),
                LocalDateTime.of(2024, 1, 16, 22, 30),
                15.00F
        );
    }

    @Nested
    @DisplayName("POST /showtime - Add New Showtime Tests")
    class AddNewShowtimeTests {

        @Test
        @DisplayName("Should add showtime and return 201 CREATED with success message")
        void shouldAddShowtime_AndReturn201Created() throws Exception {
            doNothing().when(showtimeService).addShowtime(any(Showtime.class));
             
            mockMvc.perform(post("/showtime")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testShowtime)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().string("Showtime was added successfully."));

            verify(showtimeService, times(1)).addShowtime(any(Showtime.class));
        }

        @Test
        @DisplayName("Should call service with correct showtime data")
        void shouldCallService_WithCorrectShowtimeData() throws Exception {
             doNothing().when(showtimeService).addShowtime(any(Showtime.class));

            // Act
            mockMvc.perform(post("/showtime")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testShowtime)))
                    .andExpect(status().isCreated());

            // Assert
            verify(showtimeService).addShowtime(argThat(showtime ->
                    showtime.getMovieId().equals(1L) &&
                            showtime.getTheater().equals("Theater A") &&
                            showtime.getPrice() == 12.50
            ));
        }

        @Test
        @DisplayName("Should throw ValidationException when showtime has invalid data")
        void shouldThrowValidationException_WhenShowtimeHasInvalidData() throws Exception {
             Showtime invalidShowtime = new Showtime(
                    null,
                    1L,
                    "Theater A",
                    LocalDateTime.of(2024, 1, 15, 22, 0),
                    LocalDateTime.of(2024, 1, 15, 20, 0), // End before start
                    12.50F
            );

            doThrow(new ValidationException("End time must be after start time"))
                    .when(showtimeService).addShowtime(any(Showtime.class));

             
            mockMvc.perform(post("/showtime")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidShowtime)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(showtimeService, times(1)).addShowtime(any(Showtime.class));
        }

        @Test
        @DisplayName("Should throw ValidationException when showtime overlaps")
        void shouldThrowValidationException_WhenShowtimeOverlaps() throws Exception {
             doThrow(new ValidationException("Showtime overlaps with another showtime in the same theater"))
                    .when(showtimeService).addShowtime(any(Showtime.class));

             
            mockMvc.perform(post("/showtime")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testShowtime)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(showtimeService, times(1)).addShowtime(any(Showtime.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when movie does not exist")
        void shouldThrowResourceNotFoundException_WhenMovieDoesNotExist() throws Exception {
             doThrow(new ResourceNotFoundException("ERROR: Movie with id 999 does not exist."))
                    .when(showtimeService).addShowtime(any(Showtime.class));

            Showtime showtimeWithInvalidMovie = new Showtime(
                    null,
                    999L,
                    "Theater A",
                    startTime,
                    endTime,
                    12.50F
            );

             
            mockMvc.perform(post("/showtime")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(showtimeWithInvalidMovie)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(showtimeService, times(1)).addShowtime(any(Showtime.class));
        }

        @Test
        @DisplayName("Should accept valid showtime with all required fields")
        void shouldAcceptValidShowtime_WithAllRequiredFields() throws Exception {
             Showtime validShowtime = new Showtime(
                    null,
                    2L,
                    "Theater C",
                    LocalDateTime.of(2024, 1, 20, 18, 0),
                    LocalDateTime.of(2024, 1, 20, 20, 30),
                    14.00F
            );
            doNothing().when(showtimeService).addShowtime(any(Showtime.class));

             
            mockMvc.perform(post("/showtime")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validShowtime)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().string("Showtime was added successfully."));

            verify(showtimeService, times(1)).addShowtime(any(Showtime.class));
        }
    }

    @Nested
    @DisplayName("GET /showtime/{id} - Get Showtime By Id Tests")
    class GetShowtimeByIdTests {

        @Test
        @DisplayName("Should return showtime with 200 OK when valid id provided")
        void shouldReturnShowtime_WithStatus200() throws Exception {
             when(showtimeService.getShowtimeById(1L)).thenReturn(testShowtime);

             
            mockMvc.perform(get("/showtime/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.movieId", is(1)))
                    .andExpect(jsonPath("$.theater", is("Theater A")))
                    .andExpect(jsonPath("$.price", is(12.50)));

            verify(showtimeService, times(1)).getShowtimeById(1L);
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when showtime does not exist")
        void shouldReturn404NotFound_WhenShowtimeDoesNotExist() throws Exception {
             Long nonExistentId = 999L;
            when(showtimeService.getShowtimeById(nonExistentId))
                    .thenThrow(new ResourceNotFoundException("ERROR: Showtime with id " + nonExistentId + " does not exist."));

             
            mockMvc.perform(get("/showtime/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(showtimeService, times(1)).getShowtimeById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle different showtime IDs correctly")
        void shouldHandleDifferentShowtimeIds_Correctly() throws Exception {
             Long showtimeId = 42L;
            Showtime differentShowtime = new Showtime(
                    42L,
                    3L,
                    "Theater D",
                    LocalDateTime.of(2024, 1, 20, 15, 0),
                    LocalDateTime.of(2024, 1, 20, 17, 0),
                    10.00F
            );
            when(showtimeService.getShowtimeById(showtimeId)).thenReturn(differentShowtime);

             
            mockMvc.perform(get("/showtime/" + showtimeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(42)))
                    .andExpect(jsonPath("$.movieId", is(3)))
                    .andExpect(jsonPath("$.theater", is("Theater D")));

            verify(showtimeService, times(1)).getShowtimeById(42L);
        }

        @Test
        @DisplayName("Should return correct content type")
        void shouldReturnCorrectContentType() throws Exception {
             when(showtimeService.getShowtimeById(1L)).thenReturn(testShowtime);

             
            mockMvc.perform(get("/showtime/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(showtimeService, times(1)).getShowtimeById(1L);
        }
    }

    @Nested
    @DisplayName("PUT /showtime/{id} - Update Showtime Tests")
    class UpdateShowtimeTests {

        @Test
        @DisplayName("Should update showtime and return 200 OK with updated showtime")
        void shouldUpdateShowtime_AndReturn200Ok() throws Exception {
             when(showtimeService.updateShowtime(eq(1L), any(Showtime.class))).thenReturn(updatedShowtime);

             
            mockMvc.perform(put("/showtime/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedShowtime)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.movieId", is(1)))
                    .andExpect(jsonPath("$.theater", is("Theater B")))
                    .andExpect(jsonPath("$.price", is(15.00)));

            verify(showtimeService, times(1)).updateShowtime(eq(1L), any(Showtime.class));
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when updating non-existent showtime")
        void shouldReturn404NotFound_WhenUpdatingNonExistentShowtime() throws Exception {
             Long nonExistentId = 999L;
            when(showtimeService.updateShowtime(eq(nonExistentId), any(Showtime.class)))
                    .thenThrow(new ResourceNotFoundException("ERROR: Showtime with id " + nonExistentId + " does not exist."));

             
            mockMvc.perform(put("/showtime/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedShowtime)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(showtimeService, times(1)).updateShowtime(eq(nonExistentId), any(Showtime.class));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when update has invalid times")
        void shouldReturn400BadRequest_WhenUpdateHasInvalidTimes() throws Exception {
             Showtime invalidUpdate = new Showtime(
                    1L,
                    1L,
                    "Theater A",
                    LocalDateTime.of(2024, 1, 16, 22, 0),
                    LocalDateTime.of(2024, 1, 16, 20, 0), // End before start
                    15.00F
            );

            when(showtimeService.updateShowtime(eq(1L), any(Showtime.class)))
                    .thenThrow(new ValidationException("End time must be after start time"));

             
            mockMvc.perform(put("/showtime/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidUpdate)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(showtimeService, times(1)).updateShowtime(eq(1L), any(Showtime.class));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when update causes overlap")
        void shouldReturn400BadRequest_WhenUpdateCausesOverlap() throws Exception {
             when(showtimeService.updateShowtime(eq(1L), any(Showtime.class)))
                    .thenThrow(new ValidationException("Showtime overlaps with another showtime in the same theater"));

             
            mockMvc.perform(put("/showtime/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedShowtime)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(showtimeService, times(1)).updateShowtime(eq(1L), any(Showtime.class));
        }

        @Test
        @DisplayName("Should handle path variable correctly")
        void shouldHandlePathVariableCorrectly() throws Exception {
             Long showtimeId = 55L;
            Showtime updatedShowtimeWithNewId = new Showtime(
                    55L,
                    2L,
                    "Theater E",
                    LocalDateTime.of(2024, 1, 25, 19, 0),
                    LocalDateTime.of(2024, 1, 25, 21, 0),
                    18.00F
            );
            when(showtimeService.updateShowtime(eq(showtimeId), any(Showtime.class)))
                    .thenReturn(updatedShowtimeWithNewId);

             
            mockMvc.perform(put("/showtime/" + showtimeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedShowtimeWithNewId)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(55)));

            verify(showtimeService, times(1)).updateShowtime(eq(55L), any(Showtime.class));
        }

        @Test
        @DisplayName("Should call service with correct ID and showtime details")
        void shouldCallService_WithCorrectIdAndShowtimeDetails() throws Exception {
             when(showtimeService.updateShowtime(eq(1L), any(Showtime.class))).thenReturn(updatedShowtime);

            // Act
            mockMvc.perform(put("/showtime/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedShowtime)))
                    .andExpect(status().isOk());

            // Assert
            verify(showtimeService).updateShowtime(eq(1L), argThat(showtime ->
                    showtime.getTheater().equals("Theater B") &&
                            showtime.getPrice() == 15.00F
            ));
        }
    }

    @Nested
    @DisplayName("DELETE /showtime/{id} - Delete Showtime Tests")
    class DeleteShowtimeTests {

        @Test
        @DisplayName("Should delete showtime and return 200 OK with success message")
        void shouldDeleteShowtime_AndReturn200Ok() throws Exception {
             Long showtimeId = 1L;
            doNothing().when(showtimeService).deleteShowtime(showtimeId);

             
            mockMvc.perform(delete("/showtime/" + showtimeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("Showtime with id 1 was deleted successfully."));

            verify(showtimeService, times(1)).deleteShowtime(showtimeId);
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when deleting non-existent showtime")
        void shouldReturn404NotFound_WhenDeletingNonExistentShowtime() throws Exception {
             Long nonExistentId = 999L;
            doThrow(new ResourceNotFoundException("ERROR: Showtime with id " + nonExistentId + " does not exist."))
                    .when(showtimeService).deleteShowtime(nonExistentId);

             
            mockMvc.perform(delete("/showtime/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(showtimeService, times(1)).deleteShowtime(nonExistentId);
        }

        @Test
        @DisplayName("Should handle different showtime IDs correctly")
        void shouldHandleDifferentShowtimeIds_Correctly() throws Exception {
             Long showtimeId = 123L;
            doNothing().when(showtimeService).deleteShowtime(showtimeId);

             
            mockMvc.perform(delete("/showtime/" + showtimeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("Showtime with id 123 was deleted successfully."));

            verify(showtimeService, times(1)).deleteShowtime(showtimeId);
        }

        @Test
        @DisplayName("Should verify service method is called with correct ID")
        void shouldVerifyServiceMethod_IsCalledWithCorrectId() throws Exception {
             Long showtimeId = 77L;
            doNothing().when(showtimeService).deleteShowtime(showtimeId);

            // Act
            mockMvc.perform(delete("/showtime/" + showtimeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Assert
            verify(showtimeService, times(1)).deleteShowtime(showtimeId);
        }
    }

}