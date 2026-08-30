import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="navbar">
      <Link to="/" className="brand"><span className="dot"></span>EventBook</Link>
      <div className="nav-links">
        <Link to="/">Events</Link>
        {user && <Link to="/my-bookings">My Bookings</Link>}
        {(user?.role === "ADMIN" || user?.role === "ORGANIZER") && <Link to="/admin">Admin</Link>}
        {user ? (
          <button onClick={() => { logout(); navigate("/"); }}>Log out ({user.name.split(" ")[0]})</button>
        ) : (
          <>
            <Link to="/login">Log in</Link>
            <Link to="/register" className="btn btn-primary" style={{ padding: "8px 16px" }}>Sign up</Link>
          </>
        )}
      </div>
    </div>
  );
}
