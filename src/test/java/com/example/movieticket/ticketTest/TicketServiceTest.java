package com.example.movieticket.ticketTest;

import com.example.movieticket.ticket.*;

import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.exception.ValidationException;
import com.example.movieticket.showtime.ShowtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService Tests")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ShowtimeService showtimeService;

    @InjectMocks
    private TicketService ticketService;

    private Ticket testTicket;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testTicket = new Ticket(
                null,
                1L,
                15,
                testUserId
        );
    }


    @Nested
    @DisplayName("addTicket() Tests")
    class AddTicketTests {

        @Test
        @DisplayName("Should successfully add ticket when all validations pass")
        void shouldAddTicket_WhenAllValidationsPass() {

            doNothing().when(showtimeService).validateShowtimeExists(1L);
            when(ticketRepository.existsByShowtimeIdAndSeatNumber(1L, 15)).thenReturn(false);
            when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

            ticketService.addTicket(testTicket);

            verify(showtimeService, times(1)).validateShowtimeExists(1L);
            verify(ticketRepository, times(1)).existsByShowtimeIdAndSeatNumber(1L, 15);
            verify(ticketRepository, times(1)).save(testTicket);
        }

        @Test
        @DisplayName("Should call repository save with correct ticket object")
        void shouldCallRepositorySave_WithCorrectTicketObject() {

            doNothing().when(showtimeService).validateShowtimeExists(1L);
            when(ticketRepository.existsByShowtimeIdAndSeatNumber(1L, 15)).thenReturn(false);
            when(ticketRepository.save(testTicket)).thenReturn(testTicket);


            ticketService.addTicket(testTicket);

            verify(ticketRepository).save(argThat(ticket ->
                    ticket.getShowtimeId().equals(1L) &&
                            ticket.getSeatNumber().equals(15) &&
                            ticket.getUserId().equals(testUserId)
            ));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when showtime does not exist")
        void shouldThrowResourceNotFoundException_WhenShowtimeDoesNotExist() {

            long nonExistentShowtimeId = 999L;
            Ticket ticketWithInvalidShowtime = new Ticket(
                    null,
                    nonExistentShowtimeId,
                    15,
                    UUID.randomUUID()
            );

            doThrow(new ResourceNotFoundException("ERROR: Showtime with id " + nonExistentShowtimeId + " does not exist."))
                    .when(showtimeService).validateShowtimeExists(nonExistentShowtimeId);


            assertThatThrownBy(() -> ticketService.addTicket(ticketWithInvalidShowtime))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Showtime with id " + nonExistentShowtimeId + " does not exist.");

            verify(showtimeService, times(1)).validateShowtimeExists(nonExistentShowtimeId);
            verify(ticketRepository, never()).existsByShowtimeIdAndSeatNumber(anyLong(), anyInt());
            verify(ticketRepository, never()).save(any(Ticket.class));
        }

        @Test
        @DisplayName("Should throw ValidationException when seat is already booked")
        void shouldThrowValidationException_WhenSeatIsAlreadyBooked() {

            doNothing().when(showtimeService).validateShowtimeExists(1L);
            when(ticketRepository.existsByShowtimeIdAndSeatNumber(1L, 15)).thenReturn(true);


            assertThatThrownBy(() -> ticketService.addTicket(testTicket))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("The wanted seat is already booked! choose anther seat.");

            verify(showtimeService, times(1)).validateShowtimeExists(1L);
            verify(ticketRepository, times(1)).existsByShowtimeIdAndSeatNumber(1L, 15);
            verify(ticketRepository, never()).save(any(Ticket.class));
        }

        @Test
        @DisplayName("Should allow same seat number for different showtimes")
        void shouldAllowSameSeatNumber_ForDifferentShowtimes() {

            UUID anotherUserId = UUID.randomUUID();
            Ticket ticketForDifferentShowtime = new Ticket(
                    null,
                    2L, // Different showtime
                    15, // Same seat
                    anotherUserId
            );

            doNothing().when(showtimeService).validateShowtimeExists(2L);
            when(ticketRepository.existsByShowtimeIdAndSeatNumber(2L, 15)).thenReturn(false);
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketForDifferentShowtime);


            ticketService.addTicket(ticketForDifferentShowtime);


            verify(showtimeService, times(1)).validateShowtimeExists(2L);
            verify(ticketRepository, times(1)).existsByShowtimeIdAndSeatNumber(2L, 15);
            verify(ticketRepository, times(1)).save(ticketForDifferentShowtime);
        }

        @Test
        @DisplayName("Should allow different seat numbers for same showtime")
        void shouldAllowDifferentSeatNumbers_ForSameShowtime() {
            UUID anotherUserId = UUID.randomUUID();
            Ticket ticketWithDifferentSeat = new Ticket(
                    null,
                    1L, // Same showtime
                    50, // Different seat
                    anotherUserId
            );

            doNothing().when(showtimeService).validateShowtimeExists(1L);
            when(ticketRepository.existsByShowtimeIdAndSeatNumber(1L, 50)).thenReturn(false);
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketWithDifferentSeat);


            ticketService.addTicket(ticketWithDifferentSeat);


            verify(showtimeService, times(1)).validateShowtimeExists(1L);
            verify(ticketRepository, times(1)).existsByShowtimeIdAndSeatNumber(1L, 50);
            verify(ticketRepository, times(1)).save(ticketWithDifferentSeat);
        }

        @Test
        @DisplayName("Should validate showtime before checking seat availability")
        void shouldValidateShowtime_BeforeCheckingSeatAvailability() {

            doThrow(new ResourceNotFoundException("ERROR: Showtime with id 999 does not exist."))
                    .when(showtimeService).validateShowtimeExists(999L);

            Ticket ticketWithInvalidShowtime = new Ticket(
                    null,
                    999L,
                    15,
                    UUID.randomUUID()
            );


            assertThatThrownBy(() -> ticketService.addTicket(ticketWithInvalidShowtime))
                    .isInstanceOf(ResourceNotFoundException.class);

            // Verify seat check was never called since showtime validation failed first
            verify(showtimeService, times(1)).validateShowtimeExists(999L);
            verify(ticketRepository, never()).existsByShowtimeIdAndSeatNumber(anyLong(), anyInt());
        }
    }

    @Nested
    @DisplayName("validateTicket() - Private Method Tests (via public methods)")
    class ValidateTicketTests {

        @Test
        @DisplayName("Should validate showtime exists first, then check seat availability")
        void shouldValidateShowtimeFirst_ThenCheckSeatAvailability() {

            doNothing().when(showtimeService).validateShowtimeExists(1L);
            when(ticketRepository.existsByShowtimeIdAndSeatNumber(1L, 15)).thenReturn(false);
            when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);


            ticketService.addTicket(testTicket);

            InOrder inOrder = inOrder(showtimeService, ticketRepository);
            inOrder.verify(showtimeService).validateShowtimeExists(1L);
            inOrder.verify(ticketRepository).existsByShowtimeIdAndSeatNumber(1L, 15);
            inOrder.verify(ticketRepository).save(testTicket);
        }

        @Test
        @DisplayName("Should stop validation at first failure")
        void shouldStopValidation_AtFirstFailure() {

            doThrow(new ResourceNotFoundException("ERROR: Showtime with id 1 does not exist."))
                    .when(showtimeService).validateShowtimeExists(1L);


            assertThatThrownBy(() -> ticketService.addTicket(testTicket))
                    .isInstanceOf(ResourceNotFoundException.class);

            // Verify subsequent validations were not called
            verify(showtimeService, times(1)).validateShowtimeExists(1L);
            verify(ticketRepository, never()).existsByShowtimeIdAndSeatNumber(anyLong(), anyInt());
            verify(ticketRepository, never()).save(any(Ticket.class));
        }
    }
}