package com.example.movieticket.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private Object message;
    private String path;

    public ErrorResponse(LocalDateTime timestamp, HttpStatus status, String error, Object message, String path) {
        this.timestamp = timestamp;
        this.status = status.value();
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
