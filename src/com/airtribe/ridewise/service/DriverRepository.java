package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Driver;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DriverRepository {
    private final Map<String, Driver> drivers = new LinkedHashMap<>();

    public void save(Driver driver) { drivers.put(driver.getId(), driver); }
    public Driver findById(String id) { return drivers.get(id); }
    public List<Driver> findAll() { return new ArrayList<>(drivers.values()); }
}
