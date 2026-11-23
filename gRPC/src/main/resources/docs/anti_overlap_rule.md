# Anti‑Overlap Rule for Room Reservations

**Purpose**: Prevent two reservations for the same room from overlapping in time.

**Rule definition**:
- A reservation *A* (`startA`, `endA`) overlaps with reservation *B* (`startB`, `endB`) if the intervals intersect, i.e.:
  ```
  !(endA <= startB || startA >= endB)
  ```
- The rule must be enforced at both the database level and the application level.

**Database enforcement**:
- Unique constraint on `(room_id, start_time, end_time)` in the `reservation` table prevents duplicate intervals.
- A check constraint `CHECK (start_time < end_time)` guarantees a valid interval.
- Additional custom trigger (optional) can be added to reject overlapping intervals that are not identical.

**Application enforcement**:
- `RoomService.isOverlap(roomId, start, end)` checks existing reservations for a given room.
- Before creating a new reservation, call this method; if it returns `true`, reject the request with a clear error message.

**Error handling**:
- Return gRPC `Status.ABORTED` with description "Reservation time overlaps with an existing reservation".
- For REST, respond with HTTP `409 Conflict`.

**Testing**:
- Include unit tests that attempt to insert overlapping reservations and expect failure.
- Verify that non‑overlapping reservations succeed.
