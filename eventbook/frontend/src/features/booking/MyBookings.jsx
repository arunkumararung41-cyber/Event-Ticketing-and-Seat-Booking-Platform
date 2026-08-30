import React, { useEffect, useState } from "react";
import client from "../../api/client";

export default function MyBookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client.get("/bookings/me").then((res) => {
      setBookings(res.data);
      setLoading(false);
    });
  }, []);

  if (loading) return <div className="spinner">Loading your bookings...</div>;

  return (
    <div className="container">
      <h1 className="page-title">My bookings</h1>
      <p className="page-subtitle">Everything you've held, confirmed, or let expire.</p>

      {bookings.length === 0 ? (
        <div className="empty-state">No bookings yet. Go find an event you like.</div>
      ) : (
        <div className="card booking-list">
          {bookings.map((b) => (
            <div className="booking-row" key={b.bookingId}>
              <div>
                <strong>{b.eventName}</strong>
                <div style={{ color: "var(--text-dim)", fontSize: "0.85rem" }}>
                  Seats: {b.seatNumbers.join(", ")}
                </div>
              </div>
              <span className={`status-pill status-${b.status}`}>{b.status}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
