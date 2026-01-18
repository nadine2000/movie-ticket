package com.example.movieticket.ticketTest;

import com.example.movieticket.exception.GlobalExceptionHandler;
import com.example.movieticket.ticket.*;

import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketController Tests")
class TicketControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private Ticket testTicket;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(ticketController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        testUserId = UUID.randomUUID();
        testTicket = createTicket(1L, 15, testUserId);
    }

    private Ticket createTicket(Long showtimeId, Integer seatNumber, UUID userId) {
        Ticket ticket = new Ticket();
        ticket.setShowtimeId(showtimeId);
        ticket.setSeatNumber(seatNumber);
        ticket.setUserId(userId);
        return ticket;
    }


    @Nested
    @DisplayName("POST /bookings - Book Ticket Tests")
    class BookTicketTests {

        @Test
        @DisplayName("Should book ticket and return 201 CREATED with success message")
        void shouldBookTicket_AndReturn201Created() throws Exception {

            UUID generatedId = UUID.randomUUID();
            doAnswer(invocation -> {
                Ticket ticket = invocation.getArgument(0);
                ticket.setId(generatedId);
                return null;
            }).when(ticketService).addTicket(any(Ticket.class));

            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testTicket)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookingId", notNullValue()));

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should call service with correct ticket data")
        void shouldCallService_WithCorrectTicketData() throws Exception {

            UUID generatedId = UUID.randomUUID();
            doAnswer(invocation -> {
                Ticket ticket = invocation.getArgument(0);
                ticket.setId(generatedId);
                return null;
            }).when(ticketService).addTicket(any(Ticket.class));


            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testTicket)))
                    .andExpect(status().isOk());


            verify(ticketService).addTicket(argThat(ticket ->
                    ticket.getShowtimeId().equals(1L) &&
                            ticket.getSeatNumber().equals(15) &&
                            ticket.getUserId().equals(testUserId)
            ));
        }

        @Test
        @DisplayName("Should throw ValidationException when seat is already booked")
        void shouldThrowValidationException_WhenSeatIsAlreadyBooked() throws Exception {

            doThrow(new ValidationException("The wanted seat is already booked! choose anther seat."))
                    .when(ticketService).addTicket(any(Ticket.class));


            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testTicket)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when showtime does not exist")
        void shouldThrowResourceNotFoundException_WhenShowtimeDoesNotExist() throws Exception {

            Ticket ticketWithInvalidShowtime = createTicket(999L, 15, testUserId);

            doThrow(new ResourceNotFoundException("ERROR: Showtime with id 999 does not exist."))
                    .when(ticketService).addTicket(any(Ticket.class));


            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ticketWithInvalidShowtime)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when seat number is less than 1")
        void shouldReturn400BadRequest_WhenSeatNumberIsLessThan1() throws Exception {

            Ticket invalidTicket = createTicket(1L, 0, testUserId);


            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidTicket)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(ticketService, never()).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when seat number is greater than 100")
        void shouldReturn400BadRequest_WhenSeatNumberIsGreaterThan100() throws Exception {

            Ticket invalidTicket = createTicket(1L, 101, testUserId);

            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidTicket)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(ticketService, never()).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when user ID is null")
        void shouldReturn400BadRequest_WhenUserIdIsNull() throws Exception {
            Ticket invalidTicket = createTicket(1L, 15, null);

            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidTicket)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(ticketService, never()).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should accept ticket with seat number at minimum boundary (1)")
        void shouldAcceptTicket_WithSeatNumberAtMinimumBoundary() throws Exception {

            Ticket validTicket = createTicket(1L, 1, testUserId);
            UUID generatedId = UUID.randomUUID();
            doAnswer(invocation -> {
                Ticket ticket = invocation.getArgument(0);
                ticket.setId(generatedId);
                return null;
            }).when(ticketService).addTicket(any(Ticket.class));


            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validTicket)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookingId", notNullValue()));

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should accept ticket with seat number at maximum boundary (100)")
        void shouldAcceptTicket_WithSeatNumberAtMaximumBoundary() throws Exception {

            Ticket validTicket = createTicket(1L, 100, testUserId);
            UUID generatedId = UUID.randomUUID();
            doAnswer(invocation -> {
                Ticket ticket = invocation.getArgument(0);
                ticket.setId(generatedId);
                return null;
            }).when(ticketService).addTicket(any(Ticket.class));


            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validTicket)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookingId", notNullValue()));

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should accept valid ticket with all required fields")
        void shouldAcceptValidTicket_WithAllRequiredFields() throws Exception {

            UUID anotherUserId = UUID.randomUUID();
            Ticket validTicket = createTicket(2L, 50, anotherUserId);
            UUID generatedId = UUID.randomUUID();
            doAnswer(invocation -> {
                Ticket ticket = invocation.getArgument(0);
                ticket.setId(generatedId);
                return null;
            }).when(ticketService).addTicket(any(Ticket.class));


            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validTicket)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookingId", notNullValue()));

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }
    }
}