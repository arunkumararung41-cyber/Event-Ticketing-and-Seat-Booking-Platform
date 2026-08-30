import React, { useEffect, useState } from "react";
import client from "../../api/client";

export default function AdminDashboard() {
  const [venues, setVenues] = useState([]);
  const [eventForm, setEventForm] = useState({
    name: "", description: "", category: "Music", venueId: "",
    eventDate: "", basePrice: 500, rows: 8, seatsPerRow: 10
  });
  const [venueForm, setVenueForm] = useState({ name: "", address: "", city: "", totalCapacity: 200 });
  const [createdEvent, setCreatedEvent] = useState(null);
  const [summary, setSummary] = useState(null);
  const [lookupEventId, setLookupEventId] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadVenues = async () => {
    const res = await client.get("/admin/venues");
    setVenues(res.data);
  };

  useEffect(() => { loadVenues(); }, []);

  const updateVenue = (field) => (e) => setVenueForm({ ...venueForm, [field]: e.target.value });
  const updateEvent = (field) => (e) => setEventForm({ ...eventForm, [field]: e.target.value });

  const handleCreateVenue = async (e) => {
    e.preventDefault();
    setError(""); setMessage("");
    try {
      await client.post("/admin/venues", venueForm);
      setMessage("Venue created.");
      setVenueForm({ name: "", address: "", city: "", totalCapacity: 200 });
      loadVenues();
    } catch (err) {
      setError(err.friendlyMessage || "Could not create venue");
    }
  };

  const handleCreateEvent = async (e) => {
    e.preventDefault();
    setError(""); setMessage(""); setCreatedEvent(null);
    try {
      const res = await client.post("/events", {
        ...eventForm,
        venueId: Number(eventForm.venueId),
        basePrice: Number(eventForm.basePrice),
        rows: Number(eventForm.rows),
        seatsPerRow: Number(eventForm.seatsPerRow),
        eventDate: new Date(eventForm.eventDate).toISOString()
      });
      setCreatedEvent(res.data);
      setMessage(`Event "${res.data.name}" published with ${res.data.totalSeats} seats.`);
    } catch (err) {
      setError(err.friendlyMessage || "Could not create event");
    }
  };

  const handleLookupSummary = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const res = await client.get(`/admin/events/${lookupEventId}/sales-summary`);
      setSummary(res.data);
    } catch (err) {
      setError(err.friendlyMessage || "Could not find that event");
    }
  };

  return (
    <div className="container">
      <h1 className="page-title">Organizer / Admin dashboard</h1>
      <p className="page-subtitle">Create venues, publish events, and check live sales.</p>

      {message && <div className="card" style={{ marginBottom: 20, color: "var(--success)" }}>{message}</div>}
      {error && <div className="error-text" style={{ marginBottom: 20 }}>{error}</div>}

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 24, marginBottom: 24 }}>
        <div className="card">
          <h3 style={{ marginBottom: 16 }}>1. Add a venue</h3>
          <form onSubmit={handleCreateVenue}>
            <div className="form-group"><label>Venue name</label>
              <input className="input" required value={venueForm.name} onChange={updateVenue("name")} /></div>
            <div className="form-group"><label>Address</label>
              <input className="input" required value={venueForm.address} onChange={updateVenue("address")} /></div>
            <div className="form-group"><label>City</label>
              <input className="input" required value={venueForm.city} onChange={updateVenue("city")} /></div>
            <div className="form-group"><label>Total capacity</label>
              <input className="input" type="number" value={venueForm.totalCapacity} onChange={updateVenue("totalCapacity")} /></div>
            <button className="btn btn-secondary">Add venue</button>
          </form>
        </div>

        <div className="card">
          <h3 style={{ marginBottom: 16 }}>2. Publish an event</h3>
          <form onSubmit={handleCreateEvent}>
            <div className="form-group"><label>Event name</label>
              <input className="input" required value={eventForm.name} onChange={updateEvent("name")} /></div>
            <div className="form-group"><label>Category</label>
              <select value={eventForm.category} onChange={updateEvent("category")}>
                <option>Music</option><option>Comedy</option><option>Theatre</option><option>Sports</option><option>Conference</option>
              </select></div>
            <div className="form-group"><label>Venue</label>
              <select required value={eventForm.venueId} onChange={updateEvent("venueId")}>
                <option value="">Select a venue</option>
                {venues.map((v) => <option key={v.id} value={v.id}>{v.name} — {v.city}</option>)}
              </select></div>
            <div className="form-group"><label>Date & time</label>
              <input className="input" type="datetime-local" required value={eventForm.eventDate} onChange={updateEvent("eventDate")} /></div>
            <div className="form-group"><label>Base price (₹)</label>
              <input className="input" type="number" value={eventForm.basePrice} onChange={updateEvent("basePrice")} /></div>
            <div style={{ display: "flex", gap: 12 }}>
              <div className="form-group" style={{ flex: 1 }}><label>Rows</label>
                <input className="input" type="number" value={eventForm.rows} onChange={updateEvent("rows")} /></div>
              <div className="form-group" style={{ flex: 1 }}><label>Seats per row</label>
                <input className="input" type="number" value={eventForm.seatsPerRow} onChange={updateEvent("seatsPerRow")} /></div>
            </div>
            <button className="btn btn-primary">Publish event</button>
          </form>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: 16 }}>3. Live sales summary</h3>
        <form onSubmit={handleLookupSummary} style={{ display: "flex", gap: 10, marginBottom: 16 }}>
          <input className="input" placeholder="Event ID" value={lookupEventId} onChange={(e) => setLookupEventId(e.target.value)} />
          <button className="btn btn-secondary">Look up</button>
        </form>
        {summary && (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: 16, textAlign: "center" }}>
            <div><div style={{ color: "var(--text-dim)", fontSize: "0.8rem" }}>Total seats</div><strong>{summary.totalSeats}</strong></div>
            <div><div style={{ color: "var(--text-dim)", fontSize: "0.8rem" }}>Booked</div><strong style={{ color: "var(--success)" }}>{summary.bookedSeats}</strong></div>
            <div><div style={{ color: "var(--text-dim)", fontSize: "0.8rem" }}>Held</div><strong style={{ color: "var(--amber)" }}>{summary.heldSeats}</strong></div>
            <div><div style={{ color: "var(--text-dim)", fontSize: "0.8rem" }}>Available</div><strong>{summary.availableSeats}</strong></div>
            <div><div style={{ color: "var(--text-dim)", fontSize: "0.8rem" }}>Gross revenue</div><strong>₹{Number(summary.grossRevenue).toFixed(0)}</strong></div>
          </div>
        )}
      </div>
    </div>
  );
}
