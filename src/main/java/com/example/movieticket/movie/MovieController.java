package com.example.movieticket.movie;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/all")
    public List<Movie> getAll() {
        return movieService.getMovies();
    }

    @PostMapping
    public ResponseEntity<Movie> addNewMovie(@Valid @RequestBody Movie movie) {
        movieService.addMovie(movie);
        return ResponseEntity.ok(movie);

    }

    @PutMapping("/update/{movieTitle}")
    public ResponseEntity<Movie> updateMovieInfo(
            @PathVariable String movieTitle,
            @Valid @RequestBody Movie movieDetails) {

        Movie updated = movieService.updateMovie(movieTitle, movieDetails);
        return ResponseEntity.ok(updated);

    }

    @DeleteMapping("/{movieTitle}")
    public ResponseEntity<String> deleteMovie(@PathVariable String movieTitle) {
        movieService.deleteMovie(movieTitle);
        return ResponseEntity.ok("Movie with title " + movieTitle + " was deleted successfully.");
    }
}


