package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Driver;
import java.util.List;

public interface DriverRepository {
    void save(Driver driver);
    Driver findById(String id);
    List<Driver> findAll();
}
