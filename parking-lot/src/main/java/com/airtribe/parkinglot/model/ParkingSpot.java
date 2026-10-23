package com.airtribe.parkinglot.model;

import java.util.Comparator;

/**
 * A physical bay. Immutable: whether it is free is tracked by the allocator, not by a flag
 * on the spot, so nothing can flip availability behind the service's back.
 */
public record ParkingSpot(int floor, int number, SpotSize size) implements Comparable<ParkingSpot> {
    private static final Comparator<ParkingSpot> ORDER =
            Comparator.comparingInt(ParkingSpot::floor).thenComparingInt(ParkingSpot::number);

    public ParkingSpot {
        if (floor < 0 || number < 1) {
            throw new IllegalArgumentException("Floor must be >= 0 and spot number >= 1");
        }
        if (size == null) {
            throw new IllegalArgumentException("Spot size is required");
        }
    }

    public String id() {
        return "F" + floor + "-S" + number;
    }

    @Override
    public int compareTo(ParkingSpot other) {
        return ORDER.compare(this, other);
    }
}
