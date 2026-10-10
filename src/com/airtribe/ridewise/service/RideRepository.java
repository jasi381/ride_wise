package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Ride;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RideRepository {
    private final Map<String, Ride> rides = new LinkedHashMap<>();

    public void save(Ride ride) { rides.put(ride.getId(), ride); }
    public Ride findById(String id) { return rides.get(id); }
    public List<Ride> findAll() { return new ArrayList<>(rides.values()); }
}
