package com.example.movieticket.showtimeTest;

import com.example.movieticket.showtime.Showtime;
import com.example.movieticket.showtime.ShowtimeRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("Showtime Repository Tests")
public class ShowtimeRepositoryTest {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Showtime defaultShowtime;

    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
        defaultShowtime = createShowtime("Theater A", 1L, startTime, endTime, 10.0);
    }

    private Showtime createShowtime(String theater, Long movieId, LocalDateTime start, LocalDateTime end, Double price) {
        Showtime s = new Showtime();
        s.setTheater(theater);
        s.setMovieId(movieId);
        s.setStartTime(start);
        s.setEndTime(end);
        s.setPrice(price);
        return s;
    }

    @Test
    @DisplayName("save a showtime to database")
    void testSaveShowtime() {
        Showtime saved = showtimeRepository.save(defaultShowtime);

        assertThat(saved)
                .isNotNull()
                .hasFieldOrPropertyWithValue("movieId", 1L)
                .hasFieldOrPropertyWithValue("startTime", startTime)
                .hasFieldOrPropertyWithValue("endTime", endTime)
                .hasFieldOrPropertyWithValue("theater", "Theater A")
                .hasFieldOrPropertyWithValue("price", 10.0);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should retrieve a showtime by ID")
    void testFindShowtimeById() {
        Showtime saved = showtimeRepository.save(defaultShowtime);

        Optional<Showtime> found = showtimeRepository.findById(saved.getId());

        assertThat(found).isPresent()
                .contains(saved);
    }

    @Test
    @DisplayName("Should detect overlapping showtime in same theater")
    void testFindOverlappingShowtime() {
        Showtime s1 = createShowtime("Theater A", 1L, startTime, endTime, 10.0);
        Showtime s2 = createShowtime("Theater A", 2L, startTime.plusMinutes(30), endTime.plusMinutes(30), 12.0);

        showtimeRepository.saveAll(List.of(s1, s2));

        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtime("Theater A", startTime, endTime);

        assertThat(overlapping).isNotEmpty()
                .contains(s2);
    }

    @Test
    @DisplayName("Should not detect overlapping showtimes in different theaters")
    void testNoOverlapDifferentTheaters() {
        Showtime s1 = createShowtime("Theater A", 1L, startTime, endTime, 10.0);
        Showtime s2 = createShowtime("Theater B", 1L, startTime, endTime, 12.0);

        showtimeRepository.save(s1);

        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtime(s2.getTheater(), s2.getStartTime(),
                s2.getEndTime());

        assertThat(overlapping).isEmpty();
    }

    @Test
    @DisplayName("Should not detect non-overlapping showtime in same theater")
    void testNoOverlapSameTheater() {
        Showtime s1 = createShowtime("Theater A", 1L, startTime, endTime, 10.0);
        Showtime s2 = createShowtime("Theater A", 7L, endTime.plusHours(1), endTime.plusHours(3), 12.0);

        showtimeRepository.saveAll(List.of(s1, s2));

        Showtime s3 = createShowtime("Theater A", 5L, endTime.plusHours(5), endTime.plusHours(7), 19.0);

        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtime(s3.getTheater(), s3.getStartTime(),
                s3.getEndTime());

        assertThat(overlapping).isEmpty();
    }

    @Test
    @DisplayName("Should exclude specified showtime when checking for overlaps")
    void testFindOverlappingShowtimesExcluding() {
        Showtime s1 = createShowtime("Theater A", 1L, startTime, endTime, 10.0);
        Showtime s2 = createShowtime("Theater A", 2L, startTime.plusMinutes(30), endTime.plusMinutes(30), 12.0);

        Showtime saved1 = showtimeRepository.save(s1);
        Showtime saved2 = showtimeRepository.save(s2);

        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtime(
                "Theater A", startTime, endTime
        );

        assertThat(overlapping).contains(saved2);
        assertThat(overlapping).contains(saved1);
    }

    @Test
    @DisplayName("update a showtime")
    void testUpdateShowtime() {
        Showtime saved = showtimeRepository.save(defaultShowtime);
        saved.setPrice(15.0);
        Showtime updated = showtimeRepository.save(saved);
        assertThat(updated).hasFieldOrPropertyWithValue("price", 15.0);
    }

    @Test
    @DisplayName("delete a showtime")
    void testDeleteShowtime() {
        Showtime saved = showtimeRepository.save(defaultShowtime);
        Long id = saved.getId();
        showtimeRepository.delete(saved);
        assertThat(showtimeRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException for invalid showtime values")
    void testInvalidShowtimeValues() {

        Showtime invalid = createShowtime(" ", 2L, startTime.plusMinutes(30), null, -12.0);


        assertThatThrownBy(() -> showtimeRepository.saveAndFlush(invalid))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Theater")
                .hasMessageContaining("price")
                .hasMessageContaining("endTime");
    }
}
