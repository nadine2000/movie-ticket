package com.example.movieticket.movie;

import com.example.movieticket.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Movie updateMovie(String movieTitle, Movie movieDetails) {
        Movie existingMovie = getMovieByTitle(movieTitle);
        Long existingId = existingMovie.getId();
        movieDetails.setId(existingId);
        return movieRepository.save(movieDetails);
    }

    public void addMovie(Movie movie) {
        movieRepository.save(movie);
    }

    public void deleteMovie(String movieTitle) {
        Movie toDelete = getMovieByTitle(movieTitle);
        movieRepository.delete(toDelete);
    }

    public List<Movie> getMovies() {
        return movieRepository.findAll();
    }

    public Movie getMovieByTitle(String movieTitle) {
        return movieRepository.findByTitle(movieTitle)
                .orElseThrow(() -> new ResourceNotFoundException("ERROR: Movie with title " + movieTitle + " does not exist."));
    }

    public void validateMovieExists(long id) {
        if  (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("ERROR: Movie with id " + id + " does not exist.");
        }
    }

}
