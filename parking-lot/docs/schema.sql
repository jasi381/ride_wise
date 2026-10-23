-- Relational model behind the in-memory implementation. Written for PostgreSQL.

CREATE TABLE parking_spot (
    id          VARCHAR(16) PRIMARY KEY,                       -- e.g. F1-S7
    floor       INT         NOT NULL CHECK (floor >= 0),
    number      INT         NOT NULL CHECK (number >= 1),
    size        VARCHAR(8)  NOT NULL CHECK (size IN ('SMALL', 'MEDIUM', 'LARGE')),
    UNIQUE (floor, number)
);

CREATE TABLE vehicle (
    plate       VARCHAR(16) PRIMARY KEY,                       -- normalised: trimmed, upper case
    type        VARCHAR(12) NOT NULL CHECK (type IN ('MOTORCYCLE', 'CAR', 'BUS'))
);

CREATE TABLE rate (
    vehicle_type VARCHAR(12)   PRIMARY KEY,
    hourly_rate  NUMERIC(10,2) NOT NULL CHECK (hourly_rate >= 0)
);

CREATE TABLE parking_transaction (
    id          VARCHAR(16)   PRIMARY KEY,                     -- ticket id
    plate       VARCHAR(16)   NOT NULL REFERENCES vehicle (plate),
    spot_id     VARCHAR(16)   NOT NULL REFERENCES parking_spot (id),
    entry_time  TIMESTAMPTZ   NOT NULL,
    exit_time   TIMESTAMPTZ,
    fee         NUMERIC(10,2),
    status      VARCHAR(8)    NOT NULL CHECK (status IN ('ACTIVE', 'CLOSED')),
    CHECK ((status = 'ACTIVE' AND exit_time IS NULL AND fee IS NULL)
        OR (status = 'CLOSED' AND exit_time IS NOT NULL AND fee IS NOT NULL AND exit_time >= entry_time))
);

-- A vehicle can be parked once, and a spot can hold one vehicle, at a time. These two partial
-- unique indexes are the database version of the in-memory conditional writes.
CREATE UNIQUE INDEX one_active_per_plate ON parking_transaction (plate)   WHERE status = 'ACTIVE';
CREATE UNIQUE INDEX one_active_per_spot  ON parking_transaction (spot_id) WHERE status = 'ACTIVE';

-- Allocation (best fit, lowest floor first). SKIP LOCKED lets concurrent gates take different spots
-- without waiting on each other:
--   SELECT s.id FROM parking_spot s
--   WHERE s.size >= :minimum_size                      -- compare via an ordinal column or CASE
--     AND NOT EXISTS (SELECT 1 FROM parking_transaction t WHERE t.spot_id = s.id AND t.status = 'ACTIVE')
--   ORDER BY s.size, s.floor, s.number
--   LIMIT 1 FOR UPDATE SKIP LOCKED;

-- Check-out is a guarded update; zero rows updated means someone else already checked the ticket out:
--   UPDATE parking_transaction SET status = 'CLOSED', exit_time = :now, fee = :fee
--   WHERE id = :ticket AND status = 'ACTIVE';

-- Live availability:
--   SELECT s.size, COUNT(*) FROM parking_spot s
--   LEFT JOIN parking_transaction t ON t.spot_id = s.id AND t.status = 'ACTIVE'
--   WHERE t.id IS NULL GROUP BY s.size;
