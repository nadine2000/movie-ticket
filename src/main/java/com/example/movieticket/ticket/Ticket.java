package com.example.movieticket.ticket;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue
    private UUID id;

    @NotNull(message = "Showtime ID is required")
    @Positive(message = "Showtime ID must be positive.")
    private Long showtimeId;

    @Min(value = 1, message = " minimum seat number is 1. ")
    @Max(value = 100, message = " maximum seat number is 100. ")
    @NotNull(message = " seat number is required. ")
    private Integer seatNumber;

    @NotNull(message = "User id is required.")
    private UUID userId;
}
