package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Rider;
import java.util.List;
import java.util.Optional;

public interface RiderRepository {
    void save(Rider rider);
    Optional<Rider> findById(String id);
    List<Rider> findAll();
}
