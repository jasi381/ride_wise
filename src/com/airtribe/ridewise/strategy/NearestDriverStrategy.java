package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Rider;
import java.util.Comparator;
import java.util.List;

public final class NearestDriverStrategy implements RideMatchingStrategy {
    @Override
    public Driver findDriver(Rider rider, List<Driver> drivers) {
        return drivers.stream()
                .filter(Driver::isAvailable)
                .min(Comparator.<Driver>comparingDouble(driver -> rider.getLocation().distanceTo(driver.getCurrentLocation()))
                        .thenComparing(Driver::getId))
                .orElse(null);
    }
}
