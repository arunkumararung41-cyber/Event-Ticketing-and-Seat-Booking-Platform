# 🎟️ Event Ticketing & Seat Booking Platform

A full-stack web application for discovering events, managing venues and seats, and booking event tickets online. The platform provides secure authentication, role-based authorization, event management, venue management, seat availability tracking, and booking functionality.

## 🚀 Features

* User Registration and Login
* JWT-based Authentication
* Role-Based Authorization
* Event Management
* Venue Management
* Seat Management
* Real-Time Seat Availability
* Event Ticket Booking
* Booking Management
* RESTful APIs
* Redis Caching
* MySQL Database
* Frontend and Backend Integration

## 🛠️ Technologies Used

### Backend

* Java
* Spring Boot
* Spring Security
* JWT
* Spring Data JPA
* Hibernate
* REST APIs
* Maven

### Frontend

* React.js
* HTML5
* CSS3
* JavaScript

### Database & Caching

* MySQL
* Redis

### Tools

* Git
* GitHub
* Postman
* Eclipse / Spring Tool Suite
* VS Code

## 🏗️ Architecture

The application follows a layered architecture:

**Controller → Service → Repository → Database**

The frontend communicates with the backend through REST APIs.

## 🔐 Authentication & Authorization

* Users can register and log in securely.
* JWT tokens are used for authentication.
* Role-based authorization controls access to protected operations.
* Admin users can manage events, venues, and seats.
* Users can browse events and make bookings.

## 🎫 Booking Flow

1. User registers or logs in.
2. User browses available events.
3. User selects an event and venue.
4. Available seats are displayed.
5. User selects seats.
6. Booking is created through the REST API.
7. Selected seats become unavailable for subsequent bookings.
8. User can view their booking details.

## ⚡ Redis Caching

Redis is used to cache frequently accessed data such as event information and seat availability to reduce unnecessary database queries and improve application performance.

## 🗄️ Database

MySQL is used for persistent data storage.

Main entities include:

* User
* Event
* Venue
* Seat
* Booking

The entities are connected using appropriate JPA relationships.

## 📌 API Categories

* Authentication APIs
* Event APIs
* Venue APIs
* Seat APIs
* Booking APIs
* User APIs

## 🎯 Project Objectives

* Build a real-world full-stack booking application.
* Implement secure authentication and authorization.
* Practice Spring Boot REST API development.
* Work with JPA entity relationships.
* Implement seat availability and booking logic.
* Integrate React.js with Spring Boot.
* Improve performance using Redis caching.

## 🔮 Future Enhancements

* Online Payment Gateway
* Email Booking Confirmation
* QR Code Ticket Generation
* Event Search and Filtering
* Booking Cancellation and Refunds
* Docker Deployment
* Cloud Deployment

## 👨‍💻 Project Status

**Completed / Under Development** — Core authentication, event, venue, seat, and booking functionality implemented.
