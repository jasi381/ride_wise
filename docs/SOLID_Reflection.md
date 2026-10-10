# SOLID Reflection

- **SRP:** Rider registration, driver availability, ride lifecycle, fare calculation (`FareCalculator`), and console interaction each have distinct owners.
- **OCP:** New ride-matching and fare policies implement `RideMatchingStrategy` or `FareStrategy`; `RideService` consumes the interfaces.
- **LSP:** Any implementation must honor its interface contract: matching returns an eligible driver or `null`, and fare strategies return a finite non-negative amount.
- **ISP:** Matching and fare policies are separate, focused interfaces.
- **DIP:** `RideService` receives services, repositories, and strategies through constructor parameters or interface types.

The menu wires the default implementations and lets a user switch to the other included policies at runtime. A future policy can be added without adding policy-specific branches to the ride request flow.

`LeastActiveDriverStrategy` balances work by selecting the available driver with the fewest rides assigned so far. A driver with an assigned ride is temporarily unavailable; completing or cancelling that ride makes the driver available again if they remain on shift.
