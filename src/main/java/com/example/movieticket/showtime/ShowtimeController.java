package com.example.movieticket.showtime;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping("/{id}")
    public Showtime getShowtimeById(@PathVariable Long id) {
        return showtimeService.getShowtimeById(id);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Showtime> updateShowtimeInfo(
            @PathVariable Long id,
            @Valid @RequestBody Showtime showtimeDetails) {

        Showtime updated = showtimeService.updateShowtime(id, showtimeDetails);
        return ResponseEntity.ok(updated);

    }

    @PostMapping
    public ResponseEntity<Showtime> addNewShowtime(@Valid @RequestBody Showtime showtime) {
        showtimeService.addShowtime(showtime);
        return ResponseEntity.ok(showtime);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.ok("Showtime with id " + id + " was deleted successfully.");
    }
}
