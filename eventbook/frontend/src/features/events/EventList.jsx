import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import client from "../../api/client";

export default function EventList() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [category, setCategory] = useState("");
  const [city, setCity] = useState("");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchEvents = async () => {
    setLoading(true);
    try {
      const res = await client.get("/events", {
        params: { category: category || undefined, city: city || undefined, query: query || undefined, page, size: 9, sort: "eventDate,asc" }
      });
      setEvents(res.data.content);
      setTotalPages(res.data.totalPages);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchEvents(); }, [page]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    fetchEvents();
  };

  return (
    <div className="container">
      <h1 className="page-title">Upcoming events</h1>
      <p className="page-subtitle">Search, filter, and book real-time seats.</p>

      <form className="filters" onSubmit={handleSearch}>
        <input className="input" placeholder="Search events..." value={query} onChange={(e) => setQuery(e.target.value)} />
        <select value={category} onChange={(e) => setCategory(e.target.value)}>
          <option value="">All categories</option>
          <option value="Music">Music</option>
          <option value="Comedy">Comedy</option>
          <option value="Theatre">Theatre</option>
          <option value="Sports">Sports</option>
          <option value="Conference">Conference</option>
        </select>
        <input className="input" placeholder="City" value={city} onChange={(e) => setCity(e.target.value)} />
        <button className="btn btn-secondary">Search</button>
      </form>

      {loading ? (
        <div className="spinner">Loading events...</div>
      ) : events.length === 0 ? (
        <div className="empty-state">No events match your search yet. Try clearing a filter.</div>
      ) : (
        <>
          <div className="event-grid">
            {events.map((ev) => {
              const pct = ev.totalSeats ? Math.round((ev.availableSeats / ev.totalSeats) * 100) : 0;
              return (
                <Link to={`/events/${ev.id}`} className="event-card" key={ev.id}>
                  <span className="category-tag">{ev.category}</span>
                  <h3>{ev.name}</h3>
                  <span className="meta">{ev.venueName} · {ev.city}</span>
                  <span className="meta">{new Date(ev.eventDate).toLocaleString()}</span>
                  <div className="availability-bar"><div className="availability-fill" style={{ width: `${pct}%` }} /></div>
                  <span className="meta">{ev.availableSeats} / {ev.totalSeats} seats left</span>
                  <span className="price">From ₹{Number(ev.basePrice).toFixed(0)}</span>
                </Link>
              );
            })}
          </div>
          <div style={{ display: "flex", gap: 10, justifyContent: "center", marginTop: 30 }}>
            <button className="btn btn-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
            <span style={{ alignSelf: "center", color: "var(--text-dim)" }}>Page {page + 1} of {Math.max(totalPages, 1)}</span>
            <button className="btn btn-secondary" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Next</button>
          </div>
        </>
      )}
    </div>
  );
}
