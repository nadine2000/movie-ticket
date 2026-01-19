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
        validateShowtime(showtimeDetails, id);
        showtimeDetails.setId(id);
        return showtimeRepository.save(showtimeDetails);

    }

    public void addShowtime(Showtime showtime) {
        validateShowtime(showtime, null);
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


    private void validateShowtime(Showtime showtime, Long excludeId) {

        if (!showtime.getEndTime().isAfter(showtime.getStartTime())) {
            throw new ValidationException("End time must be after start time");
        }

        movieService.validateMovieExists(showtime.getMovieId());

        List<Showtime> overlapping;

        if (excludeId == null) {
            overlapping = showtimeRepository.findOverlappingShowtime(
                    showtime.getTheater(), showtime.getStartTime(), showtime.getEndTime());
        }
        else {
            overlapping = showtimeRepository.findOverlappingShowtimeExcludingId(
                    showtime.getTheater(), showtime.getStartTime(), showtime.getEndTime(), excludeId);
        }

        if (!overlapping.isEmpty()) {
            throw new ValidationException("Showtime overlaps with another showtime in the same theater");
        }
    }

}
