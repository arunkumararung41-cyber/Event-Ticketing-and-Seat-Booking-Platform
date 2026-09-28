# 🎟️ Event Ticketing & Seat Booking Platform

A full-stack **Event Ticketing and Seat Booking Platform** that allows users to discover events, view available seats, and book tickets online.

The application provides secure authentication, role-based authorization, event management, venue and seat management, booking management, and Redis-based caching for improved performance.

---

## 📌 Project Overview

The Event Ticketing & Seat Booking Platform is designed using a modern full-stack architecture with **React.js for the frontend** and **Spring Boot for the backend**.

Users can register and log in securely, browse available events, select seats, and make bookings. Administrators can manage events, venues, seats, and other platform operations through protected APIs.

---

## 🚀 Key Features

### 👤 User Features

- User Registration
- User Login
- JWT-based Authentication
- Browse Events
- View Event Details
- View Available Seats
- Select Seats
- Book Tickets
- View Booking Details
- Manage User Bookings

### 🛡️ Admin Features

- Admin Authentication
- Role-Based Authorization
- Create and Manage Events
- Create and Manage Venues
- Manage Seats
- Manage Event Information
- View Booking Information

### ⚡ Performance

- Redis caching for frequently accessed data
- Cached event and seat availability information
- Reduced unnecessary database queries

---

## 🛠️ Technologies Used

### Backend

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- REST APIs
- Maven

### Frontend

- React.js
- JavaScript
- HTML5
- CSS3

### Database

- MySQL

### Caching

- Redis

### Development Tools

- Eclipse / Spring Tool Suite
- Visual Studio Code
- Git
- GitHub
- Postman
- MySQL Workbench

---

## 🏗️ Architecture

The application follows a layered backend architecture:

                    ┌───────────────┐
                    │  React.js UI  │
                    └───────┬───────┘
                            │
                       REST APIs
                            │
                    ┌───────▼───────┐
                    │ Spring Boot   │
                    │   Backend     │
                    └───────┬───────┘
                            │
                 ┌──────────┴──────────┐
                 │                     │
          ┌──────▼──────┐       ┌──────▼──────┐
          │    Redis    │       │    MySQL    │
          │    Cache    │       │   Database  │
          └─────────────┘       └─────────────┘


Authentication & Authorization

The application uses Spring Security with JWT for secure authentication and authorization.

Authentication Flow
User
 ↓
Login
 ↓
Spring Security
 ↓
JWT Token Generated
 ↓
Frontend Stores Token
 ↓
Token Sent With Protected Requests
 ↓
Backend Validates Token
 ↓
Request Allowed / Rejected

Role-Based Access

The application supports different user roles.

For example:
USER
 ↓
Browse Events
 ↓
View Seats
 ↓
Book Tickets
 ↓
View Bookings

ADMIN
 ↓
Manage Events
 ↓
Manage Venues
 ↓
Manage Seats
 ↓
Manage Bookings

🎫 Booking Flow

The typical booking process is:
1. User Registration
        ↓
2. User Login
        ↓
3. Browse Events
        ↓
4. Select Event
        ↓
5. Select Venue
        ↓
6. View Available Seats
        ↓
7. Select Seats
        ↓
8. Confirm Booking
        ↓
9. Booking Stored in Database
        ↓
10. Selected Seats Become Unavailable

⚡ Redis Caching

Redis is used to improve application performance.

Frequently accessed information such as:

Event information
Seat availability
Frequently requested data

can be cached in Redis.

This helps reduce repeated database queries and improves response time.

⚙️ Prerequisites

Before running the project, make sure the following are installed:

Java 17 or later,
Maven,
Node.js,
npm,
MySQL,
Redis,
Git

▶️ How to Run the Project

🔹 Backend — Spring Boot

Open the backend project in STS/Eclipse and run the main Spring Boot application:

Run As → Spring Boot App

Or using Maven:

mvn spring-boot:run

Backend will run on:

http://localhost:8080

🔹 Frontend — React.js

Open the frontend folder in VS Code:

npm install
npm start

If you're using Vite:

npm install
npm run dev

Frontend will normally run on:

http://localhost:3000

or, with Vite:

http://localhost:5173

🔹 Required

Make sure MySQL and Redis are running before starting the backend.

## Home Page
<img width="1859" height="878" alt="Home page" src="https://github.com/user-attachments/assets/f68332b0-ee40-4c27-879d-7bcb6ee70fdd" />



## Login Page

<img width="1853" height="823" alt="Login page" src="https://github.com/user-attachments/assets/248b8bcb-f9fc-4043-85fb-b1ef2d754acb" />


## Event Page

<img width="1873" height="908" alt="Event Booking" src="https://github.com/user-attachments/assets/67332158-bca0-482d-8efa-388a3790c29d" />


## Seat Selection

<img width="1844" height="893" alt="Seat Booking" src="https://github.com/user-attachments/assets/44c02c65-19fd-488b-8958-f3db56cb405c" />


## Booking Page
<img width="1857" height="802" alt="Booking Page" src="https://github.com/user-attachments/assets/3e7433ee-051e-4b35-aad3-d3cd9be3bfab" />



## Admin Dashboard
<img width="1850" height="904" alt="Admin page" src="https://github.com/user-attachments/assets/7654e2a0-d068-4fad-b47d-4e01fa7fe78d" />



👨‍💻 Author

Arun Kumar

GitHub:
https://github.com/arunkumararung41-cyber
