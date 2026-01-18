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

import java.util.Optional;

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

    @BeforeEach
    void setUp() {
        testTicket = new Ticket(
                1L,
                1L,
                15,
                "John Doe"
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
                            ticket.getCustomerName().equals("John Doe")
            ));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when showtime does not exist")
        void shouldThrowResourceNotFoundException_WhenShowtimeDoesNotExist() {
            
            Long nonExistentShowtimeId = 999L;
            Ticket ticketWithInvalidShowtime = new Ticket(
                    null,
                    nonExistentShowtimeId,
                    15,
                    "John Doe"
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
            
            Ticket ticketForDifferentShowtime = new Ticket(
                    null,
                    2L, // Different showtime
                    15, // Same seat
                    "Jane Smith"
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
            
            Ticket ticketWithDifferentSeat = new Ticket(
                    null,
                    1L, // Same showtime
                    50, // Different seat
                    "Jane Smith"
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
                    "John Doe"
            );

            
            assertThatThrownBy(() -> ticketService.addTicket(ticketWithInvalidShowtime))
                    .isInstanceOf(ResourceNotFoundException.class);

            // Verify seat check was never called since showtime validation failed first
            verify(showtimeService, times(1)).validateShowtimeExists(999L);
            verify(ticketRepository, never()).existsByShowtimeIdAndSeatNumber(anyLong(), anyInt());
        }
    }

    @Nested
    @DisplayName("getTicketById() Tests")
    class GetTicketByIdTests {

        @Test
        @DisplayName("Should return ticket when valid id is provided")
        void shouldReturnTicket_WhenValidIdProvided() {
            
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

            
            Ticket result = ticketService.getTicketById(1L);

            
            assertThat(result)
                    .isNotNull()
                    .isEqualTo(testTicket);
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getShowtimeId()).isEqualTo(1L);
            assertThat(result.getSeatNumber()).isEqualTo(15);
            assertThat(result.getCustomerName()).isEqualTo("John Doe");
            verify(ticketRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when ticket not found")
        void shouldThrowResourceNotFoundException_WhenTicketNotFound() {
            
            Long nonExistentId = 999L;
            when(ticketRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            
            assertThatThrownBy(() -> ticketService.getTicketById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ERROR: Ticket with id " + nonExistentId + " does not exist.");
            verify(ticketRepository, times(1)).findById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle different ticket IDs correctly")
        void shouldHandleDifferentTicketIds_Correctly() {
            
            Long ticketId = 42L;
            Ticket differentTicket = new Ticket(
                    42L,
                    2L,
                    75,
                    "Alice Johnson"
            );
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(differentTicket));

            
            Ticket result = ticketService.getTicketById(ticketId);

            
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(42L);
            assertThat(result.getShowtimeId()).isEqualTo(2L);
            assertThat(result.getSeatNumber()).isEqualTo(75);
            assertThat(result.getCustomerName()).isEqualTo("Alice Johnson");
            verify(ticketRepository, times(1)).findById(42L);
        }

        @Test
        @DisplayName("Should return ticket with all fields populated")
        void shouldReturnTicket_WithAllFieldsPopulated() {
            
            Ticket completeTicket = new Ticket(
                    5L,
                    3L,
                    90,
                    "Bob Williams"
            );
            when(ticketRepository.findById(5L)).thenReturn(Optional.of(completeTicket));

            
            Ticket result = ticketService.getTicketById(5L);

            
            assertThat(result.getId()).isNotNull();
            assertThat(result.getShowtimeId()).isNotNull();
            assertThat(result.getSeatNumber()).isNotNull();
            assertThat(result.getCustomerName()).isNotBlank();
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