# Class Model

```mermaid
classDiagram
    Rider "1" --> "0..*" Ride : requests
    Driver "0..1" --> "0..*" Ride : assigned to
    Ride "1" *-- "0..1" FareReceipt : produces
    RideService --> RideMatchingStrategy : selects
    RideService --> FareStrategy : calculates
    RideService --> FareCalculator : delegates to
    RideService --> RiderService
    RideService --> DriverService
```

`Rider`, `Driver`, `Ride`, `FareReceipt`, `Location`, `RideStatus`, and `VehicleType` are domain types. Services own use-case coordination. Strategy interfaces isolate matching and fare policy. Repositories are service-facing interfaces backed by in-memory implementations in the MVP.
