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

import static org.hamcrest.Matchers.is;
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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(ticketController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        testTicket = new Ticket(
                1L,
                1L,
                15,
                "John Doe"
        );
    }

    @Nested
    @DisplayName("POST /tickets - Book Ticket Tests")
    class BookTicketTests {

        @Test
        @DisplayName("Should book ticket and return 201 CREATED with success message")
        void shouldBookTicket_AndReturn201Created() throws Exception {
            
            doNothing().when(ticketService).addTicket(any(Ticket.class));

           
            mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testTicket)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().string("Ticket was booked successfully."));

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should call service with correct ticket data")
        void shouldCallService_WithCorrectTicketData() throws Exception {
            
            doNothing().when(ticketService).addTicket(any(Ticket.class));

            
            mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testTicket)))
                    .andExpect(status().isCreated());

            
            verify(ticketService).addTicket(argThat(ticket ->
                    ticket.getShowtimeId().equals(1L) &&
                            ticket.getSeatNumber().equals(15) &&
                            ticket.getCustomerName().equals("John Doe")
            ));
        }

        @Test
        @DisplayName("Should throw ValidationException when seat is already booked")
        void shouldThrowValidationException_WhenSeatIsAlreadyBooked() throws Exception {
            
            doThrow(new ValidationException("The wanted seat is already booked! choose anther seat."))
                    .when(ticketService).addTicket(any(Ticket.class));

           
            mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testTicket)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when showtime does not exist")
        void shouldThrowResourceNotFoundException_WhenShowtimeDoesNotExist() throws Exception {
            
            Ticket ticketWithInvalidShowtime = new Ticket(
                    null,
                    999L,
                    15,
                    "John Doe"
            );

            doThrow(new ResourceNotFoundException("ERROR: Showtime with id 999 does not exist."))
                    .when(ticketService).addTicket(any(Ticket.class));

           
            mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ticketWithInvalidShowtime)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when seat number is less than 1")
        void shouldReturn400BadRequest_WhenSeatNumberIsLessThan1() throws Exception {
            
            Ticket invalidTicket = new Ticket(
                    null,
                    1L,
                    0, // Invalid: less than minimum (1)
                    "John Doe"
            );

           
            mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidTicket)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(ticketService, never()).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when seat number is greater than 100")
        void shouldReturn400BadRequest_WhenSeatNumberIsGreaterThan100() throws Exception {
            
            Ticket invalidTicket = new Ticket(
                    null,
                    1L,
                    101, // Invalid: greater than maximum (100)
                    "John Doe"
            );

           
            mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidTicket)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(ticketService, never()).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when seat number is null")
        void shouldReturn400BadRequest_WhenSeatNumberIsNull() throws Exception {
            
            Ticket invalidTicket = new Ticket(
                    null,
                    1L,
                    null, // Invalid: null seat number
                    "John Doe"
            );

           
            mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidTicket)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(ticketService, never()).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should accept ticket with seat number at minimum boundary (1)")
        void shouldAcceptTicket_WithSeatNumberAtMinimumBoundary() throws Exception {
            
            Ticket validTicket = new Ticket(
                    null,
                    1L,
                    1, // Valid: exactly at minimum
                    "Jane Smith"
            );
            doNothing().when(ticketService).addTicket(any(Ticket.class));

           
            mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validTicket)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().string("Ticket was booked successfully."));

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should accept ticket with seat number at maximum boundary (100)")
        void shouldAcceptTicket_WithSeatNumberAtMaximumBoundary() throws Exception {
            
            Ticket validTicket = new Ticket(
                    null,
                    1L,
                    100, // Valid: exactly at maximum
                    "Bob Williams"
            );
            doNothing().when(ticketService).addTicket(any(Ticket.class));

           
            mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validTicket)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().string("Ticket was booked successfully."));

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }

        @Test
        @DisplayName("Should accept valid ticket with all required fields")
        void shouldAcceptValidTicket_WithAllRequiredFields() throws Exception {
            
            Ticket validTicket = new Ticket(
                    null,
                    2L,
                    50,
                    "Alice Johnson"
            );
            doNothing().when(ticketService).addTicket(any(Ticket.class));

           
            mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validTicket)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().string("Ticket was booked successfully."));

            verify(ticketService, times(1)).addTicket(any(Ticket.class));
        }
    }

    @Nested
    @DisplayName("GET /tickets/{id} - Get Ticket By Id Tests")
    class GetTicketByIdTests {

        @Test
        @DisplayName("Should return ticket with 200 OK when valid id provided")
        void shouldReturnTicket_WithStatus200() throws Exception {
            
            when(ticketService.getTicketById(1L)).thenReturn(testTicket);

           
            mockMvc.perform(get("/tickets/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.showtimeId", is(1)))
                    .andExpect(jsonPath("$.seatNumber", is(15)))
                    .andExpect(jsonPath("$.customerName", is("John Doe")));

            verify(ticketService, times(1)).getTicketById(1L);
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when ticket does not exist")
        void shouldReturn404NotFound_WhenTicketDoesNotExist() throws Exception {
            
            Long nonExistentId = 999L;
            when(ticketService.getTicketById(nonExistentId))
                    .thenThrow(new ResourceNotFoundException("ERROR: Ticket with id " + nonExistentId + " does not exist."));

           
            mockMvc.perform(get("/tickets/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(ticketService, times(1)).getTicketById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle different ticket IDs correctly")
        void shouldHandleDifferentTicketIds_Correctly() throws Exception {
            
            Long ticketId = 42L;
            Ticket differentTicket = new Ticket(
                    42L,
                    2L,
                    75,
                    "Alice Johnson"
            );
            when(ticketService.getTicketById(ticketId)).thenReturn(differentTicket);

           
            mockMvc.perform(get("/tickets/" + ticketId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(42)))
                    .andExpect(jsonPath("$.showtimeId", is(2)))
                    .andExpect(jsonPath("$.seatNumber", is(75)))
                    .andExpect(jsonPath("$.customerName", is("Alice Johnson")));

            verify(ticketService, times(1)).getTicketById(42L);
        }

        @Test
        @DisplayName("Should return correct content type")
        void shouldReturnCorrectContentType() throws Exception {
            
            when(ticketService.getTicketById(1L)).thenReturn(testTicket);

           
            mockMvc.perform(get("/tickets/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(ticketService, times(1)).getTicketById(1L);
        }

        @Test
        @DisplayName("Should verify service method is called with correct ID")
        void shouldVerifyServiceMethod_IsCalledWithCorrectId() throws Exception {
            
            Long ticketId = 77L;
            Ticket ticket = new Ticket(77L, 3L, 88, "Charlie Brown");
            when(ticketService.getTicketById(ticketId)).thenReturn(ticket);

            
            mockMvc.perform(get("/tickets/" + ticketId))
                    .andExpect(status().isOk());

            
            verify(ticketService, times(1)).getTicketById(77L);
            verify(ticketService, never()).getTicketById(argThat(id -> !id.equals(77L)));
        }

        @Test
        @DisplayName("Should return ticket with minimum seat number")
        void shouldReturnTicket_WithMinimumSeatNumber() throws Exception {
            
            Ticket ticketWithMinSeat = new Ticket(
                    10L,
                    1L,
                    1, // Minimum seat number
                    "Min Seat Customer"
            );
            when(ticketService.getTicketById(10L)).thenReturn(ticketWithMinSeat);

           
            mockMvc.perform(get("/tickets/10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.seatNumber", is(1)));

            verify(ticketService, times(1)).getTicketById(10L);
        }

        @Test
        @DisplayName("Should return ticket with maximum seat number")
        void shouldReturnTicket_WithMaximumSeatNumber() throws Exception {
            
            Ticket ticketWithMaxSeat = new Ticket(
                    20L,
                    1L,
                    100, // Maximum seat number
                    "Max Seat Customer"
            );
            when(ticketService.getTicketById(20L)).thenReturn(ticketWithMaxSeat);

           
            mockMvc.perform(get("/tickets/20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.seatNumber", is(100)));

            verify(ticketService, times(1)).getTicketById(20L);
        }
    }
}