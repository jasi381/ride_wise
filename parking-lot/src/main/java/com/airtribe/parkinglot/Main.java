package com.airtribe.parkinglot;

import com.airtribe.parkinglot.exception.ParkingException;
import com.airtribe.parkinglot.model.ParkingTransaction;
import com.airtribe.parkinglot.model.Receipt;
import com.airtribe.parkinglot.model.SpotSize;
import com.airtribe.parkinglot.model.Vehicle;
import com.airtribe.parkinglot.model.VehicleType;
import com.airtribe.parkinglot.repository.InMemoryTransactionRepository;
import com.airtribe.parkinglot.service.ParkingLotLayout;
import com.airtribe.parkinglot.service.ParkingService;
import com.airtribe.parkinglot.strategy.BestFitSpotAllocation;
import com.airtribe.parkinglot.strategy.FeeStrategy;
import com.airtribe.parkinglot.strategy.HourlyFeeStrategy;
import com.airtribe.parkinglot.strategy.PeakHourFeeStrategy;
import com.airtribe.parkinglot.strategy.RateCard;
import com.airtribe.parkinglot.util.IdGenerator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/** Console front end. Run with an optional fee strategy name: {@code hourly} (default) or {@code peak}. */
public final class Main {
    private static final Duration GRACE_PERIOD = Duration.ofMinutes(15);

    private final Scanner scanner = new Scanner(System.in);
    private final ParkingService parking;

    private Main(FeeStrategy fees) {
        Clock clock = Clock.systemDefaultZone();
        parking = new ParkingService(
                new BestFitSpotAllocation(new ParkingLotLayout()
                        .floor(0).spots(SpotSize.SMALL, 4).spots(SpotSize.MEDIUM, 4)
                        .floor(1).spots(SpotSize.MEDIUM, 6).spots(SpotSize.LARGE, 2)
                        .build()),
                fees,
                new InMemoryTransactionRepository(),
                clock,
                new IdGenerator("T"));
    }

    public static void main(String[] args) {
        RateCard rates = RateCard.standard();
        Map<String, FeeStrategy> strategies = new LinkedHashMap<>();
        strategies.put("hourly", new HourlyFeeStrategy(rates, GRACE_PERIOD));
        strategies.put("peak", new PeakHourFeeStrategy(rates, GRACE_PERIOD, LocalTime.of(8, 0), LocalTime.of(11, 0),
                BigDecimal.valueOf(1.5), Clock.systemDefaultZone().getZone()));

        String name = args.length > 0 ? args[0] : "hourly";
        FeeStrategy chosen = strategies.get(name);
        if (chosen == null) {
            System.out.println("Unknown fee strategy '" + name + "'. Available: " + strategies.keySet());
            return;
        }
        new Main(chosen).run();
    }

    private void run() {
        boolean running = true;
        while (running) {
            showMenu();
            String choice = readText("Choose an option: ");
            try {
                switch (choice) {
                    case "1": checkIn(); break;
                    case "2": checkOut(); break;
                    case "3": showAvailability(); break;
                    case "4": showActive(); break;
                    case "5": showHistory(); break;
                    case "0": running = false; break;
                    default: System.out.println("Choose one of the listed options.");
                }
            } catch (ParkingException | IllegalArgumentException exception) {
                System.out.println("Error: " + exception.getMessage());
            }
        }
    }

    private void showMenu() {
        System.out.println();
        System.out.println("=== Smart Parking Lot ===");
        System.out.println("1. Check in");
        System.out.println("2. Check out");
        System.out.println("3. Show availability");
        System.out.println("4. List parked vehicles");
        System.out.println("5. Show history");
        System.out.println("0. Exit");
    }

    private void checkIn() {
        String plate = readText("Plate number: ");
        VehicleType type = readVehicleType();
        ParkingTransaction ticket = parking.checkIn(new Vehicle(plate, type));
        System.out.printf("Ticket %s: %s %s -> spot %s%n", ticket.id(), type,
                ticket.vehicle().plate(), ticket.spot().id());
    }

    private void checkOut() {
        Receipt receipt = parking.checkOut(readText("Ticket id: "));
        System.out.println("--- Receipt ---");
        System.out.printf("Ticket   : %s%n", receipt.ticketId());
        System.out.printf("Vehicle  : %s (%s)%n", receipt.plate(), receipt.vehicleType());
        System.out.printf("Spot     : %s%n", receipt.spotId());
        System.out.printf("Entry    : %s%n", receipt.entryTime());
        System.out.printf("Exit     : %s%n", receipt.exitTime());
        System.out.printf("Duration : %d min%n", receipt.duration().toMinutes());
        System.out.printf("Fee      : %s%n", receipt.fee());
    }

    private void showAvailability() {
        parking.availability().forEach((size, free) -> System.out.printf("%-7s free: %d%n", size, free));
    }

    private void showActive() {
        if (parking.activeTickets().isEmpty()) {
            System.out.println("No vehicles are parked.");
        }
        for (ParkingTransaction ticket : parking.activeTickets()) {
            System.out.printf("%s  %s (%s)  spot %s  since %s%n", ticket.id(), ticket.vehicle().plate(),
                    ticket.vehicle().type(), ticket.spot().id(), ticket.entryTime());
        }
    }

    private void showHistory() {
        if (parking.history().isEmpty()) {
            System.out.println("Nothing recorded yet.");
        }
        for (ParkingTransaction t : parking.history()) {
            System.out.printf("%s  %s  %s  %s  fee %s%n", t.id(), t.vehicle().plate(), t.spot().id(),
                    t.status(), t.fee() == null ? "-" : t.fee());
        }
    }

    private VehicleType readVehicleType() {
        String options = String.join("/", Arrays.stream(VehicleType.values()).map(Enum::name).toList());
        String text = readText("Vehicle type (" + options + "): ");
        try {
            return VehicleType.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Vehicle type must be one of " + options);
        }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "0";
    }
}
