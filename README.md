# Smart Notification Management System

A full-stack notification management application with retry mechanism and asynchronous processing using RabbitMQ.

---

## 📋 Technology Stack

**Backend:** Java 21, Spring Boot 4.1.0, JPA/Hibernate, RabbitMQ 4.0.9 (Erlang 27.3.4.13), MySQL  
**Frontend:** React.js 18, Axios, React Router

---

## 🚀 Setup Instructions

### Prerequisites
- Java 21
- Gradle 9.5.1
- Node.js 16+
- MySQL 8.0+
- RabbitMQ 4.0.9

### 1. Database Setup
```bash
mysql -u root -p
CREATE DATABASE NotificationDB;
```

### 2. RabbitMQ Setup
```bash
# Using Docker (Recommended)
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# Or install locally from https://www.rabbitmq.com/download.html
# Access RabbitMQ Management: http://localhost:15672 (guest/guest)
```

### 3. Backend Setup
```bash
cd notification-backend

# Update application.properties with your credentials:
# - spring.datasource.username
# - spring.datasource.password
# - spring.rabbitmq.host (if not localhost)

# Build the project
./gradlew clean build

# Run the Spring Boot application
./gradlew bootRun
```
Backend runs on: **http://localhost:8181**

### 4. Frontend Setup
```bash
cd notification-frontend
npm install
npm start
```
Frontend runs on: **http://localhost:3000**

---

## 🏗 Project Architecture

### Backend Layered Architecture

```
┌─────────────────────────────────────────┐
│          CONTROLLER LAYER               │
│  - NotificationController               │
│  - DashboardController                  │
│  (REST API Endpoints)                   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          SERVICE LAYER                  │
│  - NotificationService                  │
│  - NotificationProcessorService         │
│  - DashboardService                     │
│  (Business Logic & Validation)          │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│       VALIDATOR LAYER                   │
│  - MessageValidator                     │
│  (Custom Business Rules)                │
└─────────────────────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│       REPOSITORY LAYER                  │
│  - NotificationRepository               │
│  (Data Access with JPA)                 │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          DATABASE                       │
│  MySQL - notifications table            │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│       MESSAGE QUEUE (RabbitMQ)          │
│  - notification-queue                   │
│  (Async Processing)                     │
└─────────────────────────────────────────┘
```

### Frontend Component Architecture

```
App (Routing & Navigation)
├── Dashboard (Statistics & Charts)
├── NotificationForm (Create Notifications)
├── NotificationList (View & Filter)
│   └── Pagination (Page Navigation)
└── NotificationService (API Integration)
```

### Data Flow

1. **Create Notification:**
   ```
   User → Frontend → REST API → Service (Validate) 
   → Repository (Save) → RabbitMQ Queue → Async Processor 
   → Update Status → Database
   ```

2. **Retry Notification:**
   ```
   User → Frontend → REST API → Service (Validate Retry Rules) 
   → Update Status → RabbitMQ Queue → Async Processor
   ```

3. **View Notifications:**
   ```
   User → Frontend → REST API → Service → Repository 
   → Database (Paginated Query) → Response
   ```

---

## 🗄 Database Schema

### Notifications Table

```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    schedule_time DATETIME NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    last_retry_time DATETIME,
    error_message VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

#### Column Descriptions

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key (auto-increment) |
| `user_id` | BIGINT | User identifier |
| `type` | VARCHAR(20) | Notification type: EMAIL, SMS, PUSH |
| `status` | VARCHAR(20) | Current status: PENDING, SENT, FAILED, RETRYING |
| `message` | TEXT | Notification content |
| `schedule_time` | DATETIME | Scheduled delivery time |
| `retry_count` | INT | Number of retry attempts (default: 0) |
| `last_retry_time` | DATETIME | Timestamp of last retry attempt |
| `error_message` | VARCHAR(500) | Error details if notification failed |
| `created_at` | DATETIME | Record creation timestamp |
| `updated_at` | DATETIME | Last update timestamp |

#### Indexes
No additional indexes created (Can be used for future optimization).

---
## 📝 Assumptions

* **RabbitMQ**

  * Running and reachable at configured host/port
  * Queues auto-created by Spring AMQP

* **Processing**
  * Notifications are async via RabbitMQ
  * `scheduleTime` stored but not used for delay
  * Email/SMS sending is mocked (no real providers)

* **Time**
  * Server timezone used (no conversions)

* **Scaling**
  * Single instance only (no distributed locks)

* **Security**
  * No auth/authz; all APIs are public
  * `userId` passed in request

* **Data**
  * Notifications stored indefinitely (no cleanup)

* **Failures**
  * Failed messages stay in `FAILED` state
  * Retry is manual via API

---
## 🔧 Important Implementation Details (Short)

### 1. Business Rules

* **Retry Logic:** Only FAILED notifications can retry (max 3 attempts, 2 min gap).
* **Duplicate Check:** Blocks same userId + type + message within 5 minutes.
* **Validation:** Rejects messages with any word repeated > 3 times.
* **Failure Simulation:** 30% random failure rate via config.

### 2. RabbitMQ Flow

* Create notification → SAVE (PENDING) → send ID to queue → consumer processes.
* Consumer simulates delay, then updates status to SENT/FAILED in DB.
* Uses single consumer with manual acknowledgment.

### 3. API Endpoints

* `POST /api/notifications` → create
* `GET /api/notifications` → list (filters + pagination)
* `POST /api/notifications/{id}/retry` → retry failed
* `GET /api/dashboard` → stats

### 4. Frontend

* Form: validation + reset + success/error UI.
* List: filters, pagination, retry button.
* Dashboard: counts + breakdown + auto-refresh.

### 5. Exception Handling

* Custom errors: duplicate, invalid message, retry not allowed, not found.
* Global handler returns clean error responses.

### 6. Config

* Retry: max 3 attempts, 2 min interval.
* Duplicate window: 5 min.
* Failure rate: 0.3.
* CORS enabled for frontend.

### 7. Logging

* Logs all major actions + errors with notification IDs for tracking.


---

## 🧪 Testing the Application

### Quick Test Steps:

1. **Start Services:**
   ```bash
   # Terminal 1: RabbitMQ
   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   
   # Terminal 2: Backend
   cd notification-backend && ./gradlew bootRun

   
   # Terminal 3: Frontend
   cd notification-frontend && npm start
   ```

2. **Create Notification:**
   - Go to http://localhost:3000/create
   - Fill form and submit
   - Check RabbitMQ Management UI for queue activity

3. **View Notifications:**
   - Go to http://localhost:3000/notifications
   - Apply filters
   - Test pagination

4. **Test Retry:**
   - Wait for a failed notification (30% chance)
   - Click "Retry" button
   - Verify retry count increments

5. **View Dashboard:**
   - Go to http://localhost:3000
   - Check statistics

---


**Assessment Submission**  
Full Stack Developer - Smart Notification Management System  
Technology: Java 21 + Spring Boot + React.js + RabbitMQ + MySQL
