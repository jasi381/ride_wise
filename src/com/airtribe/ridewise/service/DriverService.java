package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.util.IdGenerator;
import java.util.ArrayList;
import java.util.List;

public final class DriverService {
    private final DriverRepository drivers;
    private final IdGenerator ids;

    public DriverService(DriverRepository drivers, IdGenerator ids) {
        this.drivers = drivers;
        this.ids = ids;
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
        Driver driver = drivers.findById(id);
        if (driver == null) throw new IllegalArgumentException("Driver not found: " + id);
        return driver;
    }

    public List<Driver> listAvailableDrivers() {
        List<Driver> available = new ArrayList<>();
        for (Driver driver : drivers.findAll()) {
            if (driver.isAvailable()) available.add(driver);
        }
        return available;
    }
}
