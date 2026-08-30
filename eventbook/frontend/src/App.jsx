import React from "react";
import { Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import Login from "./features/auth/Login.jsx";
import Register from "./features/auth/Register.jsx";
import EventList from "./features/events/EventList.jsx";
import EventDetail from "./features/seatmap/EventDetail.jsx";
import Checkout from "./features/booking/Checkout.jsx";
import MyBookings from "./features/booking/MyBookings.jsx";
import AdminDashboard from "./features/admin/AdminDashboard.jsx";

export default function App() {
  return (
    <div className="app-shell">
      <Navbar />
      <Routes>
        <Route path="/" element={<EventList />} />
        <Route path="/events/:id" element={<EventDetail />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/checkout/:bookingId"
          element={<ProtectedRoute><Checkout /></ProtectedRoute>}
        />
        <Route
          path="/bookings"
          element={<ProtectedRoute><MyBookings /></ProtectedRoute>}
        />
        <Route
          path="/my-bookings"
          element={<ProtectedRoute><MyBookings /></ProtectedRoute>}
        />
        <Route
          path="/admin"
          element={<ProtectedRoute roles={["ADMIN", "ORGANIZER"]}><AdminDashboard /></ProtectedRoute>}
        />
      </Routes>
    </div>
  );
}
