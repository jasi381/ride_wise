-- One row per stay. active_plate / active_spot hold the plate / spot only while the ticket is
-- ACTIVE (NULL once closed), so their UNIQUE constraints mean "one active ticket per plate" and
-- "one active ticket per spot" on any SQL database, without partial indexes.
CREATE TABLE IF NOT EXISTS parking_transaction (
    id           VARCHAR(32)   PRIMARY KEY,
    plate        VARCHAR(16)   NOT NULL,
    vehicle_type VARCHAR(12)   NOT NULL,
    floor        INT           NOT NULL,
    spot_number  INT           NOT NULL,
    spot_size    VARCHAR(8)    NOT NULL,
    entry_time   TIMESTAMP WITH TIME ZONE NOT NULL,
    exit_time    TIMESTAMP WITH TIME ZONE,
    fee          DECIMAL(10,2),
    status       VARCHAR(8)    NOT NULL CHECK (status IN ('ACTIVE', 'CLOSED')),
    active_plate VARCHAR(16)   UNIQUE,
    active_spot  VARCHAR(16)   UNIQUE
);
