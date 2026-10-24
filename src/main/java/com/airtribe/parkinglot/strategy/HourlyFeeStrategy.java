package com.airtribe.parkinglot.strategy;

import com.airtribe.parkinglot.model.VehicleType;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * Flat hourly rate for the vehicle type. Stays up to the grace period are free; beyond that
 * every started hour is billed in full.
 */
public final class HourlyFeeStrategy implements FeeStrategy {
    private final RateCard rates;
    private final Duration gracePeriod;

    public HourlyFeeStrategy(RateCard rates, Duration gracePeriod) {
        this.rates = rates;
        this.gracePeriod = gracePeriod;
    }

    @Override
    public BigDecimal calculate(VehicleType type, Instant entry, Instant exit) {
        Duration stay = Duration.between(entry, exit);
        if (stay.isNegative()) {
            throw new IllegalArgumentException("Exit time is before entry time");
        }
        if (stay.compareTo(gracePeriod) <= 0) {
            return BigDecimal.ZERO;
        }
        return rates.hourlyRate(type).multiply(BigDecimal.valueOf(startedHours(stay)));
    }

    /** Every started hour counts, and a stay past the grace period is at least one hour. */
    static long startedHours(Duration stay) {
        long seconds = stay.toSeconds() + (stay.toNanosPart() > 0 ? 1 : 0);
        return Math.max(1, (seconds + 3599) / 3600);
    }
}
