package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.util.IdGenerator;
import java.util.List;
import java.util.stream.Collectors;

public final class DriverService {
    private final DriverRepository drivers;
    private final IdGenerator ids;

    public DriverService(DriverRepository drivers, IdGenerator ids) {
        this.drivers = java.util.Objects.requireNonNull(drivers);
        this.ids = java.util.Objects.requireNonNull(ids);
    }

    public Driver registerDriver(String name, Location location, VehicleType vehicleType) {
        Driver driver = new Driver(ids.nextId(), name, location, vehicleType);
        drivers.save(driver);
        return driver;
    }

    public void updateAvailability(String driverId, boolean available) {
        Driver driver = getDriverById(driverId);
        driver.setAvailable(available);
    }

    public Driver getDriverById(String id) {
        return drivers.findById(id).orElseThrow(() -> new IllegalArgumentException("Driver not found: " + id));
    }

    public List<Driver> listAvailableDrivers() {
        return drivers.findAll().stream().filter(Driver::isAvailable).collect(Collectors.toList());
    }
}
