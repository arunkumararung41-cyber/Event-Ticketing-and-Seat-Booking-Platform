import React, { useEffect, useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import client from "../../api/client";

function useCountdown(expiresAt) {
  const [remaining, setRemaining] = useState(0);

  useEffect(() => {
    if (!expiresAt) return;
    const target = new Date(expiresAt).getTime();
    const tick = () => setRemaining(Math.max(0, Math.floor((target - Date.now()) / 1000)));
    tick();
    const interval = setInterval(tick, 1000);
    return () => clearInterval(interval);
  }, [expiresAt]);

  const minutes = String(Math.floor(remaining / 60)).padStart(2, "0");
  const seconds = String(remaining % 60).padStart(2, "0");
  return { remaining, label: `${minutes}:${seconds}` };
}

export default function Checkout() {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState("");
  const [confirming, setConfirming] = useState(false);

  const load = async () => {
    const res = await client.get("/bookings/me");
    const found = res.data.find((b) => b.bookingId === Number(bookingId));
    setBooking(found);
  };

  useEffect(() => { load(); }, [bookingId]);

  const { remaining, label } = useCountdown(booking?.expiresAt);

  useEffect(() => {
    if (booking && booking.status === "HELD" && remaining === 0) {
      setError("Your hold expired. The seats have been released back to the pool.");
    }
  }, [remaining, booking]);

  const handleConfirm = async () => {
    setConfirming(true);
    setError("");
    try {
      await client.post(`/bookings/${bookingId}/confirm`);
      navigate(`/bookings`);
    } catch (err) {
      setError(err.friendlyMessage || "Could not confirm booking — the hold may have expired.");
    } finally {
      setConfirming(false);
    }
  };

  const handleCancel = async () => {
    await client.delete(`/bookings/${bookingId}`);
    navigate("/");
  };

  if (!booking) return <div className="spinner">Loading your hold...</div>;

  return (
    <div className="container auth-page">
      <div className="card">
        <h2>Confirm your booking</h2>
        <p style={{ color: "var(--text-dim)", marginTop: 4 }}>{booking.eventName}</p>

        <div style={{ margin: "20px 0" }}>
          <strong>Seats:</strong> {booking.seatNumbers.join(", ")}
        </div>

        {booking.status === "HELD" && remaining > 0 && (
          <div style={{ marginBottom: 20 }}>
            <span style={{ color: "var(--text-dim)" }}>Seats held for </span>
            <span className="hold-timer">{label}</span>
            <p style={{ color: "var(--text-dim)", fontSize: "0.85rem", marginTop: 6 }}>
              This is a real distributed lock in Redis — if it expires, your seats return to the pool automatically.
            </p>
          </div>
        )}

        {error && <div className="error-text">{error}</div>}

        <div style={{ display: "flex", gap: 12, marginTop: 20 }}>
          <button className="btn btn-primary" onClick={handleConfirm} disabled={confirming || remaining === 0}>
            {confirming ? "Confirming..." : "Confirm & pay (simulated)"}
          </button>
          <button className="btn btn-danger" onClick={handleCancel}>Cancel hold</button>
        </div>
      </div>
    </div>
  );
}
