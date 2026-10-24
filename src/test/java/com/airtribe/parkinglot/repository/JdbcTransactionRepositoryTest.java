package com.airtribe.parkinglot.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.airtribe.parkinglot.model.ParkingSpot;
import com.airtribe.parkinglot.model.ParkingTransaction;
import com.airtribe.parkinglot.model.SpotSize;
import com.airtribe.parkinglot.model.Vehicle;
import com.airtribe.parkinglot.model.VehicleType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class JdbcTransactionRepositoryTest {
    private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

    private DriverManagerDataSource db;
    private JdbcTransactionRepository repo;

    @BeforeEach
    void freshDatabase() {
        db = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(db);
        repo = new JdbcTransactionRepository(new JdbcTemplate(db));
    }

    private static ParkingTransaction ticket(String id, String plate, int spotNumber) {
        return ParkingTransaction.open(id, new Vehicle(plate, VehicleType.CAR),
                new ParkingSpot(0, spotNumber, SpotSize.MEDIUM), T0);
    }

    @Test
    void ticketsSurviveANewRepositoryOnTheSameDatabase() {
        assertTrue(repo.openIfPlateNotParked(ticket("T1", "KA01", 1)));

        JdbcTransactionRepository afterRestart = new JdbcTransactionRepository(new JdbcTemplate(db));

        assertEquals(List.of(ticket("T1", "KA01", 1)), afterRestart.findActive());
        assertEquals("T1", afterRestart.findById("T1").orElseThrow().id());
    }

    @Test
    void secondActiveTicketForSamePlateOrSpotIsRejected() {
        assertTrue(repo.openIfPlateNotParked(ticket("T1", "KA01", 1)));
        assertFalse(repo.openIfPlateNotParked(ticket("T2", "KA01", 2)));
        assertFalse(repo.openIfPlateNotParked(ticket("T3", "KA02", 1)));
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void closeWorksOnceAndFreesThePlate() {
        ParkingTransaction open = ticket("T1", "KA01", 1);
        repo.openIfPlateNotParked(open);
        ParkingTransaction closed = open.close(T0.plusSeconds(3600), new BigDecimal("20.00"));

        assertTrue(repo.close(closed));
        assertFalse(repo.close(closed));
        assertEquals(closed, repo.findById("T1").orElseThrow());
        assertTrue(repo.findActive().isEmpty());
        assertTrue(repo.openIfPlateNotParked(ticket("T2", "KA01", 1)));
    }

    @Test
    void manyThreadsParkingTheSamePlateYieldOneTicket() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Callable<Boolean>> attempts = IntStream.range(0, 16)
                .<Callable<Boolean>>mapToObj(i -> () -> repo.openIfPlateNotParked(ticket("T" + i, "KA01", i + 1)))
                .toList();
        long wins = 0;
        for (Future<Boolean> f : pool.invokeAll(attempts)) {
            wins += f.get() ? 1 : 0;
        }
        pool.shutdown();
        assertEquals(1, wins);
        assertEquals(1, repo.findActive().size());
    }
}
