package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.util.IdGenerator;
import java.util.List;

public final class RiderService {
    private final RiderRepository riders;
    private final IdGenerator ids;

    public RiderService(RiderRepository riders, IdGenerator ids) {
        this.riders = riders;
        this.ids = ids;
    }

    public Rider registerRider(String name, Location location) {
        Rider rider = new Rider(ids.nextId(), name, location);
        riders.save(rider);
        return rider;
    }

    public Rider getRiderById(String id) {
        Rider rider = riders.findById(id);
        if (rider == null) throw new IllegalArgumentException("Rider not found: " + id);
        return rider;
    }

    public List<Rider> getAllRiders() { return riders.findAll(); }
}
