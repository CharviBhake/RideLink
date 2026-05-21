RideLink 
A scalable system for a carpooling platform that enables users to share rides, reduce travel costs, and minimize environmental impact through efficient ride matching and real-time communication.

- Overview
RideLink provides a safe, secure, and efficient way to connect users traveling in the same direction. The backend handles ride management, booking workflows, real-time chat, geospatial search, and user analytics such as savings and carbon footprint.

- Features
 Authentication & Security
* JWT-based authentication (Login / Signup)
* Refresh token mechanism
* Secure logout with Redis-based JWT blacklist

- Ride Management
* Create and publish rides
* Search rides using geospatial queries (MongoDB 2dsphere index)
* Match rides based on proximity and destination
* Dynamic seat availability tracking

- Booking System
* Book seats in a ride
* Driver can Confirm/reject booking requests
* Role-based ride participation (Driver / Passenger)

- Real-time Chat
* WebSocket-based chat system
* Dedicated chat room per ride
* Enables communication between all ride participants

- Rating & Review System
* Passengers can rate drivers after ride completion
* Supports:
  * ⭐ Star ratings (1–5)
  * ✍️ Comments
* Prevents duplicate reviews per ride

- Analytics & Insights
* CO₂ savings calculation per ride
* User-level cumulative environmental impact
* Travel cost savings estimation
* Ride history (Driver + Passenger views)

- User Profile
* Editable user profile
* Stores:
  * Contact info
  * Location
  * Vehicle details
* Tracks total rides and activity

🧠 Tech Stack

| Layer            | Technology                         |
| ---------------- | ---------------------------------- |
| Backend          | Spring Boot                        |
| Database         | MongoDB (with Geospatial Indexing) |
| Authentication   | JWT (Access + Refresh Tokens)      |
| Cache / Security | Redis (JWT Blacklisting)           |
| Real-time        | WebSockets                         |
| Deployment       | Railway                            |


🏗️ Architecture
The backend follows a layered architecture:
Controller → Service → Repository → Database
Structure:
* Controller→ Handles HTTP requests
* Service → Business logic
* Repository → Database interaction
* Entity→ Data models
* Config → Security, WebSocket, Redis configurations

 📡 API Endpoints (Key)
 🔐 Auth
 Base-URL
*localhost:8080

* POST /public/sign-up
* GET /public/login

🚗 Ride

* POST /ride/add → Create ride
* GET /SearchRides/search → Find rides by location
* GET /ride/history/driver
* GET /ride/history/passenger -> history of user as a passenger 

📍 Booking

* POST /booking/{rideId}
* POST /booking/{rideId}/{bbokingId}/CONFIRMED
* POST /booking/{rideId}/{bookingId}/REJECTED

⭐ Reviews

* POST /rating`
* GET /rating/my`
* GET /review/rating/{userId}`

👤 User

* GET /user/profile
* PUT /user/update

🌍 Geospatial Search
* Uses MongoDB 2dsphere index
* Stores:
  * Latitude / Longitude
  * GeoJSON Points
* Enables:
  * Nearby ride discovery
  * Efficient location-based filtering

🔐 Security Highlights

* Stateless authentication using JWT
* Token invalidation using Redis blacklist
* Protected endpoints with role-based access

⚙️ Setup & Installation

 1. Clone the repo
git clone https://github.com/your-username/ridelink-backend.git](https://github.com/CharviBhake/RideLink.git
cd ridelink-backend

 2. Run the application
./mvnw spring-boot:run

🔮 Future Improvements
* Payment integration
* Advanced ride recommendations
* Notification system (email/SMS)
* Driver verification system
* Ride scheduling optimization

 🎯 Purpose

This project was built to demonstrate:

* Real-world backend system design
* Scalable architecture using Spring Boot
* Integration of geospatial queries, caching, and real-time systems

💼 Why this project stands out
* Combines multiple backend concepts:
  * Authentication + Security
  * Geospatial querying
  * Real-time communication
  * Distributed caching
* Designed with production-level thinking

👩‍💻 Author
Charvi Bhake

