package com.example.movieticket.showtime;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Showtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Movie ID is required")
    @Positive(message = "Movie ID must be positive.")
    private Long movieId;

    @NotBlank(message = " Theater is required. ")
    @Size(min = 1, max = 200, message = "theater length between 1 and 200" )
    private String theater;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;


    @NotNull(message = "End time is required")
    private LocalDateTime endTime;


    @DecimalMin(value = "0.01", message = " Price must be greater than 0. ")
    @NotNull(message = " Price is required. ")
    private Double price;

}
