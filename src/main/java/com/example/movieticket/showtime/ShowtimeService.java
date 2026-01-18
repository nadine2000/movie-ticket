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
        Showtime oldShowTime = getShowtimeById(id);
        boolean timeDidNotChanged = oldShowTime.getStartTime().equals(showtimeDetails.getStartTime())
                && oldShowTime.getEndTime().equals(showtimeDetails.getEndTime());

        System.out.println("timeDidNotChanged: " + timeDidNotChanged);
        if ((timeDidNotChanged)) {
            validateShowtime(showtimeDetails, id);
        } else {
            validateShowtime(showtimeDetails, null);
        }

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

// todo: remove
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

        if (excludeId == null) {
            List<Showtime> overlapping = showtimeRepository.findOverlappingShowtime(
                    showtime.getTheater(), showtime.getStartTime(), showtime.getEndTime());

            if (!overlapping.isEmpty()) {
                throw new ValidationException("Showtime overlaps with another showtime in the same theater");
            }
        }
    }

}
