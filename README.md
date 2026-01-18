# **Movie Ticket Booking System**

A **RESTful API** for managing **movies, showtimes, and ticket bookings**, built with **Spring Boot 3**, **Java 17**, and **Maven Wrapper**. The system ensures seat availability, validates showtimes, and handles all common errors gracefully.

---

## **Table of Contents**

1. [Features](#features)
2. [Technologies](#technologies)
3. [Getting Started](#getting-started)
4. [API Endpoints](#api-endpoints)
5. [Error Handling](#error-handling)
6. [Running Tests](#running-tests)

---

## **Features**

* **Movie Management**

  * Add, update, delete, and fetch movies
  * Attributes: `title`, `genre`, `duration`, `rating`, `releaseYear`

* **Showtime Management**

  * Add, update, delete, and fetch showtimes
  * Validates overlapping showtimes in the same theater
  * Ensures `startTime < endTime`

* **Ticket Booking**

  * Book tickets for a specific showtime
  * Prevent booking seats that are already taken
  * Validate showtime existence

* **Validation & Exception Handling**

  * `ResourceNotFoundException` → missing movies/showtimes/tickets
  * `ValidationException` → invalid data (seat taken, overlapping showtimes)
  * `MethodArgumentNotValidException` → invalid request body

* **Unit Testing**

  * JUnit 5 + Mockito
  * Service & controller tests with MockMvc

---

## **Technologies**

* **Java 17**
* **Spring Boot 3.x** (MVC, Data JPA)
* **Maven Wrapper (`mvnw`)**
* **H2 Database** (in-memory for dev)
* **JUnit 5 & Mockito** for testing
* **Jackson** for JSON serialization

---

## **Getting Started**

### **Clone the Repository**

```bash
git clone https://github.com/nadine2000/movie-ticket.git
cd movie-ticket
```

### **Build and Run Using Maven Wrapper**

```cmd
.\mvnw clean install      # Build + run tests
.\mvnw spring-boot:run    # Start the application
```

> Runs on `http://localhost:8080` by default.

---

## **API Endpoints**

### **Movies**

| Method | Endpoint       | Description          |
| ------ | -------------- | -------------------- |
| GET    | `/movies`      | Fetch all movies     |
| POST   | `/movies`      | Add a new movie      |
| PUT    | `/movies/{id}` | Update a movie       |
| DELETE | `/movies/{id}` | Delete a movie by ID |

### **Showtimes**

| Method | Endpoint         | Description           |
| ------ | ---------------- | --------------------- |
| GET    | `/showtime/{id}` | Fetch showtime by ID  |
| POST   | `/showtime`      | Add a new showtime    |
| PUT    | `/showtime/{id}` | Update a showtime     |
| DELETE | `/showtime/{id}` | Delete showtime by ID |

### **Tickets**

| Method | Endpoint        | Description                  |
| ------ | --------------- | ---------------------------- |
| POST   | `/tickets`      | Book a ticket for a showtime |
| GET    | `/tickets/{id}` | Get ticket by ID             |

---

## **Error Handling**

The API provides **consistent error responses** for validation and exceptions.

### **Sample Error Response**

```json
{
  "timestamp": "2026-01-18T10:00:00",
  "status": 404,
  "error": "Resource Not Found Error",
  "message": "Showtime with id 999 does not exist.",
  "path": "/showtime/999"
}
```

### **Exceptions**

| Exception                         | HTTP Status | Description                                  |
| --------------------------------- | ----------- | -------------------------------------------- |
| `ResourceNotFoundException`       | 404         | Movie, showtime, or ticket not found         |
| `ValidationException`             | 400         | Invalid input (seat already booked, overlap) |
| `MethodArgumentNotValidException` | 400         | Invalid request body (failed @Valid)         |
| `Exception`                       | 500         | Any unexpected internal error                |

---

## **Running Tests**

* Run all unit tests with Maven Wrapper:

```cmd
.\mvnw test
```

* Tests include:

  * **Controller tests** with MockMvc
  * **Service tests** with Mockito
  * **Validation rules** (seat booking, showtime overlaps)

---
