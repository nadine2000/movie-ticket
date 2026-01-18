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

    public Movie updateMovie(Long id, Movie movieDetails) {

        validateMovieExists(id);

        return movieRepository.save(new Movie(
                    id,
                    movieDetails.getTitle(),
                    movieDetails.getGenre(),
                    movieDetails.getDuration(),
                    movieDetails.getRating(),
                    movieDetails.getReleaseYear()
                    ));

    }

    public void addMovie(Movie movie) {
        movieRepository.save(movie);
    }

    public void deleteMovie(long id) {
        Movie toDelete = getMovieById(id);
        movieRepository.delete(toDelete);
    }

    public List<Movie> getMovies() {
        return movieRepository.findAll();
    }

    public void validateMovieExists(long id) {
        if  (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("ERROR: Movie with id " + id + " does not exist.");
        }
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ERROR: Movie with id " + id + " does not exist."));
    }
}
