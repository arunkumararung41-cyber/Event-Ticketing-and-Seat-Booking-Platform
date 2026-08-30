import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "", role: "ATTENDEE" });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await register(form.name, form.email, form.password, form.role);
      navigate("/");
    } catch (err) {
      setError(err.friendlyMessage || "Registration failed");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container auth-page">
      <div className="card">
        <h2 style={{ marginBottom: 20 }}>Create an account</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Full name</label>
            <input className="input" required value={form.name} onChange={update("name")} />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input className="input" type="email" required value={form.email} onChange={update("email")} />
          </div>
          <div className="form-group">
            <label>Password (min 6 characters)</label>
            <input className="input" type="password" minLength={6} required value={form.password} onChange={update("password")} />
          </div>
          <div className="form-group">
            <label>I am signing up as</label>
            <select value={form.role} onChange={update("role")}>
              <option value="ATTENDEE">Attendee — I want to book tickets</option>
              <option value="ORGANIZER">Organizer — I want to publish events</option>
            </select>
          </div>
          {error && <div className="error-text">{error}</div>}
          <button className="btn btn-primary" style={{ width: "100%", marginTop: 8 }} disabled={submitting}>
            {submitting ? "Creating account..." : "Sign up"}
          </button>
        </form>
        <p style={{ marginTop: 16, color: "var(--text-dim)", fontSize: "0.9rem" }}>
          Already have an account? <Link to="/login" style={{ color: "var(--violet)" }}>Log in</Link>
        </p>
      </div>
    </div>
  );
}
