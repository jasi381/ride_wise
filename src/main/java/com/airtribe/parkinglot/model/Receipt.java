package com.airtribe.parkinglot.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/** What the driver gets at check-out. Built from a closed transaction. */
public record Receipt(String ticketId, String plate, VehicleType vehicleType, String spotId,
                      Instant entryTime, Instant exitTime, Duration duration, BigDecimal fee) {

    public static Receipt from(ParkingTransaction closed) {
        return new Receipt(closed.id(), closed.vehicle().plate(), closed.vehicle().type(), closed.spot().id(),
                closed.entryTime(), closed.exitTime(), closed.duration(), closed.fee());
    }
}
