package com.airtribe.parkinglot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.airtribe.parkinglot.TestClock;
import com.airtribe.parkinglot.exception.ParkingFullException;
import com.airtribe.parkinglot.exception.TicketAlreadyClosedException;
import com.airtribe.parkinglot.exception.TicketNotFoundException;
import com.airtribe.parkinglot.exception.VehicleAlreadyParkedException;
import com.airtribe.parkinglot.model.ParkingTransaction;
import com.airtribe.parkinglot.model.Receipt;
import com.airtribe.parkinglot.model.SpotSize;
import com.airtribe.parkinglot.model.TransactionStatus;
import com.airtribe.parkinglot.model.Vehicle;
import com.airtribe.parkinglot.model.VehicleType;
import com.airtribe.parkinglot.repository.InMemoryTransactionRepository;
import com.airtribe.parkinglot.repository.TransactionRepository;
import com.airtribe.parkinglot.strategy.BestFitSpotAllocation;
import com.airtribe.parkinglot.strategy.HourlyFeeStrategy;
import com.airtribe.parkinglot.strategy.RateCard;
import com.airtribe.parkinglot.util.IdGenerator;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkingServiceTest {
    private TestClock clock;
    private TransactionRepository repository;
    private ParkingService service;

    @BeforeEach
    void setUp() {
        clock = new TestClock(Instant.parse("2026-01-05T12:00:00Z"));
        repository = new InMemoryTransactionRepository();
        service = new ParkingService(
                new BestFitSpotAllocation(new ParkingLotLayout()
                        .floor(0).spots(SpotSize.SMALL, 1).spots(SpotSize.MEDIUM, 2).spots(SpotSize.LARGE, 1)
                        .build()),
                new HourlyFeeStrategy(RateCard.standard(), Duration.ofMinutes(15)),
                repository, clock, new IdGenerator("T"));
    }

    private static Vehicle car(String plate) {
        return new Vehicle(plate, VehicleType.CAR);
    }

    @Test
    void checkInAssignsASpotAndRecordsEntryTime() {
        ParkingTransaction ticket = service.checkIn(car("KA01"));
        assertEquals("T1", ticket.id());
        assertEquals(SpotSize.MEDIUM, ticket.spot().size());
        assertEquals(clock.instant(), ticket.entryTime());
        assertEquals(TransactionStatus.ACTIVE, ticket.status());
    }

    @Test
    void checkOutChargesByDurationAndVehicleType() {
        ParkingTransaction ticket = service.checkIn(car("KA01"));
        clock.advance(Duration.ofMinutes(130));
        Receipt receipt = service.checkOut(ticket.id());
        assertEquals(Duration.ofMinutes(130), receipt.duration());
        assertEquals(0, new BigDecimal("60").compareTo(receipt.fee()));
        assertEquals("KA01", receipt.plate());
        assertEquals(clock.instant(), receipt.exitTime());
    }

    @Test
    void sameStayCostsMoreForABusThanForAMotorcycle() {
        ParkingTransaction bus = service.checkIn(new Vehicle("BUS1", VehicleType.BUS));
        ParkingTransaction bike = service.checkIn(new Vehicle("BIKE1", VehicleType.MOTORCYCLE));
        clock.advance(Duration.ofHours(1));
        assertEquals(0, new BigDecimal("50").compareTo(service.checkOut(bus.id()).fee()));
        assertEquals(0, new BigDecimal("10").compareTo(service.checkOut(bike.id()).fee()));
    }

    @Test
    void availabilityUpdatesOnEntryAndExit() {
        assertEquals(2, service.availability().get(SpotSize.MEDIUM));
        ParkingTransaction ticket = service.checkIn(car("KA01"));
        assertEquals(1, service.availability().get(SpotSize.MEDIUM));
        assertEquals(1, service.activeTickets().size());
        service.checkOut(ticket.id());
        assertEquals(2, service.availability().get(SpotSize.MEDIUM));
        assertTrue(service.activeTickets().isEmpty());
        assertEquals(1, service.history().size());
    }

    @Test
    void fullLotRejectsTheVehicleAndLeavesNoTicketBehind() {
        service.checkIn(new Vehicle("BUS1", VehicleType.BUS));
        assertThrows(ParkingFullException.class, () -> service.checkIn(new Vehicle("BUS2", VehicleType.BUS)));
        assertEquals(1, service.history().size());
    }

    @Test
    void sameVehicleCannotBeParkedTwiceAndItsSpotIsNotLeaked() {
        service.checkIn(car("KA01"));
        assertThrows(VehicleAlreadyParkedException.class, () -> service.checkIn(car("ka01 ")));
        assertEquals(1, service.availability().get(SpotSize.MEDIUM));
        assertEquals(1, service.history().size());
    }

    @Test
    void vehicleCanComeBackAfterCheckingOut() {
        service.checkOut(service.checkIn(car("KA01")).id());
        ParkingTransaction again = service.checkIn(car("KA01"));
        assertEquals("T2", again.id());
    }

    @Test
    void checkingOutTwiceIsRejectedAndFreesTheSpotOnlyOnce() {
        ParkingTransaction ticket = service.checkIn(car("KA01"));
        service.checkOut(ticket.id());
        assertThrows(TicketAlreadyClosedException.class, () -> service.checkOut(ticket.id()));
        assertEquals(2, service.availability().get(SpotSize.MEDIUM));
    }

    @Test
    void unknownTicketIsRejected() {
        assertThrows(TicketNotFoundException.class, () -> service.checkOut("T99"));
    }

    @Test
    void spotIsReturnedWhenTheRepositoryRefusesTheTicket() {
        TransactionRepository refusing = new TransactionRepository() {
            @Override public boolean openIfPlateNotParked(ParkingTransaction transaction) { return false; }
            @Override public boolean close(ParkingTransaction closed) { return false; }
            @Override public Optional<ParkingTransaction> findById(String id) { return Optional.empty(); }
            @Override public List<ParkingTransaction> findActive() { return List.of(); }
            @Override public List<ParkingTransaction> findAll() { return List.of(); }
        };
        ParkingService svc = new ParkingService(
                new BestFitSpotAllocation(new ParkingLotLayout().floor(0).spots(SpotSize.MEDIUM, 1).build()),
                new HourlyFeeStrategy(RateCard.standard(), Duration.ZERO), refusing, clock, new IdGenerator("T"));
        assertThrows(VehicleAlreadyParkedException.class, () -> svc.checkIn(car("A")));
        assertEquals(1, svc.availability().get(SpotSize.MEDIUM));
    }
}
