package com.airtribe.parkinglot.strategy;

import com.airtribe.parkinglot.model.VehicleType;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/** Hourly rate per vehicle type. Every type must have a rate, so none can be silently free. */
public final class RateCard {
    private final Map<VehicleType, BigDecimal> hourlyRates = new EnumMap<>(VehicleType.class);

    public RateCard(Map<VehicleType, BigDecimal> rates) {
        for (VehicleType type : VehicleType.values()) {
            BigDecimal rate = rates.get(type);
            if (rate == null || rate.signum() < 0) {
                throw new IllegalArgumentException("Missing or negative hourly rate for " + type);
            }
            hourlyRates.put(type, rate);
        }
    }

    public static RateCard standard() {
        return new RateCard(Map.of(
                VehicleType.MOTORCYCLE, BigDecimal.valueOf(10),
                VehicleType.CAR, BigDecimal.valueOf(20),
                VehicleType.BUS, BigDecimal.valueOf(50)));
    }

    public BigDecimal hourlyRate(VehicleType type) {
        return hourlyRates.get(type);
    }
}
