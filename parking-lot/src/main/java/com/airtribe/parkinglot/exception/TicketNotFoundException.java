package com.airtribe.parkinglot.exception;

public class TicketNotFoundException extends ParkingException {
    public TicketNotFoundException(String value) {
        super(String.format("No ticket with id %s", value));
    }
}
