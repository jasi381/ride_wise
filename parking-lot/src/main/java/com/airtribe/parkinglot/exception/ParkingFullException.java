package com.airtribe.parkinglot.exception;

import com.airtribe.parkinglot.model.VehicleType;

public class ParkingFullException extends ParkingException {
    public ParkingFullException(VehicleType type) {
        super(String.format("No free spot for a %s", type));
    }
}
