package com.airtribe.ridewise.model;

public final class Driver {
    private final String id;
    private final String name;
    private Location currentLocation;
    private final VehicleType vehicleType;
    private boolean available;
    private int activeRideCount;
    private int totalRideCount;

    public Driver(String id, String name, Location currentLocation, VehicleType vehicleType) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Driver ID is required.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Driver name is required.");
        this.id = id.trim();
        this.name = name.trim();
        this.currentLocation = java.util.Objects.requireNonNull(currentLocation, "Location is required.");
        this.vehicleType = java.util.Objects.requireNonNull(vehicleType, "Vehicle type is required.");
        this.available = true;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Location getCurrentLocation() { return currentLocation; }
    public VehicleType getVehicleType() { return vehicleType; }
    public boolean isAvailable() { return available && activeRideCount == 0; }
    public int getActiveRideCount() { return activeRideCount; }
    public int getTotalRideCount() { return totalRideCount; }
    public void setAvailable(boolean available) { this.available = available; }
    public void updateLocation(Location location) { this.currentLocation = java.util.Objects.requireNonNull(location); }
    public void startRide() { activeRideCount++; totalRideCount++; }
    public void finishRide() {
        if (activeRideCount == 0) throw new IllegalStateException("Driver has no active rides.");
        activeRideCount--;
    }

    @Override
    public String toString() {
        return id + " - " + name + " (" + vehicleType + ") at " + currentLocation
                + ", active rides: " + activeRideCount;
    }
}
