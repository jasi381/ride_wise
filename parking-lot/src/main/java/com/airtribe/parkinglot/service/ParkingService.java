package com.airtribe.parkinglot.service;

import com.airtribe.parkinglot.exception.ParkingFullException;
import com.airtribe.parkinglot.exception.TicketAlreadyClosedException;
import com.airtribe.parkinglot.exception.TicketNotFoundException;
import com.airtribe.parkinglot.exception.VehicleAlreadyParkedException;
import com.airtribe.parkinglot.model.ParkingSpot;
import com.airtribe.parkinglot.model.ParkingTransaction;
import com.airtribe.parkinglot.model.Receipt;
import com.airtribe.parkinglot.model.SpotSize;
import com.airtribe.parkinglot.model.Vehicle;
import com.airtribe.parkinglot.repository.TransactionRepository;
import com.airtribe.parkinglot.strategy.FeeStrategy;
import com.airtribe.parkinglot.strategy.SpotAllocationStrategy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Check-in, check-out and availability. Every collaborator is injected as an interface, so the
 * service can be tested with a fixed clock and swapped strategies.
 *
 * Thread safety comes from the collaborators' atomic operations rather than a global lock:
 * spots are taken with an atomic poll, a plate is claimed with a conditional insert and a
 * ticket is closed with a conditional update. Whoever loses a race gets a clear exception and
 * leaves no state behind.
 */
public final class ParkingService {
    private final SpotAllocationStrategy spots;
    private final FeeStrategy fees;
    private final TransactionRepository transactions;
    private final Clock clock;
    private final Supplier<String> ticketIds;

    public ParkingService(SpotAllocationStrategy spots, FeeStrategy fees, TransactionRepository transactions,
                          Clock clock, Supplier<String> ticketIds) {
        this.spots = spots;
        this.fees = fees;
        this.transactions = transactions;
        this.clock = clock;
        this.ticketIds = ticketIds;
    }

    public ParkingTransaction checkIn(Vehicle vehicle) {
        ParkingSpot spot = spots.allocate(vehicle.type())
                .orElseThrow(() -> new ParkingFullException(vehicle.type()));
        // The record is only stored after the spot is secured, and the spot goes back if storing
        // fails, so a failure here can neither strand a ticket nor leak a spot.
        boolean stored = false;
        try {
            ParkingTransaction ticket = ParkingTransaction.open(ticketIds.get(), vehicle, spot, clock.instant());
            if (!transactions.openIfPlateNotParked(ticket)) {
                throw new VehicleAlreadyParkedException(vehicle.plate());
            }
            stored = true;
            return ticket;
        } finally {
            if (!stored) {
                spots.release(spot);
            }
        }
    }

    public Receipt checkOut(String ticketId) {
        ParkingTransaction ticket = transactions.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        if (!ticket.isActive()) {
            throw new TicketAlreadyClosedException(ticketId);
        }
        Instant exit = clock.instant();
        BigDecimal fee = fees.calculate(ticket.vehicle().type(), ticket.entryTime(), exit)
                .setScale(2, RoundingMode.HALF_UP);
        ParkingTransaction closed = ticket.close(exit, fee);
        if (!transactions.close(closed)) {
            throw new TicketAlreadyClosedException(ticketId);
        }
        spots.release(ticket.spot());
        return Receipt.from(closed);
    }

    public Map<SpotSize, Integer> availability() {
        return spots.freeSpots();
    }

    public List<ParkingTransaction> activeTickets() {
        return transactions.findActive();
    }

    public List<ParkingTransaction> history() {
        return transactions.findAll();
    }
}
