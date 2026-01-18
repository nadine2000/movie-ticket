package com.example.movieticket.movie;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = " Title cannot be empty. ")
    @Size(min = 1, max = 400, message = "title length between 1 and 400" )
    private String title;

    @NotBlank(message = " Genre cannot be empty. ")
    @Size(min = 1, max = 200, message = "Genre length between 1 and 400" )
    private String genre;

    @Min(value = 15, message = " minimum movie duration is 15 min. ")
    @Max(value = 300, message = " maximum movie duration is 300 min (5 hours). ")
    @NotNull(message = " Duration is required. ")
    private Integer duration;

    @Min(value = 0, message = " minimum rating is 0. ")
    @Max(value = 10, message = " maximum rating  is 10. ")
    @NotNull(message = " Rating is required. ")
    private Integer rating;

    @Min(value = 1900, message = "Release year must be at least 1900")
    @Max(value = 2100, message = "Release year cannot be greater than 2100")
    @NotNull(message = " Release year is required. ")
    private Integer releaseYear;
}