package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.strategy.FareStrategy;

public final class FareCalculator {
    private FareStrategy strategy;

    public FareCalculator(FareStrategy strategy) { this.strategy = strategy; }

    public double calculateFare(Ride ride) {
        double amount = strategy.calculateFare(ride);
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) {
            throw new IllegalStateException("Fare strategy returned an invalid fare.");
        }
        return Math.round(amount * 100.0) / 100.0;
    }

    public void setStrategy(FareStrategy strategy) { this.strategy = strategy; }
}
