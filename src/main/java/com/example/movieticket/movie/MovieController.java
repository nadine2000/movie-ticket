package com.example.movieticket.movie;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovieInfo(
            @PathVariable Long id,
            @Valid @RequestBody Movie movieDetails) {

        Movie updated = movieService.updateMovie(id, movieDetails);
        return ResponseEntity.ok(updated);

    }

    @PostMapping
    public ResponseEntity<String> addNewMovie(@Valid @RequestBody Movie movie) {
        movieService.addMovie(movie);
        return ResponseEntity.status(HttpStatus.CREATED).body("Movie was added successfully.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok("Movie with id " + id + " was deleted successfully.");
    }

    @GetMapping
    public List<Movie> getAll() {
        return movieService.getMovies();
    }
}
