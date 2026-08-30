# EventBook — Event Ticketing & Seat Booking Platform

A full-stack seat-booking system that solves the classic **overselling-under-concurrency** problem you'd see in real ticketing platforms (Ticketmaster, BookMyShow). When a seat is selected, it's locked with a **Redis TTL-based distributed lock**, not a slow database transaction — verified with an automated test that fires 50 simultaneous requests at the same seat and asserts exactly one wins.

## Why this project exists

Most CRUD portfolio projects don't have a real concurrency story. This one does: the core design decision — how to prevent two people from booking the same seat at the same time, at scale, across multiple backend instances — is the same class of problem production ticketing systems solve. See [`docs/seat-locking-design.md`](docs/seat-locking-design.md) for the full write-up.

## Tech stack

**Backend:** Java 17, Spring Boot 3, Spring Security + JWT, Spring Data JPA, MySQL, Redis, ZXing (QR codes), JUnit 5 + Mockito + embedded Redis for concurrency testing
**Frontend:** React 18, React Router, Axios, Vite
**Infra:** Docker, Docker Compose, GitHub Actions CI

## Architecture

```
React SPA (Nginx) → Spring Boot REST API → MySQL (events, seats, bookings, tickets)
                                          → Redis (seat-hold locks, TTL-based)
                                          → ZXing (QR ticket generation)
```

Layered backend: `controller → service → repository`, with a dedicated `SeatLockService` wrapping all Redis operations and a `BookingService` enforcing the HELD → CONFIRMED / EXPIRED / CANCELLED state machine.

## Core feature: the seat-hold mechanism

1. User selects seats → `POST /api/bookings/hold` attempts an atomic Redis `SETNX` lock per seat with a 5-minute TTL.
2. If **any** seat in the batch is already locked, all locks acquired during that attempt are rolled back — no partial holds.
3. On success, the booking is created with status `HELD` and a countdown starts client-side.
4. `POST /api/bookings/{id}/confirm` re-verifies every lock is still owned by this booking, then marks seats `BOOKED`, issues QR tickets, and releases the (now redundant) Redis keys.
5. If the user abandons checkout, the Redis TTL expires the lock automatically; a scheduled job reconciles the database side every 60 seconds so seat maps and reports stay accurate.

## Getting started (Docker — recommended)

```bash
git clone <your-repo-url>
cd eventbook
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## Running locally without Docker

**Backend**
```bash
cd backend
# requires local MySQL on 3306 and Redis on 6379, or point env vars elsewhere
mvn spring-boot:run
```

**Frontend**
```bash
cd frontend
npm install
npm run dev
```

## Running the tests

```bash
cd backend
mvn test
```

This includes `SeatLockConcurrencyTest` — the test that proves the core claim of this project by firing 50 threads at the same seat against a real embedded Redis instance and asserting exactly one succeeds.

## API documentation

Full endpoint list is available via Swagger at `/swagger-ui.html` once the backend is running, or see [`docs/api-reference.md`](docs/api-reference.md) for a quick-reference table.

## Project structure

```
eventbook/
├── backend/            Spring Boot API (Java 17)
│   ├── src/main/java/com/eventbook/
│   │   ├── config/       Security, Redis, OpenAPI config
│   │   ├── controller/   REST endpoints
│   │   ├── service/      Business logic (SeatLockService, BookingService...)
│   │   ├── repository/   Spring Data JPA repositories
│   │   ├── entity/       JPA entities
│   │   ├── dto/          Request/response objects
│   │   ├── security/     JWT filter, UserDetails, JwtUtil
│   │   ├── exception/    Custom exceptions + global handler
│   │   └── scheduler/    Expired-hold cleanup job
│   └── src/test/java/com/eventbook/
│       ├── service/       Unit tests (Mockito)
│       └── concurrency/   The 50-thread seat-lock race test
├── frontend/           React + Vite SPA
│   └── src/features/    auth, events, seatmap, booking, admin
├── docs/               ER diagram notes, seat-locking design doc, API reference
├── docker-compose.yml
└── .github/workflows/ci.yml
```

## What's implemented vs. what's a natural next step

**Implemented:** JWT auth with roles (ADMIN/ORGANIZER/ATTENDEE), event CRUD + dynamic search/filter/pagination, auto-generated seat maps with tiered pricing, Redis-backed seat holding, two-phase booking confirmation, QR ticket generation, scheduled hold cleanup, admin sales summary, unit + concurrency tests, full Docker setup, CI pipeline.

**Natural extensions (good "what would you add next" interview answers):** real payment gateway integration (Stripe/Razorpay sandbox), WebSocket-based live seat-map updates instead of polling, waitlists for sold-out events, multi-currency support, resend/refund flows, rate limiting on the hold endpoint.

## Resume bullet points

- Built a concurrent-safe seat-booking platform using Redis distributed locking with TTL-based auto-release, verified with a 50-thread test suite achieving zero double-bookings.
- Implemented JWT-based role authentication and a two-phase hold-then-confirm booking state machine mirroring production ticketing systems.
- Designed dynamic event search with pagination, sorting, and multi-criteria filtering using JPA Specifications.
- Generated QR-code tickets with ZXing and containerized the full stack with Docker Compose and GitHub Actions CI.
