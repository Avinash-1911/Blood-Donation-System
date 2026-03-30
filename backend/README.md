# 🩸 Blood Donation & Emergency Locator — Spring Boot Backend

Complete REST API backend for the Blood Donation System. Built with **Spring Boot 3**, **MongoDB**, and **Twilio SMS**.

---

## 🗂 Project Structure

```
src/main/java/com/blooddonation/
├── BloodDonationApplication.java       ← Entry point
├── config/
│   ├── AppConfig.java                  ← Async + auditing
│   ├── DataInitializer.java            ← Seeds default admin
│   ├── JwtAuthFilter.java              ← JWT request filter
│   ├── JwtUtils.java                   ← JWT generation/validation
│   └── SecurityConfig.java             ← CORS + Security rules
├── controller/
│   ├── AuthController.java             ← /api/auth/**
│   ├── BloodRequestController.java     ← /api/requests/**
│   └── DonorController.java            ← /api/donors/**
├── dto/
│   ├── ApiResponse.java                ← Generic wrapper
│   ├── AuthDTO.java                    ← Login/register payloads
│   ├── BloodRequestDTO.java            ← Request payloads
│   └── DonorDTO.java                   ← Donor payloads
├── exception/
│   ├── GlobalExceptionHandler.java     ← Centralized error handling
│   └── ResourceNotFoundException.java
├── model/
│   ├── BloodRequest.java               ← MongoDB document
│   ├── DonationEvent.java              ← Donation history
│   ├── Donor.java                      ← Donor document + blood group logic
│   └── User.java                       ← Admin/user accounts
├── repository/
│   ├── BloodRequestRepository.java
│   ├── DonationEventRepository.java
│   ├── DonorRepository.java
│   └── UserRepository.java
└── service/
    ├── AuthService.java                ← Login, JWT issuance
    ├── BloodRequestService.java        ← Request creation + auto-matching
    ├── DonorService.java               ← Geo-spatial donor search
    ├── SmsService.java                 ← Twilio SMS alerts
    └── UserDetailsServiceImpl.java     ← Spring Security integration
```

---

## ✅ Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.8+ |
| MongoDB | 6.0+ (local or Atlas) |
| Twilio account | (optional — SMS logs to console otherwise) |

---

## 🚀 Setup & Run

### 1. Clone / place backend folder

```bash
cd blood-donation-backend
```

### 2. Configure MongoDB

Edit `src/main/resources/application.properties`:

```properties
# Local MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/blooddonation

# OR MongoDB Atlas
spring.data.mongodb.uri=mongodb+srv://<user>:<password>@cluster.mongodb.net/blooddonation
```

> ⚠️ MongoDB must have a **2dsphere geo index** created automatically by Spring Data.

### 3. Configure Twilio SMS (optional)

```properties
twilio.account.sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
twilio.auth.token=your_auth_token
twilio.phone.number=+1XXXXXXXXXX
```

If you skip this, SMS messages are **logged to the console** instead — perfect for development.

### 4. Run the application

```bash
mvn spring-boot:run
```

Server starts at **http://localhost:8080**

**Default admin account** is created automatically:
- Email: `admin@blooddonation.com`
- Password: `Admin@123`

---

## 🔌 API Reference

### Authentication

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/login` | Public | Login (returns JWT) |
| POST | `/api/auth/register/admin` | Public | Register admin user |

#### Login example
```json
POST /api/auth/login
{
  "email": "admin@blooddonation.com",
  "password": "Admin@123"
}
```

---

### Donors

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/donors/register` | Public | Register as donor |
| GET | `/api/donors/nearby` | Public | Find donors by GPS + blood group |
| GET | `/api/donors/search` | Public | Search by city / blood group |
| GET | `/api/donors/me` | Donor | Get own profile |
| GET | `/api/donors/{id}` | Auth | Get donor by ID |
| PUT | `/api/donors/{id}` | Auth | Update profile |
| PATCH | `/api/donors/{id}/availability` | Auth | Toggle availability |
| GET | `/api/donors` | Admin | List all donors |

