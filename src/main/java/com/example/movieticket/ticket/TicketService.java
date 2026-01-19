package com.example.movieticket.ticket;


import com.example.movieticket.exception.ValidationException;
import com.example.movieticket.showtime.ShowtimeService;
import org.springframework.stereotype.Service;


@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final ShowtimeService showtimeService;

    public TicketService(TicketRepository ticketRepository, ShowtimeService showtimeService) {
        this.ticketRepository = ticketRepository;
        this.showtimeService = showtimeService;
    }

    public void addTicket(Ticket ticket) {
        validateTicket(ticket);
        ticketRepository.save(ticket);
    }

    private void validateTicket(Ticket ticket) {
        showtimeService.validateShowtimeExists(ticket.getShowtimeId());
        boolean bookedSeat = ticketRepository.existsByShowtimeIdAndSeatNumber(ticket.getShowtimeId(), ticket.getSeatNumber());
        if (bookedSeat) {
            throw new ValidationException("The wanted seat is already booked! choose anther seat.");
        }
    }

}
