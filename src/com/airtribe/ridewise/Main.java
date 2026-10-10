package com.airtribe.ridewise;

import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.FareReceipt;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.service.DriverService;
import com.airtribe.ridewise.service.DriverRepository;
import com.airtribe.ridewise.service.RideRepository;
import com.airtribe.ridewise.service.RiderRepository;
import com.airtribe.ridewise.service.RideService;
import com.airtribe.ridewise.service.RiderService;
import com.airtribe.ridewise.strategy.DefaultFareStrategy;
import com.airtribe.ridewise.strategy.LeastActiveDriverStrategy;
import com.airtribe.ridewise.strategy.NearestDriverStrategy;
import com.airtribe.ridewise.strategy.PeakHourFareStrategy;
import com.airtribe.ridewise.util.IdGenerator;
import java.util.List;
import java.util.Scanner;

public final class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final RiderService riderService;
    private final DriverService driverService;
    private final RideService rideService;

    private Main() {
        riderService = new RiderService(new RiderRepository(), new IdGenerator("R"));
        driverService = new DriverService(new DriverRepository(), new IdGenerator("D"));
        rideService = new RideService(riderService, driverService, new RideRepository(),
                new NearestDriverStrategy(), new DefaultFareStrategy(), new IdGenerator("T"));
    }

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        boolean running = true;
        while (running) {
            showMenu();
            String choice = readText("Choose an option: ");
            try {
                switch (choice) {
                    case "1": addRider(); break;
                    case "2": addDriver(); break;
                    case "3": showAvailableDrivers(); break;
                    case "4": requestRide(); break;
                    case "5": completeRide(); break;
                    case "6": showRides(); break;
                    case "7": cancelRide(); break;
                    case "8": selectMatchingStrategy(); break;
                    case "9": selectFareStrategy(); break;
                    case "0": running = false; break;
                    default: System.out.println("Choose one of the listed options.");
                }
            } catch (IllegalArgumentException | IllegalStateException | NoDriverAvailableException exception) {
                System.out.println("Could not complete that action: " + exception.getMessage());
            }
            System.out.println();
        }
        System.out.println("RideWise closed.");
    }

    private void showMenu() {
        System.out.println("RideWise");
        System.out.println("1. Add Rider");
        System.out.println("2. Add Driver");
        System.out.println("3. View Available Drivers");
        System.out.println("4. Request Ride");
        System.out.println("5. Complete Ride");
        System.out.println("6. View Rides");
        System.out.println("7. Cancel Ride");
        System.out.println("8. Choose Matching Strategy");
        System.out.println("9. Choose Fare Strategy");
        System.out.println("0. Exit");
    }

    private void addRider() {
        Rider rider = riderService.registerRider(readText("Name: "), readLocation());
        System.out.println("Registered " + rider);
    }

    private void addDriver() {
        String name = readText("Name: ");
        Location location = readLocation();
        VehicleType type = readEnum("Vehicle type (BIKE, AUTO, CAR): ", VehicleType.class);
        Driver driver = driverService.registerDriver(name, location, type);
        System.out.println("Registered " + driver);
    }

    private void showAvailableDrivers() {
        List<Driver> drivers = driverService.listAvailableDrivers();
        if (drivers.isEmpty()) {
            System.out.println("No available drivers.");
            return;
        }
        drivers.forEach(System.out::println);
    }

    private void requestRide() {
        String riderId = readText("Rider ID: ");
        double distance = readPositiveDouble("Trip distance in km: ");
        Ride ride = rideService.requestRide(riderId, distance);
        System.out.println("Ride assigned: " + ride);
    }

    private void completeRide() {
        FareReceipt receipt = rideService.completeRide(readText("Ride ID: "));
        System.out.println(receipt);
    }

    private void cancelRide() {
        rideService.cancelRide(readText("Ride ID: "));
        System.out.println("Ride cancelled.");
    }

    private void showRides() {
        List<Ride> rides = rideService.listRides();
        if (rides.isEmpty()) {
            System.out.println("No rides yet.");
            return;
        }
        rides.forEach(System.out::println);
    }

    private void selectMatchingStrategy() {
        System.out.println("1. Nearest driver");
        System.out.println("2. Fewest rides assigned");
        switch (readText("Choose matching strategy: ")) {
            case "1": rideService.setMatchingStrategy(new NearestDriverStrategy()); System.out.println("Nearest driver selected."); break;
            case "2": rideService.setMatchingStrategy(new LeastActiveDriverStrategy()); System.out.println("Fewest-rides driver selected."); break;
            default: System.out.println("Strategy unchanged.");
        }
    }

    private void selectFareStrategy() {
        System.out.println("1. Default fare");
        System.out.println("2. Peak-hour fare");
        switch (readText("Choose fare strategy: ")) {
            case "1": rideService.setFareStrategy(new DefaultFareStrategy()); System.out.println("Default fare selected."); break;
            case "2": rideService.setFareStrategy(new PeakHourFareStrategy()); System.out.println("Peak-hour fare selected."); break;
            default: System.out.println("Strategy unchanged.");
        }
    }

    private Location readLocation() {
        double latitude = readDouble("Latitude (-90 to 90): ");
        double longitude = readDouble("Longitude (-180 to 180): ");
        return new Location(latitude, longitude);
    }

    private double readPositiveDouble(String prompt) {
        double value = readDouble(prompt);
        if (value <= 0) throw new IllegalArgumentException("Value must be greater than zero.");
        return value;
    }

    private double readDouble(String prompt) {
        String value = readText(prompt);
        try {
            double parsed = Double.parseDouble(value);
            if (!Double.isFinite(parsed)) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a valid number.");
        }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) return "0";
        return scanner.nextLine().trim();
    }

    private <T extends Enum<T>> T readEnum(String prompt, Class<T> type) {
        String value = readText(prompt);
        try {
            return Enum.valueOf(type, value.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Choose a valid " + type.getSimpleName() + ".");
        }
    }
}
