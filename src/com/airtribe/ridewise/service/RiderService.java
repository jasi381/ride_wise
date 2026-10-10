package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.util.IdGenerator;

public final class RiderService {
    private final RiderRepository riders;
    private final IdGenerator ids;

    public RiderService(RiderRepository riders, IdGenerator ids) {
        this.riders = java.util.Objects.requireNonNull(riders);
        this.ids = java.util.Objects.requireNonNull(ids);
    }

    public Rider registerRider(String name, Location location) {
        Rider rider = new Rider(ids.nextId(), name, location);
        riders.save(rider);
        return rider;
    }

    public Rider getRiderById(String id) {
        return riders.findById(id).orElseThrow(() -> new IllegalArgumentException("Rider not found: " + id));
    }

    public java.util.List<Rider> getAllRiders() { return riders.findAll(); }
}
