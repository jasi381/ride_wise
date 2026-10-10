package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Ride;

public final class PeakHourFareStrategy implements FareStrategy {
    private final FareStrategy baseStrategy;
    private final double multiplier;

    public PeakHourFareStrategy() { this(new DefaultFareStrategy(), 1.5); }

    public PeakHourFareStrategy(FareStrategy baseStrategy, double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier < 1) {
            throw new IllegalArgumentException("Peak multiplier must be at least 1.");
        }
        this.baseStrategy = baseStrategy;
        this.multiplier = multiplier;
    }

    @Override
    public double calculateFare(Ride ride) { return baseStrategy.calculateFare(ride) * multiplier; }
}
