package com.airtribe.parkinglot.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/** One stay in the lot, from check-in to check-out. Immutable; closing returns a new instance. */
public record ParkingTransaction(
        String id,
        Vehicle vehicle,
        ParkingSpot spot,
        Instant entryTime,
        Instant exitTime,
        BigDecimal fee,
        TransactionStatus status) {

    public static ParkingTransaction open(String id, Vehicle vehicle, ParkingSpot spot, Instant entryTime) {
        return new ParkingTransaction(id, vehicle, spot, entryTime, null, null, TransactionStatus.ACTIVE);
    }

    public ParkingTransaction close(Instant exitTime, BigDecimal fee) {
        if (status == TransactionStatus.CLOSED) {
            throw new IllegalStateException("Transaction " + id + " is already closed");
        }
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time is before entry time");
        }
        return new ParkingTransaction(id, vehicle, spot, entryTime, exitTime, fee, TransactionStatus.CLOSED);
    }

    public boolean isActive() {
        return status == TransactionStatus.ACTIVE;
    }

    public Duration duration() {
        if (exitTime == null) {
            throw new IllegalStateException("Transaction " + id + " is still active");
        }
        return Duration.between(entryTime, exitTime);
    }
}
