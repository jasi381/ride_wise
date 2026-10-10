package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Ride;

public final class DefaultFareStrategy implements FareStrategy {
    private final double baseFare;
    private final double perKilometre;

    public DefaultFareStrategy() { this(40.0, 12.0); }

    public DefaultFareStrategy(double baseFare, double perKilometre) {
        if (!Double.isFinite(baseFare) || baseFare < 0 || !Double.isFinite(perKilometre) || perKilometre < 0) {
            throw new IllegalArgumentException("Fare rates must be finite and non-negative.");
        }
        this.baseFare = baseFare;
        this.perKilometre = perKilometre;
    }

    @Override
    public double calculateFare(Ride ride) { return baseFare + perKilometre * ride.getDistance(); }
}
