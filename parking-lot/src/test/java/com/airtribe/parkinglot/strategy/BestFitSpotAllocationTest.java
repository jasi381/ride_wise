package com.airtribe.parkinglot.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.airtribe.parkinglot.model.ParkingSpot;
import com.airtribe.parkinglot.model.SpotSize;
import com.airtribe.parkinglot.model.VehicleType;
import com.airtribe.parkinglot.service.ParkingLotLayout;
import java.util.List;
import org.junit.jupiter.api.Test;

class BestFitSpotAllocationTest {
    private static BestFitSpotAllocation lot(int small, int medium, int large) {
        return new BestFitSpotAllocation(new ParkingLotLayout()
                .floor(0).spots(SpotSize.SMALL, small).spots(SpotSize.MEDIUM, medium)
                .floor(1).spots(SpotSize.MEDIUM, medium).spots(SpotSize.LARGE, large)
                .build());
    }

    @Test
    void eachVehicleTypeGetsTheSmallestSpotThatFits() {
        BestFitSpotAllocation lot = lot(1, 1, 1);
        assertEquals(SpotSize.SMALL, lot.allocate(VehicleType.MOTORCYCLE).orElseThrow().size());
        assertEquals(SpotSize.MEDIUM, lot.allocate(VehicleType.CAR).orElseThrow().size());
        assertEquals(SpotSize.LARGE, lot.allocate(VehicleType.BUS).orElseThrow().size());
    }

    @Test
    void lowestFloorAndNumberAreUsedFirst() {
        BestFitSpotAllocation lot = lot(0, 2, 0);
        assertEquals("F0-S1", lot.allocate(VehicleType.CAR).orElseThrow().id());
        assertEquals("F0-S2", lot.allocate(VehicleType.CAR).orElseThrow().id());
        assertEquals("F1-S1", lot.allocate(VehicleType.CAR).orElseThrow().id());
    }

    @Test
    void biggerSpotsOverflowToSmallerVehiclesOnlyWhenTheirOwnSizeIsFull() {
        BestFitSpotAllocation lot = lot(1, 0, 1);
        assertEquals(SpotSize.SMALL, lot.allocate(VehicleType.MOTORCYCLE).orElseThrow().size());
        assertEquals(SpotSize.LARGE, lot.allocate(VehicleType.MOTORCYCLE).orElseThrow().size());
    }

    @Test
    void busNeverTakesASmallerSpot() {
        BestFitSpotAllocation lot = lot(3, 3, 0);
        assertTrue(lot.allocate(VehicleType.BUS).isEmpty());
        assertEquals(3, lot.freeSpots().get(SpotSize.SMALL));
    }

    @Test
    void releasedSpotIsAllocatedAgain() {
        BestFitSpotAllocation lot = lot(1, 0, 0);
        ParkingSpot spot = lot.allocate(VehicleType.MOTORCYCLE).orElseThrow();
        assertTrue(lot.allocate(VehicleType.MOTORCYCLE).isEmpty());
        lot.release(spot);
        assertEquals(spot, lot.allocate(VehicleType.MOTORCYCLE).orElseThrow());
    }

    @Test
    void releasingTwiceDoesNotCreateAPhantomSpot() {
        BestFitSpotAllocation lot = lot(1, 0, 0);
        ParkingSpot spot = lot.allocate(VehicleType.MOTORCYCLE).orElseThrow();
        lot.release(spot);
        lot.release(spot);
        assertEquals(1, lot.freeSpots().get(SpotSize.SMALL));
    }

    @Test
    void duplicateSpotsAreRejectedAtSetup() {
        ParkingSpot spot = new ParkingSpot(0, 1, SpotSize.SMALL);
        assertThrows(IllegalArgumentException.class, () -> new BestFitSpotAllocation(List.of(spot, spot)));
    }
}
