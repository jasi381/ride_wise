package com.airtribe.ridewise.service;

import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.FareReceipt;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.RideStatus;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;
import com.airtribe.ridewise.util.IdGenerator;
import java.util.List;

public final class RideService {
    private final RiderService riderService;
    private final DriverService driverService;
    private final RideRepository rides;
    private final IdGenerator ids;
    private RideMatchingStrategy matchingStrategy;
    private final FareCalculator fareCalculator;

    public RideService(RiderService riderService, DriverService driverService, RideRepository rides,
                       RideMatchingStrategy matchingStrategy, FareStrategy fareStrategy, IdGenerator ids) {
        this.riderService = riderService;
        this.driverService = driverService;
        this.rides = rides;
        this.matchingStrategy = matchingStrategy;
        this.fareCalculator = new FareCalculator(fareStrategy);
        this.ids = ids;
    }

    public Ride requestRide(String riderId, double distanceKm) {
        Rider rider = riderService.getRiderById(riderId);
        List<Driver> availableDrivers = driverService.listAvailableDrivers();
        Driver driver = matchingStrategy.findDriver(rider, availableDrivers);
        if (driver == null) throw new NoDriverAvailableException("No available driver could be matched.");
        Ride ride = new Ride(ids.nextId(), rider, distanceKm);
        rides.save(ride);
        ride.assign(driver);
        driver.startRide();
        return ride;
    }

    public FareReceipt completeRide(String rideId) {
        Ride ride = getRideById(rideId);
        if (ride.getStatus() != RideStatus.ASSIGNED) throw new IllegalStateException("Only assigned rides can be completed.");
        double amount = fareCalculator.calculateFare(ride);
        FareReceipt receipt = new FareReceipt(ride.getId(), amount);
        ride.complete(receipt);
        ride.getDriver().finishRide();
        return receipt;
    }

    public void cancelRide(String rideId) {
        Ride ride = getRideById(rideId);
        boolean assigned = ride.getStatus() == RideStatus.ASSIGNED;
        ride.cancel();
        if (assigned) ride.getDriver().finishRide();
    }

    public Ride getRideById(String id) {
        Ride ride = rides.findById(id);
        if (ride == null) throw new IllegalArgumentException("Ride not found: " + id);
        return ride;
    }

    public List<Ride> listRides() { return rides.findAll(); }
    public void setMatchingStrategy(RideMatchingStrategy strategy) { matchingStrategy = strategy; }
    public void setFareStrategy(FareStrategy strategy) { fareCalculator.setStrategy(strategy); }
}
