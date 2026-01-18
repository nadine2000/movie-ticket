package com.example.movieticket.showtime;

import com.example.movieticket.exception.ResourceNotFoundException;
import com.example.movieticket.exception.ValidationException;
import com.example.movieticket.movie.MovieService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;
    private final MovieService movieService;

    public ShowtimeService(ShowtimeRepository showtimeRepository, MovieService movieService) {
        this.showtimeRepository = showtimeRepository;
        this.movieService = movieService;
    }

    public Showtime updateShowtime(Long id, Showtime showtimeDetails) {

        validateShowtimeExists(id);
        validateShowtime(showtimeDetails);

        return showtimeRepository.save(new Showtime(
                id,
                showtimeDetails.getMovieId(),
                showtimeDetails.getTheater(),
                showtimeDetails.getStartTime(),
                showtimeDetails.getEndTime(),
                showtimeDetails.getPrice()
        ));

    }

    public void addShowtime(Showtime showtime) {
        validateShowtime(showtime);
        showtimeRepository.save(showtime);
    }

    public void deleteShowtime(long id) {
        Showtime toDelete = getShowtimeById(id);
        showtimeRepository.delete(toDelete);
    }

    public void validateShowtimeExists(long id) {
        if  (!showtimeRepository.existsById(id)) {
            throw new ResourceNotFoundException("ERROR: Showtime with id " + id + " does not exist.");
        }
    }

    public Showtime getShowtimeById(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ERROR: Showtime with id " + id + " does not exist."));
    }


    private void validateShowtime(Showtime showtime) {

        if (!showtime.getEndTime().isAfter(showtime.getStartTime())) {
            throw new ValidationException("End time must be after start time");
        }

        movieService.validateMovieExists(showtime.getMovieId());

        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtime(
                showtime.getTheater(), showtime.getStartTime(), showtime.getEndTime());

        if (!overlapping.isEmpty()) {
            throw new ValidationException("Showtime overlaps with another showtime in the same theater");
        }
    }

}
