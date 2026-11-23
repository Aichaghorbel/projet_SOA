package com.example.smartspace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ReservationOverlapException extends ResponseStatusException {
    public ReservationOverlapException() {
        super(HttpStatus.CONFLICT, "Reservation time overlaps with an existing reservation");
    }
}
