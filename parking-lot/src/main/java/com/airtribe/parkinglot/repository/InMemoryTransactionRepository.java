package com.airtribe.parkinglot.repository;

import com.airtribe.parkinglot.model.ParkingTransaction;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryTransactionRepository implements TransactionRepository {
    private final Map<String, ParkingTransaction> byId = new ConcurrentHashMap<>();
    private final Map<String, String> activeIdByPlate = new ConcurrentHashMap<>();

    @Override
    public boolean openIfPlateNotParked(ParkingTransaction transaction) {
        if (activeIdByPlate.putIfAbsent(transaction.vehicle().plate(), transaction.id()) != null) {
            return false;
        }
        byId.put(transaction.id(), transaction);
        return true;
    }

    @Override
    public boolean close(ParkingTransaction closed) {
        boolean replaced = byId.computeIfPresent(closed.id(), (id, current) -> current.isActive() ? closed : current) == closed;
        if (replaced) {
            activeIdByPlate.remove(closed.vehicle().plate());
        }
        return replaced;
    }

    @Override
    public Optional<ParkingTransaction> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<ParkingTransaction> findActive() {
        return findAll().stream().filter(ParkingTransaction::isActive).toList();
    }

    @Override
    public List<ParkingTransaction> findAll() {
        List<ParkingTransaction> all = new ArrayList<>(byId.values());
        all.sort(Comparator.comparing(ParkingTransaction::entryTime).thenComparing(ParkingTransaction::id));
        return all;
    }
}
