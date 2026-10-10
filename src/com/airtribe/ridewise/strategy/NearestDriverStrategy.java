package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Rider;
import java.util.List;

public final class NearestDriverStrategy implements RideMatchingStrategy {
    @Override
    public Driver findDriver(Rider rider, List<Driver> drivers) {
        Driver best = null;
        double bestDistance = 0;
        for (Driver driver : drivers) {
            if (!driver.isAvailable()) continue;
            double distance = rider.getLocation().distanceTo(driver.getCurrentLocation());
            if (best == null || distance < bestDistance
                    || (distance == bestDistance && driver.getId().compareTo(best.getId()) < 0)) {
                best = driver;
                bestDistance = distance;
            }
        }
        return best;
    }
}
