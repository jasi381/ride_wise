package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Ride;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryRideRepository implements RideRepository {
    private final Map<String, Ride> rides = new LinkedHashMap<>();
    @Override public void save(Ride ride) { rides.put(ride.getId(), ride); }
    @Override public Optional<Ride> findById(String id) { return Optional.ofNullable(rides.get(id)); }
    @Override public List<Ride> findAll() { return new ArrayList<>(rides.values()); }
}
