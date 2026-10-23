package com.airtribe.parkinglot.strategy;

import com.airtribe.parkinglot.model.ParkingSpot;
import com.airtribe.parkinglot.model.SpotSize;
import com.airtribe.parkinglot.model.VehicleType;
import java.util.Map;
import java.util.Optional;

/** Decides which free spot a vehicle gets, and owns the record of which spots are free. */
public interface SpotAllocationStrategy {

    /** Atomically takes a free spot for the vehicle type, or returns empty when none fits. */
    Optional<ParkingSpot> allocate(VehicleType type);

    /** Gives a spot back. Releasing a spot that is already free is a no-op. */
    void release(ParkingSpot spot);

    /** Live count of free spots per size. */
    Map<SpotSize, Integer> freeSpots();
}
