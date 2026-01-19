# 🎬 Movie Ticket Booking System

A Spring Boot REST API for managing movies, showtimes, and ticket bookings. This guide is written so **anyone can clone, run, and understand the project easily**, even with minimal Spring Boot experience.

---

## 📌 Table of Contents

1. Overview
2. Prerequisites
3. Project Setup
4. Running the Application
5. Running Tests
6. API Endpoints
7. Error Handling
8. Useful Maven Commands
9. Project Structure
10. Notes & Tips

---

## 1. Overview

This project demonstrates:

* Clean Spring Boot REST API design
* Validation and exception handling
* JPA/Hibernate with H2 database
* Unit & integration testing with JUnit and Mockito

**Main Features:**

* Manage Movies
* Manage Showtimes (with overlap validation)
* Book Tickets (prevent duplicate seat booking)

---

## 2. Prerequisites

Make sure you have the following installed:

* **Java JDK 21** ✅ (required)

  ```bash
  java -version
  ```
* **Maven** (or use the included Maven Wrapper)
* **IntelliJ IDEA** (recommended) or any Java IDE
* **Postman** (for API testing)

> ⚠️ If your system default Java is not 21, configure your IDE or `JAVA_HOME` to use Java 21.

---

## 3. Project Setup

### 3.1 Clone the Repository

```bash
git clone https://github.com/nadine2000/movie-ticket.git
cd movie-ticket
```

### 3.2 Open in IntelliJ IDEA

1. Open IntelliJ IDEA
2. Click **File → Open**
3. Select the `movie-ticket` folder
4. IntelliJ will automatically detect Maven and download dependencies

---

## 4. Running the Application

### Option 1: Run with IntelliJ (Recommended)

1. Open `MovieticketApplication.java`
2. Click the green ▶ **Run** button

OR create a run configuration:

* Run → Edit Configurations
* Add **Spring Boot**
* Main class: `com.example.movieticket.MovieticketApplication`

✅ Successful startup message:

```
Started MovieticketApplication in X seconds
```

📍 Application URL:

```
http://localhost:8080
```

---

### Option 2: Run from Terminal

Using Maven Wrapper (recommended):

```bash
./mvnw spring-boot:run
```
---

## 5. Running Tests

### Run All Tests

**IntelliJ:**

* Right-click `src/test/java`
* Select **Run 'Tests in 'java''**

**Terminal:**

```bash
./mvnw test
```

---

### Run Specific Test Class

```bash
./mvnw test -Dtest=MovieServiceTest
./mvnw test -Dtest=MovieControllerTest
```

### Run Single Test Method

```bash
./mvnw test -Dtest=MovieServiceTest#shouldAddMovie_WhenValidMovieProvided
```

---

## 6. API Endpoints

Base URL:

```
http://localhost:8080
```

---
### 🎥 Movies API

#### 1. Get All Movies

