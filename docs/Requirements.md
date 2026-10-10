# Requirements

## Functional

- Register riders and drivers and retrieve riders by ID.
- List drivers whose availability flag is enabled.
- Request a ride, match it using a selected strategy, and calculate its fare using a selected pricing strategy.
- Track rides through `REQUESTED`, `ASSIGNED`, `COMPLETED`, or `CANCELLED`.
- View ride history and complete an assigned ride.

## Non-functional

- Replace matching and pricing behavior through small interfaces.
- Keep service responsibilities focused and dependencies injected.
- Keep the MVP readable, in-memory, and free of external dependencies.

## Scope decisions

- Locations are latitude/longitude coordinates so nearest-driver matching has a clear distance calculation.
- Ride distance is supplied in kilometres when the rider requests a ride.
- Peak-hour pricing is selected by the user when making a ride request.
- Data lives in memory and resets when the process exits.
