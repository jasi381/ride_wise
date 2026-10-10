package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Ride;
import java.util.List;

public interface RideRepository {
    void save(Ride ride);
    Ride findById(String id);
    List<Ride> findAll();
}
