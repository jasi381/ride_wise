package com.airtribe.parkinglot.exception;

public class TicketAlreadyClosedException extends ParkingException {
    public TicketAlreadyClosedException(String value) {
        super(String.format("Ticket %s has already been checked out", value));
    }
}
