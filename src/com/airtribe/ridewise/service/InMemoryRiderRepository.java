package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Rider;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryRiderRepository implements RiderRepository {
    private final Map<String, Rider> riders = new LinkedHashMap<>();
    @Override public void save(Rider rider) { riders.put(rider.getId(), rider); }
    @Override public Optional<Rider> findById(String id) { return Optional.ofNullable(riders.get(id)); }
    @Override public List<Rider> findAll() { return new ArrayList<>(riders.values()); }
}
