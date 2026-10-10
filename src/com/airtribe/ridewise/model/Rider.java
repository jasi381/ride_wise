package com.airtribe.ridewise.model;

public final class Rider {
    private final String id;
    private final String name;
    private final Location location;

    public Rider(String id, String name, Location location) {
        this.id = requireText(id, "Rider ID");
        this.name = requireText(name, "Rider name");
        this.location = java.util.Objects.requireNonNull(location, "Location is required.");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Location getLocation() { return location; }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(label + " is required.");
        return value.trim();
    }

    @Override
    public String toString() { return id + " - " + name + " at " + location; }
}
