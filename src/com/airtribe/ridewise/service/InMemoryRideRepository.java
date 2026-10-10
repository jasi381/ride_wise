package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Ride;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InMemoryRideRepository implements RideRepository {
    private final Map<String, Ride> rides = new LinkedHashMap<>();

    @Override public void save(Ride ride) { rides.put(ride.getId(), ride); }
    @Override public Ride findById(String id) { return rides.get(id); }
    @Override public List<Ride> findAll() { return new ArrayList<>(rides.values()); }
}
