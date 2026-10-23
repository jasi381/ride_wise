package com.airtribe.parkinglot.exception;

public class VehicleAlreadyParkedException extends ParkingException {
    public VehicleAlreadyParkedException(String value) {
        super(String.format("Vehicle %s is already parked", value));
    }
}
