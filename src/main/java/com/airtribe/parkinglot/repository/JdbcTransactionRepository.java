package com.airtribe.parkinglot.repository;

import com.airtribe.parkinglot.model.ParkingSpot;
import com.airtribe.parkinglot.model.ParkingTransaction;
import com.airtribe.parkinglot.model.SpotSize;
import com.airtribe.parkinglot.model.TransactionStatus;
import com.airtribe.parkinglot.model.Vehicle;
import com.airtribe.parkinglot.model.VehicleType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * SQL storage. The two conditional writes are done by the database: the UNIQUE columns
 * {@code active_plate} / {@code active_spot} reject a second active ticket, and the check-out
 * UPDATE only matches a row that is still ACTIVE. See {@code schema.sql}.
 */
public final class JdbcTransactionRepository implements TransactionRepository {
    private static final String SELECT = "SELECT * FROM parking_transaction";
    private static final String ORDER = " ORDER BY entry_time, id";

    private final JdbcTemplate jdbc;

    public JdbcTransactionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean openIfPlateNotParked(ParkingTransaction t) {
        try {
            jdbc.update("""
                    INSERT INTO parking_transaction (id, plate, vehicle_type, floor, spot_number, spot_size,
                        entry_time, status, active_plate, active_spot)
                    VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?)""",
                    t.id(), t.vehicle().plate(), t.vehicle().type().name(), t.spot().floor(), t.spot().number(),
                    t.spot().size().name(), utc(t.entryTime()), t.vehicle().plate(), t.spot().id());
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    @Override
    public boolean close(ParkingTransaction c) {
        return jdbc.update("""
                UPDATE parking_transaction
                SET status = 'CLOSED', exit_time = ?, fee = ?, active_plate = NULL, active_spot = NULL
                WHERE id = ? AND status = 'ACTIVE'""", utc(c.exitTime()), c.fee(), c.id()) == 1;
    }

    @Override
    public Optional<ParkingTransaction> findById(String id) {
        return jdbc.query(SELECT + " WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public List<ParkingTransaction> findActive() {
        return jdbc.query(SELECT + " WHERE status = 'ACTIVE'" + ORDER, MAPPER);
    }

    @Override
    public List<ParkingTransaction> findAll() {
        return jdbc.query(SELECT + ORDER, MAPPER);
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    private static final RowMapper<ParkingTransaction> MAPPER = (rs, row) -> new ParkingTransaction(
            rs.getString("id"),
            new Vehicle(rs.getString("plate"), VehicleType.valueOf(rs.getString("vehicle_type"))),
            new ParkingSpot(rs.getInt("floor"), rs.getInt("spot_number"), SpotSize.valueOf(rs.getString("spot_size"))),
            instant(rs, "entry_time"),
            instant(rs, "exit_time"),
            rs.getBigDecimal("fee"),
            TransactionStatus.valueOf(rs.getString("status")));

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}
