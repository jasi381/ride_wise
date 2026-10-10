package com.airtribe.ridewise.model;

import java.time.Instant;

public final class FareReceipt {
    private final String rideId;
    private final double amount;
    private final Instant generatedAt;

    public FareReceipt(String rideId, double amount) {
        if (rideId == null || rideId.trim().isEmpty()) throw new IllegalArgumentException("Ride ID is required.");
        if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Fare must be non-negative.");
        this.rideId = rideId;
        this.amount = amount;
        this.generatedAt = Instant.now();
    }

    public String getRideId() { return rideId; }
    public double getAmount() { return amount; }
    public Instant getGeneratedAt() { return generatedAt; }

    @Override
    public String toString() { return "Receipt for " + rideId + ": " + String.format("%.2f", amount) + " at " + generatedAt; }
}
