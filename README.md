# Smart Parking Lot (v2)

Backend for a multi-floor parking lot: spot allocation, check-in/check-out, fee calculation and live availability. Spring Boot 3 (Java 17). Tickets are stored in SQL (embedded H2 file `./data/parking`, schema in `src/main/resources/schema.sql`), so they survive restarts. Spot occupancy is rebuilt from active tickets at startup.

## API

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/tickets` | Check in. Body `{"plate":"KA01AB1234","type":"CAR"}` (`MOTORCYCLE`, `CAR`, `BUS`). 201, or 409 if full or plate already parked. |
| POST | `/api/tickets/{id}/checkout` | Check out, returns the receipt. 404 unknown ticket, 409 already closed. |
| GET | `/api/availability` | Free spots per size. |
| GET | `/api/tickets` | Active tickets. |
| GET | `/api/tickets/history` | All tickets. |

## Run

```sh
./gradlew test                                          # 35 tests, including concurrency
./gradlew bootRun                                       # http://localhost:8080, hourly fees
./gradlew bootRun --args='--parking.fee-strategy=peak'  # peak-hour surcharge
```

Menu: 1 Check in, 2 Check out, 3 Availability, 4 Parked vehicles, 5 History, 0 Exit. Check-in prints a ticket id (`T1`); check-out takes it and prints a receipt.

## Design

| Concern | Where | How |
|---|---|---|
| Spot allocation | `BestFitSpotAllocation` | Free spots live in one sorted set per size. The smallest size that fits is tried first, lowest floor and number first. Taking a spot is an atomic `pollFirst()`, O(log n). |
| Vehicle to spot | `VehicleType` | Motorcycle needs SMALL or bigger, car MEDIUM or bigger, bus LARGE only. |
| Fees | `HourlyFeeStrategy`, `PeakHourFeeStrategy` | Rate comes from `RateCard` per vehicle type. 15 minute grace, then every started hour is billed. Peak adds x1.5 only to hours that start inside the peak window. |
| Concurrency | `ParkingService` + collaborators | No global lock. Spot: atomic poll. Plate: conditional insert (one active ticket per plate). Check-out: conditional close, so only one caller frees the spot and charges the fee. |
| Availability | `ParkingService.availability()` | Counted from the allocator's free sets, so it is never out of step with allocation. |
| Time | `java.time.Clock` | Injected, so tests move time by hand. |

Everything `ParkingService` uses is an interface (`SpotAllocationStrategy`, `FeeStrategy`, `TransactionRepository`, `Clock`, id `Supplier`), injected through the constructor. Adding a fee strategy is a new class plus one case in `ParkingConfig.feeStrategy`.

Failure handling: a ticket is stored only after a spot is secured, and the spot is returned if storing fails, so a failed check-in leaves neither a stranded ticket nor a leaked spot.

## Layout

```
src/main/java/com/airtribe/parkinglot/
  ParkingLotApplication.java
  api/                 ParkingController, ApiExceptionHandler, ParkingConfig
  model/        VehicleType, SpotSize, ParkingSpot, Vehicle, ParkingTransaction, Receipt
  strategy/     SpotAllocationStrategy, BestFitSpotAllocation, FeeStrategy, RateCard, Hourly/PeakHour strategies
  service/      ParkingService, ParkingLotLayout
  repository/   TransactionRepository, JdbcTransactionRepository, InMemoryTransactionRepository (test double)
  exception/    ParkingException and its subclasses
src/test/java/  unit and concurrency tests
```
