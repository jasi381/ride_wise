package com.airtribe.parkinglot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.airtribe.parkinglot.model.ParkingTransaction;
import com.airtribe.parkinglot.model.SpotSize;
import com.airtribe.parkinglot.model.Vehicle;
import com.airtribe.parkinglot.model.VehicleType;
import com.airtribe.parkinglot.repository.InMemoryTransactionRepository;
import com.airtribe.parkinglot.strategy.BestFitSpotAllocation;
import com.airtribe.parkinglot.strategy.HourlyFeeStrategy;
import com.airtribe.parkinglot.strategy.RateCard;
import com.airtribe.parkinglot.util.IdGenerator;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class ParkingServiceConcurrencyTest {
    private static final int SPOTS = 20;
    private static final int THREADS = 100;

    private ParkingService newService() {
        return new ParkingService(
                new BestFitSpotAllocation(new ParkingLotLayout().floor(0).spots(SpotSize.MEDIUM, SPOTS).build()),
                new HourlyFeeStrategy(RateCard.standard(), Duration.ofMinutes(15)),
                new InMemoryTransactionRepository(), Clock.systemUTC(), new IdGenerator("T"));
    }

    /** Runs all tasks at the same moment and returns their results (null where the task threw). */
    private <T> List<T> runTogether(List<Callable<T>> tasks) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch ready = new CountDownLatch(tasks.size());
        CountDownLatch go = new CountDownLatch(1);
        List<Future<T>> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                go.await();
                try {
                    return task.call();
                } catch (RuntimeException expectedRejection) {
                    return null;
                }
            }));
        }
        ready.await();
        go.countDown();
        List<T> results = new ArrayList<>();
        for (Future<T> future : futures) {
            results.add(future.get());
        }
        pool.shutdown();
        return results;
    }

    @Test
    void moreCarsThanSpotsFillsEverySpotExactlyOnce() throws Exception {
        ParkingService service = newService();
        List<Callable<ParkingTransaction>> arrivals = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Vehicle vehicle = new Vehicle("CAR" + i, VehicleType.CAR);
            arrivals.add(() -> service.checkIn(vehicle));
        }

        List<ParkingTransaction> tickets = runTogether(arrivals).stream().filter(t -> t != null).toList();

        assertEquals(SPOTS, tickets.size());
        Set<String> spotIds = new HashSet<>();
        tickets.forEach(t -> spotIds.add(t.spot().id()));
        assertEquals(SPOTS, spotIds.size(), "two vehicles were given the same spot");
        assertEquals(0, service.availability().get(SpotSize.MEDIUM));
    }

    @Test
    void sameVehicleArrivingOnManyGatesIsParkedOnce() throws Exception {
        ParkingService service = newService();
        Vehicle vehicle = new Vehicle("TWIN", VehicleType.CAR);
        List<Callable<ParkingTransaction>> arrivals = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            arrivals.add(() -> service.checkIn(vehicle));
        }

        long parked = runTogether(arrivals).stream().filter(t -> t != null).count();

        assertEquals(1, parked);
        assertEquals(SPOTS - 1, service.availability().get(SpotSize.MEDIUM));
    }

    @Test
    void sameTicketCheckedOutByManyThreadsIsChargedOnceAndFreedOnce() throws Exception {
        ParkingService service = newService();
        ParkingTransaction ticket = service.checkIn(new Vehicle("ONE", VehicleType.CAR));
        List<Callable<Object>> exits = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            exits.add(() -> service.checkOut(ticket.id()));
        }

        long succeeded = runTogether(exits).stream().filter(r -> r != null).count();

        assertEquals(1, succeeded);
        assertEquals(SPOTS, service.availability().get(SpotSize.MEDIUM));
    }

    @Test
    void mixedEntriesAndExitsKeepTheCountsConsistent() throws Exception {
        ParkingService service = newService();
        List<ParkingTransaction> parked = new ArrayList<>();
        for (int i = 0; i < SPOTS / 2; i++) {
            parked.add(service.checkIn(new Vehicle("OLD" + i, VehicleType.CAR)));
        }
        List<Callable<Object>> tasks = new ArrayList<>();
        parked.forEach(t -> tasks.add(() -> service.checkOut(t.id())));
        for (int i = 0; i < SPOTS / 2; i++) {
            Vehicle vehicle = new Vehicle("NEW" + i, VehicleType.CAR);
            tasks.add(() -> service.checkIn(vehicle));
        }

        runTogether(tasks);

        assertEquals(SPOTS / 2, service.activeTickets().size());
        assertEquals(SPOTS - service.activeTickets().size(), service.availability().get(SpotSize.MEDIUM));
    }
}
