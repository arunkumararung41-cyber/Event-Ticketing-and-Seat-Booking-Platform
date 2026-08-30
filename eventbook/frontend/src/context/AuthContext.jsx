import React, { createContext, useContext, useEffect, useState } from "react";
import client from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem("eventbook_user");
    if (stored) setUser(JSON.parse(stored));
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const res = await client.post("/auth/login", { email, password });
    persistSession(res.data);
  };

  const register = async (name, email, password, role) => {
    const res = await client.post("/auth/register", { name, email, password, role });
    persistSession(res.data);
  };

  const persistSession = (data) => {
    localStorage.setItem("eventbook_token", data.token);
    const sessionUser = { id: data.userId, name: data.name, email: data.email, role: data.role };
    localStorage.setItem("eventbook_user", JSON.stringify(sessionUser));
    setUser(sessionUser);
  };

  const logout = () => {
    localStorage.removeItem("eventbook_token");
    localStorage.removeItem("eventbook_user");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
