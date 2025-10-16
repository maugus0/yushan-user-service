# Yushan User Service

> 📚 **User Service for Yushan Webnovel Platform.** - A gamified web novel reading platform that transforms reading into an engaging, social experience.

# Yushan Platform - User Service Setup Guide

## Architecture Overview

```
┌─────────────────────────────┐
│   Eureka Service Registry   │
│       localhost:8761        │
└──────────────┬──────────────┘
               │
┌──────────────┴──────────────┐
│   Service Registration &     │
│      Discovery Layer         │
└──────────────┬──────────────┘
               │
    ┌──────────┴──────────┬───────────────┬──────────┬──────────┐
    │                     │               │          │          │
    ▼                     ▼               ▼          ▼          ▼
┌────────┐          ┌─────────┐  ┌────────────┐ ┌──────────┐ ┌──────────┐
│  User  │          │ Content │  │ Engagement │ │Gamifica- │ │Analytics │
│Service │          │ Service │  │  Service   │ │  tion    │ │ Service  │
│ :8081  │◄────────►│  :8082  │  │   :8084    │ │ Service  │ │  :8083   │
└────────┘          └─────────┘  └────────────┘ │  :8085   │ └──────────┘
    │                     │              │       └──────────┘      │
    └─────────────────────┴──────────────┴───────────────────────┘
                    Inter-service Communication
                      (via Feign Clients)
```

---
## Prerequisites

Before setting up the User Service, ensure you have:
1. **Java 21** installed
2. **Maven 3.8+** or use the included Maven wrapper
3. **Eureka Service Registry** running

---
## Step 1: Start Eureka Service Registry

**IMPORTANT**: The Eureka Service Registry must be running before starting any microservice.

```bash
# Clone the service registry repository
git clone https://github.com/maugus0/yushan-platform-service-registry
cd yushan-platform-service-registry

# Option 1: Run with Docker (Recommended)
docker-compose up -d

# Option 2: Run locally
./mvnw spring-boot:run
```

### Verify Eureka is Running

- Open: http://localhost:8761
- You should see the Eureka dashboard

---

## Step 2: Clone the User Service Repository

```bash
git clone https://github.com/maugus0/yushan-user-service.git
cd yushan-user-service

# Option 1: Run with Docker (Recommended)
docker-compose up -d

# Option 2: Run locally (in case you run locally, then you need postgre 15 to be running beforehand)
./mvnw spring-boot:run
```

---

## Expected Output

### Console Logs (Success)

```
2024-10-16 10:30:15 - Starting UserServiceApplication
2024-10-16 10:30:18 - Tomcat started on port(s): 8081 (http)
2024-10-16 10:30:20 - DiscoveryClient_USER-SERVICE/user-service:8081 - registration status: 204
2024-10-16 10:30:20 - Started UserServiceApplication in 8.5 seconds
```

### Eureka Dashboard

```
Instances currently registered with Eureka:
✅ USER-SERVICE - 1 instance(s)
   Instance ID: user-service:8081
   Status: UP (1)
```

---

## API Endpoints

### Health Check
- **GET** `/api/v1/health` - Service health status

### User Management
- **POST** `/api/v1/users/register` - Register new user
- **GET** `/api/v1/users/{userId}` - Get user details
- **PUT** `/api/v1/users/{userId}` - Update user profile
- **GET** `/api/v1/users/{userId}/reading-preferences` - Get reading preferences

---

## Next Steps

Once this basic setup is working:
1. ✅ Create database entities (User, UserProfile, ReadingPreferences, etc.)
2. ✅ Set up Flyway migrations
3. ✅ Create repositories and services
4. ✅ Implement API endpoints
5. ✅ Add Feign clients for inter-service communication
6. ✅ Set up Redis caching for session management
7. ✅ Add security and authentication

---
## Troubleshooting

**Problem: Service won't register with Eureka**
- Ensure Eureka is running: `docker ps`
- Check logs: Look for "DiscoveryClient" messages
- Verify defaultZone URL is correct

**Problem: Port 8081 already in use**
- Find process: `lsof -i :8081` (Mac/Linux) or `netstat -ano | findstr :8081` (Windows)
- Kill process or change port in application.yml

**Problem: Database connection fails**
- Verify PostgreSQL is running: `docker ps | grep yushan-postgres`
- Check database credentials in application.yml
- Test connection: `psql -h localhost -U yushan_user -d yushan_user`

**Problem: Build fails**
- Ensure Java 21 is installed: `java -version`
- Check Maven: `./mvnw -version`
- Clean and rebuild: `./mvnw clean install -U`
