package com.example.movieticket.ticket;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> bookTicket(@Valid @RequestBody Ticket ticket) {
        ticketService.addTicket(ticket);
        return ResponseEntity.ok(Map.of("bookingId", ticket.getId()));
    }
}
