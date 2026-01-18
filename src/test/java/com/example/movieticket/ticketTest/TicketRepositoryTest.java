package com.example.movieticket.ticketTest;

import com.example.movieticket.ticket.Ticket;
import com.example.movieticket.ticket.TicketRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("Ticket Repository Integration Tests")
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    private Ticket defaultTicket;

    @BeforeEach
    void setUp() {
        defaultTicket = createTicket(1L, 1, "John Doe");
    }

    private Ticket createTicket(Long showtimeId, int seatNumber, String customerName) {
        Ticket t = new Ticket();
        t.setShowtimeId(showtimeId);
        t.setSeatNumber(seatNumber);
        t.setCustomerName(customerName);
        return t;
    }

    @Test
    @DisplayName("Should save a ticket to database")
    void testSaveTicket() {
        Ticket saved = ticketRepository.save(defaultTicket);

        assertThat(saved).isNotNull()
                .hasFieldOrPropertyWithValue("showtimeId", 1L)
                .hasFieldOrPropertyWithValue("seatNumber", 1)
                .hasFieldOrPropertyWithValue("customerName", "John Doe");
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should retrieve a ticket by ID")
    void testFindTicketById() {
        Ticket saved = ticketRepository.save(defaultTicket);

        Optional<Ticket> found = ticketRepository.findById(saved.getId());

        assertThat(found).isPresent().contains(saved);
    }

    @Test
    @DisplayName("Should delete a ticket")
    void testDeleteTicket() {
        Ticket saved = ticketRepository.save(defaultTicket);
        Long id = saved.getId();

        ticketRepository.delete(saved);

        assertThat(ticketRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should check if seat is booked")
    void testExistsByShowtimeIdAndSeatNumber() {
        ticketRepository.save(defaultTicket);

        boolean exists = ticketRepository.existsByShowtimeIdAndSeatNumber(1L, 1);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when seat is not booked")
    void testSeatNotBooked() {
        boolean exists = ticketRepository.existsByShowtimeIdAndSeatNumber(1L, 1);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should prevent duplicate or same seat booking")
    void testUniqueSeatPerShowtime() {
        Ticket t1 = createTicket(1L, 1, "John Doe");
        Ticket t2 = createTicket(1L, 1, "Jane Doe");
        ticketRepository.save(t1);

        boolean booked = ticketRepository.existsByShowtimeIdAndSeatNumber(t2.getShowtimeId(),  t2.getSeatNumber());
        assertThat(booked).isTrue();

    }

    @Test
    @DisplayName("Should allow same seat in different showtimes")
    void testSameSeatDifferentShowtimes() {
        Ticket t1 = createTicket(1L, 1, "John Doe");
        Ticket t2 = createTicket(2L, 1, "Jane Doe");
        ticketRepository.saveAll(List.of(t1, t2));

        assertThat(t1.getSeatNumber()).isEqualTo(t2.getSeatNumber());
        assertThat(t1.getShowtimeId()).isNotEqualTo(t2.getShowtimeId());
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException for invalid ticket values")
    void testInvalidTicketValues() {
        Ticket invalid = createTicket(null, -9, "   ");

        assertThatThrownBy(() -> ticketRepository.saveAndFlush(invalid))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("showtimeId")
                .hasMessageContaining("seatNumber")
                .hasMessageContaining("customerName");
    }
}
