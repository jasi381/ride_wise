package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Rider;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RiderRepository {
    private final Map<String, Rider> riders = new LinkedHashMap<>();

    public void save(Rider rider) { riders.put(rider.getId(), rider); }
    public Rider findById(String id) { return riders.get(id); }
    public List<Rider> findAll() { return new ArrayList<>(riders.values()); }
}
