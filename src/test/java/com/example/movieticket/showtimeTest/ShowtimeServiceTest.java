package com.example.movieticket.showtimeTest;


import com.example.movieticket.showtime.*;

import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.exception.ValidationException;
import com.example.movieticket.movie.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShowtimeService Tests")
class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private ShowtimeService showtimeService;

    private Showtime testShowtime;
    private Showtime updatedShowtime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.of(2024, 1, 15, 19, 0);
        endTime = LocalDateTime.of(2024, 1, 15, 21, 30);

        testShowtime = new Showtime(
                1L,
                1L,
                "Theater A",
                startTime,
                endTime,
                12.50
        );

        updatedShowtime = new Showtime(
                1L,
                1L,
                "Theater B",
                LocalDateTime.of(2024, 1, 16, 20, 0),
                LocalDateTime.of(2024, 1, 16, 22, 30),
                15.00
        );
    }

    @Nested
    @DisplayName("addShowtime() Tests")
    class AddShowtimeTests {

        @Test
        @DisplayName("Should successfully add showtime when all validations pass")
        void shouldAddShowtime_WhenAllValidationsPass() {

            doNothing().when(movieService).validateMovieExists(1L);
            when(showtimeRepository.findOverlappingShowtime(
                    testShowtime.getTheater(),
                    testShowtime.getStartTime(),
                    testShowtime.getEndTime()
            )).thenReturn(List.of());
            when(showtimeRepository.save(any(Showtime.class))).thenReturn(testShowtime);


            showtimeService.addShowtime(testShowtime);


            verify(movieService, times(1)).validateMovieExists(1L);
            verify(showtimeRepository, times(1)).findOverlappingShowtime(
                    testShowtime.getTheater(),
                    testShowtime.getStartTime(),
                    testShowtime.getEndTime()
            );
            verify(showtimeRepository, times(1)).save(testShowtime);
        }

        @Test
        @DisplayName("Should throw ValidationException when end time is before start time")
        void shouldThrowValidationException_WhenEndTimeIsBeforeStartTime() {

            Showtime invalidShowtime = new Showtime(
                    null,
                    1L,
                    "Theater A",
                    LocalDateTime.of(2024, 1, 15, 21, 0),
                    LocalDateTime.of(2024, 1, 15, 19, 0), // End before start
                    12.50
            );

            assertThatThrownBy(() -> showtimeService.addShowtime(invalidShowtime))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("End time must be after start time");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("Should throw ValidationException when end time equals start time")
        void shouldThrowValidationException_WhenEndTimeEqualsStartTime() {

            LocalDateTime sameTime = LocalDateTime.of(2024, 1, 15, 19, 0);
            Showtime invalidShowtime = new Showtime(
                    null,
                    1L,
                    "Theater A",
                    sameTime,
                    sameTime,
                    12.50
            );


            assertThatThrownBy(() -> showtimeService.addShowtime(invalidShowtime))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("End time must be after start time");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when movie does not exist")
        void shouldThrowResourceNotFoundException_WhenMovieDoesNotExist() {

            doThrow(new ResourceNotFoundException("ERROR: Movie with id 999 does not exist."))
                    .when(movieService).validateMovieExists(999L);

            Showtime showtimeWithInvalidMovie = new Showtime(
                    null,
                    999L,
                    "Theater A",
                    startTime,
                    endTime,
                    12.50
            );


            assertThatThrownBy(() -> showtimeService.addShowtime(showtimeWithInvalidMovie))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Movie with id 999 does not exist.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("Should throw ValidationException when showtime overlaps with existing showtime")
        void shouldThrowValidationException_WhenShowtimeOverlaps() {

            Showtime overlappingShowtime = new Showtime(
                    2L,
                    2L,
                    "Theater A",
                    startTime.plusMinutes(30),
                    endTime.plusMinutes(30),
                    15.00
            );

            doNothing().when(movieService).validateMovieExists(1L);
            when(showtimeRepository.findOverlappingShowtime(
                    testShowtime.getTheater(),
                    testShowtime.getStartTime(),
                    testShowtime.getEndTime()
            )).thenReturn(List.of(overlappingShowtime));


            assertThatThrownBy(() -> showtimeService.addShowtime(testShowtime))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Showtime overlaps with another showtime in the same theater");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("Should allow showtime in different theater at same time")
        void shouldAllowShowtime_InDifferentTheaterAtSameTime() {

            Showtime showtimeInDifferentTheater = new Showtime(
                    null,
                    1L,
                    "Theater B", // Different theater
                    startTime,
                    endTime,
                    12.50
            );

            doNothing().when(movieService).validateMovieExists(1L);
            when(showtimeRepository.findOverlappingShowtime(
                    "Theater B",
                    startTime,
                    endTime
            )).thenReturn(List.of());
            when(showtimeRepository.save(any(Showtime.class))).thenReturn(showtimeInDifferentTheater);


            showtimeService.addShowtime(showtimeInDifferentTheater);


            verify(showtimeRepository, times(1)).save(showtimeInDifferentTheater);
        }
    }

    @Nested
    @DisplayName("getShowtimeById() Tests")
    class GetShowtimeByIdTests {

        @Test
        @DisplayName("Should return showtime when valid id is provided")
        void shouldReturnShowtime_WhenValidIdProvided() {

            when(showtimeRepository.findById(1L)).thenReturn(Optional.of(testShowtime));


            Showtime result = showtimeService.getShowtimeById(1L);


            assertThat(result)
                    .isNotNull()
                    .isEqualTo(testShowtime);
            assertThat(result.getTheater()).isEqualTo("Theater A");
            assertThat(result.getMovieId()).isEqualTo(1L);
            verify(showtimeRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when showtime not found")
        void shouldThrowResourceNotFoundException_WhenShowtimeNotFound() {

            Long nonExistentId = 999L;
            when(showtimeRepository.findById(nonExistentId)).thenReturn(Optional.empty());


            assertThatThrownBy(() -> showtimeService.getShowtimeById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Showtime with id " + nonExistentId + " does not exist.");
            verify(showtimeRepository, times(1)).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("updateShowtime() Tests")
    class UpdateShowtimeTests {

        @Test
        @DisplayName("Should update showtime successfully when all validations pass")
        void shouldUpdateShowtime_WhenAllValidationsPass() {

            when(showtimeRepository.existsById(1L)).thenReturn(true);
            doNothing().when(movieService).validateMovieExists(1L);
            when(showtimeRepository.findOverlappingShowtime(
                    updatedShowtime.getTheater(),
                    updatedShowtime.getStartTime(),
                    updatedShowtime.getEndTime()
            )).thenReturn(List.of());
            when(showtimeRepository.save(any(Showtime.class))).thenReturn(updatedShowtime);


            Showtime result = showtimeService.updateShowtime(1L, updatedShowtime);


            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTheater()).isEqualTo("Theater B");
            verify(showtimeRepository, times(1)).existsById(1L);
            verify(showtimeRepository, times(1)).save(any(Showtime.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent showtime")
        void shouldThrowResourceNotFoundException_WhenUpdatingNonExistentShowtime() {

            Long nonExistentId = 999L;
            when(showtimeRepository.existsById(nonExistentId)).thenReturn(false);


            assertThatThrownBy(() -> showtimeService.updateShowtime(nonExistentId, updatedShowtime))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Showtime with id " + nonExistentId + " does not exist.");
            verify(showtimeRepository, times(1)).existsById(nonExistentId);
            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("Should throw ValidationException when updated showtime has invalid times")
        void shouldThrowValidationException_WhenUpdatedShowtimeHasInvalidTimes() {

            Showtime invalidUpdate = new Showtime(
                    1L,
                    1L,
                    "Theater A",
                    LocalDateTime.of(2024, 1, 16, 22, 0),
                    LocalDateTime.of(2024, 1, 16, 20, 0), // End before start
                    15.00
            );
            when(showtimeRepository.existsById(1L)).thenReturn(true);

            assertThatThrownBy(() -> showtimeService.updateShowtime(1L, invalidUpdate))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("End time must be after start time");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("Should throw ValidationException when updated showtime overlaps")
        void shouldThrowValidationException_WhenUpdatedShowtimeOverlaps() {

            Showtime overlapping = new Showtime(
                    2L,
                    2L,
                    "Theater B",
                    updatedShowtime.getStartTime(),
                    updatedShowtime.getEndTime(),
                    10.00
            );

            when(showtimeRepository.existsById(1L)).thenReturn(true);
            doNothing().when(movieService).validateMovieExists(1L);
            when(showtimeRepository.findOverlappingShowtime(
                    updatedShowtime.getTheater(),
                    updatedShowtime.getStartTime(),
                    updatedShowtime.getEndTime()
            )).thenReturn(List.of(overlapping));


            assertThatThrownBy(() -> showtimeService.updateShowtime(1L, updatedShowtime))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Showtime overlaps with another showtime in the same theater");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("Should create new Showtime object with updated details")
        void shouldCreateNewShowtimeObject_WithUpdatedDetails() {

            when(showtimeRepository.existsById(1L)).thenReturn(true);
            doNothing().when(movieService).validateMovieExists(1L);
            when(showtimeRepository.findOverlappingShowtime(
                    updatedShowtime.getTheater(),
                    updatedShowtime.getStartTime(),
                    updatedShowtime.getEndTime()
            )).thenReturn(List.of());
            when(showtimeRepository.save(any(Showtime.class))).thenReturn(updatedShowtime);


            showtimeService.updateShowtime(1L, updatedShowtime);


            verify(showtimeRepository).save(argThat(showtime ->
                    showtime.getId().equals(1L) &&
                            showtime.getMovieId().equals(1L) &&
                            showtime.getTheater().equals("Theater B") &&
                            showtime.getPrice() == 15.00
            ));
        }
    }

    @Nested
    @DisplayName("deleteShowtime() Tests")
    class DeleteShowtimeTests {

        @Test
        @DisplayName("Should delete showtime successfully when valid id provided")
        void shouldDeleteShowtime_WhenValidIdProvided() {

            when(showtimeRepository.findById(1L)).thenReturn(Optional.of(testShowtime));
            doNothing().when(showtimeRepository).delete(testShowtime);


            showtimeService.deleteShowtime(1L);


            verify(showtimeRepository, times(1)).findById(1L);
            verify(showtimeRepository, times(1)).delete(testShowtime);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent showtime")
        void shouldThrowResourceNotFoundException_WhenDeletingNonExistentShowtime() {

            Long nonExistentId = 999L;
            when(showtimeRepository.findById(nonExistentId)).thenReturn(Optional.empty());


            assertThatThrownBy(() -> showtimeService.deleteShowtime(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Showtime with id " + nonExistentId + " does not exist.");
            verify(showtimeRepository, times(1)).findById(nonExistentId);
            verify(showtimeRepository, never()).delete(any(Showtime.class));
        }
    }

    @Nested
    @DisplayName("validateShowtimeExists() Tests")
    class ValidateShowtimeExistsTests {

        @Test
        @DisplayName("Should not throw exception when showtime exists")
        void shouldNotThrowException_WhenShowtimeExists() {

            when(showtimeRepository.existsById(1L)).thenReturn(true);


            assertThatCode(() -> showtimeService.validateShowtimeExists(1L))
                    .doesNotThrowAnyException();
            verify(showtimeRepository, times(1)).existsById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when showtime does not exist")
        void shouldThrowResourceNotFoundException_WhenShowtimeDoesNotExist() {

            Long nonExistentId = 999L;
            when(showtimeRepository.existsById(nonExistentId)).thenReturn(false);


            assertThatThrownBy(() -> showtimeService.validateShowtimeExists(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Showtime with id " + nonExistentId + " does not exist.");
            verify(showtimeRepository, times(1)).existsById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("validateShowtime() - Private Method Tests (via public methods)")
    class ValidateShowtimeTests {

        @Test
        @DisplayName("Should validate all conditions in correct order")
        void shouldValidateAllConditions_InCorrectOrder() {

            doNothing().when(movieService).validateMovieExists(1L);
            when(showtimeRepository.findOverlappingShowtime(
                    testShowtime.getTheater(),
                    testShowtime.getStartTime(),
                    testShowtime.getEndTime()
            )).thenReturn(List.of());
            when(showtimeRepository.save(any(Showtime.class))).thenReturn(testShowtime);


            showtimeService.addShowtime(testShowtime);


            verify(movieService, times(1)).validateMovieExists(1L);
            verify(showtimeRepository, times(1)).findOverlappingShowtime(
                    testShowtime.getTheater(),
                    testShowtime.getStartTime(),
                    testShowtime.getEndTime()
            );
        }

        @Test
        @DisplayName("Should stop validation at first failure")
        void shouldStopValidation_AtFirstFailure() {

            Showtime invalidShowtime = new Showtime(
                    null,
                    1L,
                    "Theater A",
                    LocalDateTime.of(2024, 1, 15, 22, 0),
                    LocalDateTime.of(2024, 1, 15, 20, 0), // Invalid times
                    12.50
            );



            assertThatThrownBy(() -> showtimeService.addShowtime(invalidShowtime))
                    .isInstanceOf(ValidationException.class);

            // Verify subsequent validations were not called
            verify(movieService, never()).validateMovieExists(anyLong());
            verify(showtimeRepository, never()).findOverlappingShowtime(any(), any(), any());
        }
    }
}