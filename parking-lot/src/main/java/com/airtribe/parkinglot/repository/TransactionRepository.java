package com.airtribe.parkinglot.repository;

import com.airtribe.parkinglot.model.ParkingTransaction;
import java.util.List;
import java.util.Optional;

/**
 * Storage for parking transactions. The two conditional writes mirror what a SQL backend gives
 * for free: a unique index on the plate among ACTIVE rows, and an UPDATE guarded by status.
 */
public interface TransactionRepository {

    /** Stores a new ACTIVE transaction unless the same plate already has one. Returns false then. */
    boolean openIfPlateNotParked(ParkingTransaction transaction);

    /** Replaces an ACTIVE transaction with its CLOSED version. Returns false if it was not active. */
    boolean close(ParkingTransaction closed);

    Optional<ParkingTransaction> findById(String id);

    List<ParkingTransaction> findActive();

    List<ParkingTransaction> findAll();
}
