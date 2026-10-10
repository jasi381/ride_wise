package com.airtribe.ridewise.model;

public final class Ride {
    private final String id;
    private final Rider rider;
    private Driver driver;
    private final double distance;
    private RideStatus status;
    private FareReceipt fareReceipt;

    public Ride(String id, Rider rider, double distance) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Ride ID is required.");
        if (!Double.isFinite(distance) || distance <= 0) throw new IllegalArgumentException("Distance must be greater than zero.");
        this.id = id;
        this.rider = java.util.Objects.requireNonNull(rider, "Rider is required.");
        this.distance = distance;
        this.status = RideStatus.REQUESTED;
    }

    public String getId() { return id; }
    public Rider getRider() { return rider; }
    public Driver getDriver() { return driver; }
    public double getDistance() { return distance; }
    public RideStatus getStatus() { return status; }
    public FareReceipt getFareReceipt() { return fareReceipt; }

    public void assign(Driver driver) {
        requireStatus(RideStatus.REQUESTED);
        this.driver = java.util.Objects.requireNonNull(driver, "Driver is required.");
        this.status = RideStatus.ASSIGNED;
    }

    public void complete(FareReceipt receipt) {
        requireStatus(RideStatus.ASSIGNED);
        this.fareReceipt = java.util.Objects.requireNonNull(receipt, "Fare receipt is required.");
        this.status = RideStatus.COMPLETED;
    }

    public void cancel() {
        if (status != RideStatus.REQUESTED && status != RideStatus.ASSIGNED) {
            throw new IllegalStateException("Only requested or assigned rides can be cancelled.");
        }
        status = RideStatus.CANCELLED;
    }

    private void requireStatus(RideStatus expected) {
        if (status != expected) throw new IllegalStateException("Ride " + id + " must be " + expected + " to perform this action.");
    }

    @Override
    public String toString() {
        String driverName = driver == null ? "unassigned" : driver.getName() + " (" + driver.getId() + ")";
        String fare = fareReceipt == null ? "pending" : String.format("%.2f", fareReceipt.getAmount());
        return id + " | rider: " + rider.getName() + " | driver: " + driverName
                + " | " + distance + " km | " + status + " | fare: " + fare;
    }
}
