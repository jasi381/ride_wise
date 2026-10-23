package com.airtribe.parkinglot.strategy;

import com.airtribe.parkinglot.model.ParkingSpot;
import com.airtribe.parkinglot.model.SpotSize;
import com.airtribe.parkinglot.model.VehicleType;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Smallest spot that fits wins, lowest floor then lowest number within a size. Big spots are
 * only handed to small vehicles once their own size is full.
 *
 * Free spots sit in one sorted set per size. Taking a spot is {@code pollFirst()}, which is
 * atomic, so two threads can never receive the same spot and no lock is needed. Allocation is
 * O(log n) per size instead of a scan over every spot.
 */
public final class BestFitSpotAllocation implements SpotAllocationStrategy {
    private final Map<SpotSize, ConcurrentSkipListSet<ParkingSpot>> freeBySize = new EnumMap<>(SpotSize.class);

    public BestFitSpotAllocation(Collection<ParkingSpot> spots) {
        for (SpotSize size : SpotSize.values()) {
            freeBySize.put(size, new ConcurrentSkipListSet<>());
        }
        for (ParkingSpot spot : spots) {
            if (!freeBySize.get(spot.size()).add(spot)) {
                throw new IllegalArgumentException("Duplicate spot " + spot.id());
            }
        }
    }

    @Override
    public Optional<ParkingSpot> allocate(VehicleType type) {
        for (SpotSize size : SpotSize.values()) {
            if (!type.fitsIn(size)) {
                continue;
            }
            ParkingSpot spot = freeBySize.get(size).pollFirst();
            if (spot != null) {
                return Optional.of(spot);
            }
        }
        return Optional.empty();
    }

    @Override
    public void release(ParkingSpot spot) {
        freeBySize.get(spot.size()).add(spot);
    }

    @Override
    public Map<SpotSize, Integer> freeSpots() {
        Map<SpotSize, Integer> counts = new EnumMap<>(SpotSize.class);
        freeBySize.forEach((size, free) -> counts.put(size, free.size()));
        return counts;
    }
}
