package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Rider;
import java.util.List;

public final class LeastActiveDriverStrategy implements RideMatchingStrategy {
    @Override
    public Driver findDriver(Rider rider, List<Driver> drivers) {
        Driver best = null;
        for (Driver driver : drivers) {
            if (!driver.isAvailable()) continue;
            if (best == null || driver.getTotalRideCount() < best.getTotalRideCount()
                    || (driver.getTotalRideCount() == best.getTotalRideCount()
                        && driver.getId().compareTo(best.getId()) < 0)) {
                best = driver;
            }
        }
        return best;
    }
}