```http
GET http://localhost:8080/movies/all
```

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "title": "The Matrix",
    "genre": "Sci-Fi",
    "duration": 136,
    "rating": 8.7,
    "releaseYear": 1999
  }
]
```

#### 2. Add New Movie

```http
POST http://localhost:8080/movies
Content-Type: application/json
```

**Request Body:**

```json
{
  "title": "Inception",
  "genre": "Thriller",
  "duration": 148,
  "rating": 8.8,
  "releaseYear": 2010
}
```

**Response (200 OK):**

```json
{
  "id": 2,
  "title": "Inception",
  "genre": "Thriller",
  "duration": 148,
  "rating": 8.8,
  "releaseYear": 2010
}
```

> ⚠️ Possible Errors:
>
> * Missing required fields → **400 Bad Request**
> * Duration, rating, or releaseYear out of valid range → **400 Bad Request**

#### 3. Update Movie

```http
PUT http://localhost:8080/movies/{title}
Content-Type: application/json
```

**Request Body:**

```json
{
  "title": "Inception Updated",
  "genre": "Sci-Fi",
  "duration": 148,
  "rating": 9.0,
  "releaseYear": 2010
}
```

**Response (200 OK):**

```json
{
  "id": 2,
  "title": "Inception Updated",
  "genre": "Sci-Fi",
  "duration": 148,
  "rating": 9.0,
  "releaseYear": 2010
}
```

> ⚠️ Possible Errors:
>
> * Movie not found → **404 Not Found**
> * Missing or invalid fields → **400 Bad Request**

#### 4. Delete Movie

```http
DELETE http://localhost:8080/movies/{title}
```

**Response (200 OK):**

```json
"Movie with title {title} was deleted successfully."
```

> ⚠️ Movie not found → **404 Not Found**

---

### 🎭 Showtimes API

#### 1. Get Showtime by ID

```http
GET http://localhost:8080/showtimes/{id}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "movieId": 2,
  "theater": "Theater A",
  "startTime": "2026-01-15T19:00:00",
  "endTime": "2026-01-15T21:30:00",
  "price": 12.50
}
```

> ⚠️ Showtime not found → **404 Not Found**

#### 2. Add New Showtime

```http
POST http://localhost:8080/showtimes
Content-Type: application/json
```

**Request Body:**

```json
{
  "movieId": 2,
  "theater": "Theater A",
  "startTime": "2026-01-15T19:00:00",
  "endTime": "2026-01-15T21:30:00",
  "price": 12.50
}
```

**Response (200 OK):**

```json
{
  "id": 3,
  "movieId": 2,
  "theater": "Theater A",
  "startTime": "2026-01-15T19:00:00",
  "endTime": "2026-01-15T21:30:00",
  "price": 12.50
}
```

> ⚠️ Possible Errors:
>
> * Missing required fields (movieId, theater, startTime, endTime, price) → **400 Bad Request**
> * End time before start time → **400 Bad Request**
> * Overlapping showtime → **400 Bad Request**
> * Invalid movie ID → **404 Not Found**

#### 3. Update Showtime

```http
PUT http://localhost:8080/showtimes/{id}
Content-Type: application/json
```

**Request Body:**

```json
{
  "movieId": 2,
  "theater": "Theater B",
  "startTime": "2026-01-16T20:00:00",
  "endTime": "2026-01-16T22:30:00",
  "price": 15.00
}
```

**Response (200 OK):**

```json
{
  "id": 3,
  "movieId": 2,
  "theater": "Theater B",
  "startTime": "2026-01-16T20:00:00",
  "endTime": "2026-01-16T22:30:00",
  "price": 15.00
}
```

> ⚠️ Possible Errors:
>
> * Showtime not found → **404 Not Found**
> * Missing or invalid fields → **400 Bad Request**
> * Overlapping showtime → **400 Bad Request**

#### 4. Delete Showtime

```http
DELETE http://localhost:8080/showtimes/{id}
```

**Response (200 OK):**

```json
"Showtime with id {id} was deleted successfully."
```

> ⚠️ Showtime not found → **404 Not Found**

---

### 🎟 Tickets (Bookings)

#### 1. Book Ticket

```http
POST http://localhost:8080/bookings
Content-Type: application/json
```

**Request Body:**

```json
{
  "showtimeId": 3,
  "seatNumber": 15,
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (200 OK):**

```json
{
  "bookingId": "660e8400-e29b-41d4-a716-446655440001",
  "showtimeId": 3,
  "seatNumber": 15,
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

> ⚠️ Possible Errors:
>
> * Missing required fields → **400 Bad Request**
> * Seat number less than 1 or invalid → **400 Bad Request**
> * Seat already booked → **409 Conflict**
> * Showtime ID does not exist → **404 Not Found**

---

## 7. Error Handling

The project includes **detailed validation and conflict handling** for all key operations:

### Common Error Scenarios

1. **Validation Errors (400)**

    * Invalid input fields (missing or incorrect data)
    * Entity-level validations (e.g., movie duration positive)

2. **Resource Not Found (404)**

    * Movie, Showtime, or Ticket ID does not exist

3. **Conflict Errors (400)**

    * **Seat Already Booked** for a showtime
    * **Overlapping Showtime** in the same theater
    * **start time > end time** for a showtime

### Error Response Example

```json
{
  "timestamp": "2026-01-19T12:00:00",
  "status": 400,
  "error": "Validation Error",
  "message": {
      "seatNumber": "Seat is already booked"
  },
  "path": "/bookings"
}
```

The `GlobalExceptionHandler` centralizes these responses so that every exception—validation, entity conflict, or not found—returns a clear JSON object.

---

## 8. Useful Maven Commands

```bash
./mvnw clean
./mvnw clean install
./mvnw clean package
```

---

## 9. Project Structure

```
movieticket/
├── src/
│   ├── main/
│   │   ├── java/com/example/movieticket/
│   │   │   ├── movie/
│   │   │   │   ├── Movie.java
│   │   │   │   ├── MovieController.java
│   │   │   │   ├── MovieService.java
│   │   │   │   └── MovieRepository.java
│   │   │   ├── showtime/
│   │   │   │   ├── Showtime.java
│   │   │   │   ├── ShowtimeController.java
│   │   │   │   ├── ShowtimeService.java
│   │   │   │   └── ShowtimeRepository.java
│   │   │   ├── ticket/
│   │   │   │   ├── Ticket.java
│   │   │   │   ├── TicketController.java
│   │   │   │   ├── TicketService.java
│   │   │   │   └── TicketRepository.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── ValidationException.java
│   │   │   └── MovieticketApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/example/movieticket/
│       │   ├── movieTest/
│       │   │   ├── MovieControllerTest.java
│       │   │   ├── MovieServiceTest.java
│       │   │   └── MovieRepositoryTest.java
│       │   ├── showtimeTest/
│       │   │   ├── ShowtimeControllerTest.java
│       │   │   ├── ShowtimeServiceTest.java
│       │   │   └── ShowtimeRepositoryTest.java
│       │   └── ticketTest/
│       │       ├── TicketControllerTest.java
│       │       ├── TicketServiceTest.java
│       │       └── TicketRepositoryTest.java
│       └── resources/
│           └── application-test.properties
├── pom.xml
└── README.md
```

---

## 10. Notes & Tips

* Uses **H2 in-memory database** (no setup required)
* Database resets on application restart
* Validation prevents:

    * Invalid showtimes/moives/tickets
    * Overlapping showtimes in same theater
    * Double seat booking
* Error messages now clearly indicate **field-level issues** and **business logic conflicts**

---