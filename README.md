# RideWise

RideWise is a small Java console ride-sharing system built to demonstrate object-oriented design, SOLID principles, and strategy-based extension points.

## Requirements

- Java 11 or later
- No external libraries

## Run

From the project root:

```sh
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out com.airtribe.ridewise.Main
```

Use the menu to register riders and drivers, view available drivers, request and complete rides, and choose matching and fare strategies. Ride requests include a rider ID and trip distance in kilometres.

## Structure

```text
src/com/airtribe/ridewise/
├── Main.java
├── exception/
├── model/
├── service/
├── strategy/
└── util/
docs/
```

The console is an input/output adapter. It delegates application actions to services; the services compose repositories and strategy interfaces. The in-memory repositories keep the MVP self-contained and can be replaced without changing the domain model.

## Repository

GitHub repository: [jasi381/ride_wise](https://github.com/jasi381/ride_wise).
