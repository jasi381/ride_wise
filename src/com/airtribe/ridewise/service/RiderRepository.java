package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Rider;
import java.util.List;

public interface RiderRepository {
    void save(Rider rider);
    Rider findById(String id);
    List<Rider> findAll();
}
