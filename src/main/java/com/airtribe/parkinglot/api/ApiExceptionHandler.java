package com.airtribe.parkinglot.api;

import com.airtribe.parkinglot.exception.ParkingException;
import com.airtribe.parkinglot.exception.TicketNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(TicketNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(ParkingException e) {
        return error(HttpStatus.NOT_FOUND, e);
    }

    /** Lot full, vehicle already parked, ticket already closed: the request conflicts with current state. */
    @ExceptionHandler(ParkingException.class)
    ResponseEntity<Map<String, String>> conflict(ParkingException e) {
        return error(HttpStatus.CONFLICT, e);
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, ParkingException e) {
        return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
    }
}
