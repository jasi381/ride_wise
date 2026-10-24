package com.airtribe.parkinglot.strategy;

import com.airtribe.parkinglot.model.VehicleType;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Same hourly rates, but each billed hour costs {@code multiplier} times more when it starts
 * inside the peak window. Which hours are peak is decided from the wall-clock time of each
 * hour, so a stay entirely off-peak costs the same as with {@link HourlyFeeStrategy}.
 */
public final class PeakHourFeeStrategy implements FeeStrategy {
    private final RateCard rates;
    private final Duration gracePeriod;
    private final LocalTime peakStart;
    private final LocalTime peakEnd;
    private final BigDecimal multiplier;
    private final ZoneId zone;

    public PeakHourFeeStrategy(RateCard rates, Duration gracePeriod, LocalTime peakStart, LocalTime peakEnd,
                               BigDecimal multiplier, ZoneId zone) {
        this.rates = rates;
        this.gracePeriod = gracePeriod;
        this.peakStart = peakStart;
        this.peakEnd = peakEnd;
        this.multiplier = multiplier;
        this.zone = zone;
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
        BigDecimal rate = rates.hourlyRate(type);
        BigDecimal total = BigDecimal.ZERO;
        long hours = HourlyFeeStrategy.startedHours(stay);
        for (long hour = 0; hour < hours; hour++) {
            LocalTime start = entry.plus(Duration.ofHours(hour)).atZone(zone).toLocalTime();
            total = total.add(isPeak(start) ? rate.multiply(multiplier) : rate);
        }
        return total;
    }

    /** The window may wrap past midnight, e.g. 22:00 to 02:00. */
    private boolean isPeak(LocalTime time) {
        if (peakStart.isBefore(peakEnd)) {
            return !time.isBefore(peakStart) && time.isBefore(peakEnd);
        }
        return !time.isBefore(peakStart) || time.isBefore(peakEnd);
    }
}
