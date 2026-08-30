import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import client from "../../api/client";
import { useAuth } from "../../context/AuthContext.jsx";

export default function EventDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [event, setEvent] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selected, setSelected] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [holding, setHolding] = useState(false);

  const load = async () => {
    setLoading(true);
    const [evRes, seatRes] = await Promise.all([
      client.get(`/events/${id}`),
      client.get(`/events/${id}/seats`)
    ]);
    setEvent(evRes.data);
    setSeats(seatRes.data);
    setLoading(false);
  };

  useEffect(() => { load(); }, [id]);

  const toggleSeat = (seat) => {
    if (seat.status !== "AVAILABLE") return;
    setSelected((prev) =>
      prev.includes(seat.id) ? prev.filter((s) => s !== seat.id) : [...prev, seat.id]
    );
  };

  const rows = seats.reduce((acc, s) => {
    (acc[s.seatRow] = acc[s.seatRow] || []).push(s);
    return acc;
  }, {});

  const selectedSeats = seats.filter((s) => selected.includes(s.id));
  const total = selectedSeats.reduce((sum, s) => sum + Number(s.price), 0);

  const handleHold = async () => {
    if (!user) { navigate("/login"); return; }
    setError("");
    setHolding(true);
    try {
      const res = await client.post("/bookings/hold", { eventId: Number(id), seatIds: selected });
      navigate(`/checkout/${res.data.bookingId}`);
    } catch (err) {
      setError(err.friendlyMessage || "Could not hold seats — someone may have just booked them.");
      load(); // refresh seat statuses so the map reflects reality
      setSelected([]);
    } finally {
      setHolding(false);
    }
  };

  if (loading) return <div className="spinner">Loading event...</div>;
  if (!event) return <div className="empty-state">Event not found.</div>;

  return (
    <div className="container">
      <span className="category-tag">{event.category}</span>
      <h1 className="page-title">{event.name}</h1>
      <p className="page-subtitle">{event.venueName} · {event.city} · {new Date(event.eventDate).toLocaleString()}</p>

      <div className="stage-wrap"><div className="stage">STAGE</div></div>

      <div className="legend">
        <div className="legend-item"><div className="legend-swatch" style={{ background: "var(--bg-card)", border: "1px solid var(--line)" }}></div>Available</div>
        <div className="legend-item"><div className="legend-swatch" style={{ background: "var(--violet)" }}></div>Selected</div>
        <div className="legend-item"><div className="legend-swatch" style={{ background: "var(--bg-elevated)", border: "1px solid var(--amber-dim)" }}></div>Held by someone</div>
        <div className="legend-item"><div className="legend-swatch" style={{ background: "var(--bg-elevated)" }}></div>Booked</div>
      </div>

      <div className="seat-map">
        {Object.entries(rows).map(([rowLabel, rowSeats]) => (
          <div className="seat-row" key={rowLabel}>
            <span className="seat-row-label">{rowLabel}</span>
            {rowSeats.map((seat) => (
              <button
                key={seat.id}
                className={`seat ${seat.status.toLowerCase()} ${selected.includes(seat.id) ? "selected" : ""}`}
                onClick={() => toggleSeat(seat)}
                title={`${seat.seatNumber} · ${seat.section} · ₹${seat.price}`}
              >
                {seat.seatNumber.replace(rowLabel, "")}
              </button>
            ))}
          </div>
        ))}
      </div>

      {error && <div className="error-text" style={{ textAlign: "center" }}>{error}</div>}

      {selected.length > 0 && (
        <div className="checkout-bar">
          <div>
            <strong>{selected.length} seat{selected.length > 1 ? "s" : ""}</strong>
            <span style={{ color: "var(--text-dim)" }}> · {selectedSeats.map((s) => s.seatNumber).join(", ")}</span>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
            <strong>₹{total.toFixed(0)}</strong>
            <button className="btn btn-primary" onClick={handleHold} disabled={holding}>
              {holding ? "Holding seats..." : "Hold seats — 5 min"}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
