package com.airtribe.parkinglot.service;

import com.airtribe.parkinglot.model.ParkingSpot;
import com.airtribe.parkinglot.model.SpotSize;
import java.util.ArrayList;
import java.util.List;

/** Builds the spot list for a lot: how many spots of each size on each floor. */
public final class ParkingLotLayout {
    private final List<ParkingSpot> spots = new ArrayList<>();
    private int nextNumber;
    private int currentFloor = -1;

    public ParkingLotLayout floor(int floor) {
        currentFloor = floor;
        nextNumber = 1;
        return this;
    }

    public ParkingLotLayout spots(SpotSize size, int count) {
        if (currentFloor < 0) {
            throw new IllegalStateException("Call floor(...) before adding spots");
        }
        for (int i = 0; i < count; i++) {
            spots.add(new ParkingSpot(currentFloor, nextNumber++, size));
        }
        return this;
    }

    public List<ParkingSpot> build() {
        return List.copyOf(spots);
    }
}
