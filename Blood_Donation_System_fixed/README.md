# 🩸 Blood Donation System

A full-stack Blood Donation & Emergency Locator System.

- **Backend**: Spring Boot 3 + MongoDB + JWT Auth + Twilio SMS
- **Admin Frontend**: React 19 + Vite (dark dashboard)

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MongoDB | 6+ (running locally) |

---

## Setup & Run

### 1. Start MongoDB

```bash
mongod
# or with brew: brew services start mongodb-community
```

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

The server starts at **http://localhost:8080**

**Default admin credentials** (auto-created on first run):
- Email: `admin@blooddonation.com`
- Password: `Admin@123`

### 3. Admin Frontend

```bash
cd admin
npm install
npm run dev
```

Opens at **http://localhost:3000**

---

## Environment / Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/blooddonation

# JWT (change in production!)
jwt.secret=BloodDonationSystemSecretKey2024_ChangeThisInProduction_VeryLongAndSecure
jwt.expiration=86400000

# Twilio SMS (optional - leave as-is to skip SMS in dev)
twilio.account.sid=YOUR_TWILIO_ACCOUNT_SID
twilio.auth.token=YOUR_TWILIO_AUTH_TOKEN
twilio.phone.number=+1XXXXXXXXXX
```

> SMS is automatically skipped if Twilio credentials start with `YOUR_`.

---

## API Endpoints

### Auth
| Method | Path | Access |
|--------|------|--------|
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/register/admin` | Public |

### Donors
| Method | Path | Access |
|--------|------|--------|
| POST | `/api/donors/register` | Public |
| GET | `/api/donors/nearby?lat=&lng=&radius=` | Public |
| GET | `/api/donors/search?city=&bloodGroup=` | Public |
| GET | `/api/donors` | Admin |
| GET | `/api/donors/{id}` | Auth |
| PUT | `/api/donors/{id}` | Auth |

### Blood Requests
| Method | Path | Access |
|--------|------|--------|
| POST | `/api/requests/create` | Public |
| GET | `/api/requests/{id}` | Public |
| GET | `/api/requests` | Admin |
| PATCH | `/api/requests/{id}/status` | Admin |
| DELETE | `/api/requests/{id}` | Admin |
| GET | `/api/requests/stats` | Admin |

---

## Bugs Fixed

1. **`JwtUtils.java`** — Added missing `extractUsername()` and `validateToken()` methods called by `JwtAuthFilter` (caused `NoSuchMethodError` at runtime)
2. **`BloodDonationApplication.java`** — Added `@EnableMongoAuditing` so `@CreatedDate`/`@LastModifiedDate` fields auto-populate
3. **`AppConfig.java`** — Removed duplicate `@EnableMongoAuditing` (was declared but also needed on the main class)
4. **`Donor.java`** — Removed unused `@Collation` import (compile warning / confusion)
5. **`backend/backend/`** — Removed duplicate nested folder with a conflicting `application.properties` that disabled security
6. **Admin frontend** — Replaced the default Vite starter template with a real admin dashboard (login, stats, request management, donor list)
7. **`vite.config.js`** — Added API proxy to backend so CORS issues don't occur in development

