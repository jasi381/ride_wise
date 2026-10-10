package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Rider;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InMemoryRiderRepository implements RiderRepository {
    private final Map<String, Rider> riders = new LinkedHashMap<>();

    @Override public void save(Rider rider) { riders.put(rider.getId(), rider); }
    @Override public Rider findById(String id) { return riders.get(id); }
    @Override public List<Rider> findAll() { return new ArrayList<>(riders.values()); }
}
