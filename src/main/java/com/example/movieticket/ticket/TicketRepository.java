package com.example.movieticket.ticket;


import org.springframework.data.jpa.repository.JpaRepository;
// not long uuid
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    boolean existsByShowtimeIdAndSeatNumber(Long showtimeId, Integer seatNumber);
}
