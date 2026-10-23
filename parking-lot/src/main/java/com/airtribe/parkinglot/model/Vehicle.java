package com.airtribe.parkinglot.model;

import java.util.Locale;

public record Vehicle(String plate, VehicleType type) {
    public Vehicle {
        if (plate == null || plate.isBlank()) {
            throw new IllegalArgumentException("Plate number is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type is required");
        }
        plate = plate.trim().toUpperCase(Locale.ROOT);
    }
}
