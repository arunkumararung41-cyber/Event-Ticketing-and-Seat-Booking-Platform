# Seat-Locking Design

## The problem

Two users click "book" on the same seat within milliseconds of each other. Without protection, both requests can read the seat as AVAILABLE, both write BOOKED, and the venue has sold one seat twice.

## Why not just use a database transaction?

A naive approach — `SELECT ... FOR UPDATE` inside a transaction that spans the entire checkout flow — would work for the instant of the write, but the *seat selection UX* can last minutes (the user is browsing the map, deciding, entering payment details). Holding a database row lock for that whole window would:

- Block other requests to that row for minutes, not milliseconds
- Not survive the user simply closing the tab (no natural expiry)
- Not scale across multiple backend instances without a shared lock table

## The chosen approach: Redis SETNX + TTL

```
SETNX seat-lock:{seatId} = {bookingReference}   EX 300   (5 minutes)
```

- `SETNX` (via Spring Data Redis's `setIfAbsent`) is atomic at the Redis engine level — only one caller can ever successfully set a given key, even under massive concurrent load.
- The TTL means an abandoned checkout self-heals: no cleanup job has to run *in time* to prevent a stuck seat. If it never runs, the lock still disappears.
- Because the lock lives in Redis rather than in-process memory, it works correctly even if the backend is horizontally scaled across multiple pods/instances — a `ConcurrentHashMap`-based lock would not.

## Batch acquisition semantics

When a user selects multiple seats, `SeatLockService.tryLockSeats()`:

1. Attempts `SETNX` for each seat in the batch, one at a time.
2. If every seat locks successfully, the booking proceeds.
3. If **any** seat fails (already held/booked by someone else), every lock acquired earlier in that same batch attempt is rolled back immediately. This avoids leaving a partial hold (e.g., 2 of 3 seats locked) that would confuse the user and waste inventory.

## Confirm-time re-verification

Between "hold" and "confirm" there's a window where the TTL could expire (user was slow, or timing was borderline). `BookingService.confirmBooking()` re-checks that every seat's Redis lock is still owned by *this* booking reference before finalizing payment. If not, the booking is expired server-side and the user is told to re-select seats — this prevents confirming a booking whose seats might have already been re-issued to someone else.

## Reconciling the database

Redis governs the *lock*, but `Seat.status` in MySQL is still the durable source of truth for anything beyond the 5-minute hold window (BOOKED is permanent). A scheduled job (`ExpiredHoldCleanupJob`) runs every 60 seconds, finds bookings still marked `HELD` past their `expiresAt`, and flips them to `EXPIRED` while releasing their seats back to `AVAILABLE` — this keeps seat maps and admin reports consistent even for holds that were abandoned rather than explicitly cancelled.

## What this proves under test

`SeatLockConcurrencyTest` fires 50 threads at the same seat ID simultaneously (released via a `CountDownLatch` so they all start together) against a real embedded Redis instance — not a mock. Exactly one thread's `tryLockSeats` call succeeds; the other 49 receive a failure and, in the real booking flow, a "seat no longer available" response. A second test confirms that concurrent requests for *disjoint* seats never block each other, so the locking scheme doesn't accidentally serialize unrelated bookings.

## Trade-offs and honest limitations

- **Single Redis instance is a single point of failure.** In production you'd want Redis Sentinel or Cluster for HA; this project uses a single node for simplicity.
- **Not using Redlock.** For a single-instance Redis deployment, plain SETNX is sufficient and simpler to reason about. Redlock (multi-instance consensus locking) would be the next step if Redis itself were clustered and you needed cross-node lock safety guarantees.
- **Optimistic locking (`@Version`) on the `Seat` entity is a secondary safety net**, not the primary mechanism — it guards against any code path that might bypass the Redis lock (e.g., a future admin override endpoint), but the Redis lock is what makes the hot path fast and correct under load.
