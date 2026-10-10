# Object Relationships

- A rider can request many rides; each ride retains its rider association.
- A driver can be assigned to many rides over time; a ride may have no driver before assignment.
- A ride owns its optional fare receipt. The receipt is created when the ride completes.
- `RideService` composes rider/driver services, repositories, and strategy interfaces for the request and completion workflows.
- Entities do not reach through nested object chains for unrelated service behavior. Coordinates and identity values are read directly from their collaborators.