#### Register donor
```json
POST /api/donors/register
{
  "name": "Ravi Kumar",
  "email": "ravi@example.com",
  "password": "secret123",
  "phone": "+919876543210",
  "bloodGroup": "O_POSITIVE",
  "age": 28,
  "gender": "Male",
  "city": "Nagpur",
  "state": "Maharashtra",
  "longitude": 79.0882,
  "latitude": 21.1458
}
```

#### Find nearby donors (geo search)
```
GET /api/donors/nearby?lat=21.1458&lng=79.0882&radius=30&bloodGroup=O_POSITIVE&compatible=true
```

**Blood Group Enum Values:** `A_POSITIVE`, `A_NEGATIVE`, `B_POSITIVE`, `B_NEGATIVE`, `AB_POSITIVE`, `AB_NEGATIVE`, `O_POSITIVE`, `O_NEGATIVE`

---

### Blood Requests

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/requests/create` | Public | Submit emergency request → auto-notifies donors |
| GET | `/api/requests/{id}` | Public | Track request status |
| POST | `/api/requests/{id}/accept` | Donor | Donor accepts request |
| GET | `/api/requests` | Admin | All requests |
| GET | `/api/requests/filter?status=PENDING` | Admin | Filter by status |
| GET | `/api/requests/stats` | Admin | Dashboard stats |
| PATCH | `/api/requests/{id}/status` | Admin | Update status |
| DELETE | `/api/requests/{id}` | Admin | Delete request |

#### Submit blood request
```json
POST /api/requests/create
{
  "requesterName": "Priya Sharma",
  "requesterPhone": "+919876543211",
  "requesterEmail": "priya@example.com",
  "bloodGroupNeeded": "B_POSITIVE",
  "unitsNeeded": 2,
  "urgency": "CRITICAL",
  "patientName": "Arjun Sharma",
  "hospital": "AIIMS Nagpur",
  "hospitalAddress": "Medical Square, Nagpur",
  "city": "Nagpur",
  "state": "Maharashtra",
  "longitude": 79.0882,
  "latitude": 21.1458,
  "searchRadiusKm": 30
}
```

**What happens:** System finds all available compatible donors within 30km using MongoDB `$nearSphere` query, sends them SMS alerts via Twilio, and sends a confirmation to the requester.

---

## 🗺 How Geo-Spatial Search Works

1. Frontend captures user's GPS coordinates (or uses OpenStreetMap geocoding)
2. POST to `/api/requests/create` with `longitude`, `latitude`, `searchRadiusKm`
3. Backend runs MongoDB `$nearSphere` query against donors' `GeoJsonPoint` locations
4. Results sorted by distance (nearest first)
5. Donors receive SMS via Twilio with request details
6. System falls back to city-based search if no geo results found

---

## 🩸 Blood Compatibility Chart

| Recipient | Can receive from |
|-----------|-----------------|
| O- | O- only |
| O+ | O+, O- |
| A- | A-, O- |
| A+ | A+, A-, O+, O- |
| B- | B-, O- |
| B+ | B+, B-, O+, O- |
| AB- | A-, B-, AB-, O- |
| AB+ | All groups (universal recipient) |

---

## 🔐 JWT Authentication

Include the token in all authenticated requests:

```
Authorization: Bearer <your-jwt-token>
```

Token expires in 24 hours (configurable via `jwt.expiration`).

---

## 🌐 CORS

By default, allows requests from `http://localhost:3000` (React dev server).

Change in `application.properties`:
```properties
cors.allowed.origins=http://localhost:3000,https://your-production-domain.com
```

---

## 📦 Build for production

```bash
mvn clean package -DskipTests
java -jar target/blood-donation-system-1.0.0.jar
```
