package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Ride;
import java.util.List;
import java.util.Optional;

public interface RideRepository {
    void save(Ride ride);
    Optional<Ride> findById(String id);
    List<Ride> findAll();
}
