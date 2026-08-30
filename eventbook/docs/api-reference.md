# API Reference (quick view)

Full interactive docs at `/swagger-ui.html` when the backend is running.

## Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create account (ATTENDEE or ORGANIZER) |
| POST | `/api/auth/login` | Public | Returns JWT |

## Events
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/events?category=&city=&query=&page=&size=&sort=` | Public | Search/filter/paginate events |
| GET | `/api/events/{id}` | Public | Event details + seat availability count |
| POST | `/api/events` | ADMIN/ORGANIZER | Create event + auto-generate seat map |
| GET | `/api/events/{eventId}/seats` | Public | Full seat map with live status |

## Bookings
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/bookings/hold` | Authenticated | Acquire Redis locks on selected seats |
| POST | `/api/bookings/{id}/confirm` | Authenticated (owner) | Finalize booking, issue tickets |
| DELETE | `/api/bookings/{id}` | Authenticated (owner) | Cancel a HELD booking |
| GET | `/api/bookings/me` | Authenticated | Booking history for current user |

## Tickets
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/tickets/{id}` | Authenticated | Ticket details + base64 QR code |

## Admin
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/admin/events/{eventId}/sales-summary` | ADMIN | Booked/held/available counts + revenue |
| POST | `/api/admin/venues` | ADMIN/ORGANIZER | Create a venue |
| GET | `/api/admin/venues` | ADMIN/ORGANIZER | List venues |

## Sample request: hold seats

```http
POST /api/bookings/hold
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "eventId": 1,
  "seatIds": [12, 13, 14]
}
```

Response (success):
```json
{
  "bookingId": 42,
  "eventId": 1,
  "eventName": "Coldplay Live",
  "status": "HELD",
  "seatNumbers": ["B3", "B4", "B5"],
  "expiresAt": "2026-08-21T10:15:00Z"
}
```

Response (seat already taken — HTTP 409):
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Seat(s) already being booked by someone else: [13]. Please choose different seats."
}
```
