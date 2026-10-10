# RideWise

Small console app for booking rides, written in plain Java. No libraries, no build tool. Everything lives in memory, so data is gone when you quit.

## Run

```sh
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out com.airtribe.ridewise.Main
```

Needs Java 11 or newer.

## How it works

You get a numbered menu:

1. Add rider
2. Add driver
3. List available drivers
4. Request ride
5. Complete ride
6. List rides
7. Cancel ride
8. Pick matching strategy
9. Pick fare strategy

IDs are handed out as `R1` for riders, `D1` for drivers and `T1` for rides. To request a ride you give a rider ID and the trip distance in km. If no driver is free, the request fails and no ride is created.

A ride goes `REQUESTED` -> `ASSIGNED` -> `COMPLETED`, or `CANCELLED`. A driver is busy while holding an assigned ride and free again once it is completed or cancelled.

## Matching and fares

Both are swappable at runtime from the menu.

| Matching | Picks |
|---|---|
| Nearest | closest free driver to the rider |
| Least active | free driver with the fewest rides so far |

Ties go to the lower driver ID.

| Fare | Price |
|---|---|
| Default | 40 + 12 per km |
| Peak hour | default fare x 1.5 |

Locations are latitude/longitude pairs.

## Code layout

```
src/com/airtribe/ridewise/
  Main.java        menu and input handling
  model/           Rider, Driver, Ride, FareReceipt, Location, enums
  service/         RiderService, DriverService, RideService, FareCalculator,
                   repository interfaces + in-memory versions
  strategy/        matching and fare strategies
  exception/       NoDriverAvailableException
  util/            IdGenerator
```

To add a new matching or fare rule, implement `RideMatchingStrategy` or `FareStrategy` and add a line to the menu in `Main`.
