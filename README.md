# Yushan User Service

> 👤 **User Service for Yushan Webnovel Platform.** - Manages user accounts, profiles, authentication, preferences, and user-related operations for the gamified web novel reading platform.

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
│Service │◄────────►│ Service │◄─┤  Service   │ │  tion    │ │ Service  │
│ :8081  │          │  :8082  │  │   :8084    │ │ Service  │ │  :8083   │
└───┬────┘          └─────────┘  └────────────┘ │  :8085   │ └──────────┘
    │                     │              │       └──────────┘      │
    └─────────────────────┴──────────────┴───────────────────────┘
    │                 Inter-service Communication
    │                   (via Feign Clients)
    ▼
┌───────────────┐
│  Auth & User  │
│  Management   │
│   JWT/OAuth   │
└───────────────┘
```

---
## Prerequisites

Before setting up the User Service, ensure you have:
1. **Java 21** installed
2. **Maven 3.8+** or use the included Maven wrapper
3. **Eureka Service Registry** running
4. **PostgreSQL 15+** (for user data storage)
5. **Redis** (for session management and caching)

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

# Option 2: Run locally (requires PostgreSQL 15 and Redis to be running beforehand)
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

### Authentication
- **POST** `/api/v1/auth/register` - Register new user account
- **POST** `/api/v1/auth/login` - User login
- **POST** `/api/v1/auth/logout` - User logout
- **POST** `/api/v1/auth/refresh` - Refresh access token
- **POST** `/api/v1/auth/forgot-password` - Initiate password reset
- **POST** `/api/v1/auth/reset-password` - Reset password with token
- **POST** `/api/v1/auth/verify-email` - Verify email address
- **POST** `/api/v1/auth/resend-verification` - Resend verification email

### User Management
- **GET** `/api/v1/users/{userId}` - Get user profile
- **PUT** `/api/v1/users/{userId}` - Update user profile
- **DELETE** `/api/v1/users/{userId}` - Delete user account
- **GET** `/api/v1/users/{userId}/stats` - Get user statistics
- **PUT** `/api/v1/users/{userId}/avatar` - Update avatar
- **PUT** `/api/v1/users/{userId}/password` - Change password

### User Preferences
- **GET** `/api/v1/users/{userId}/preferences` - Get user preferences
- **PUT** `/api/v1/users/{userId}/preferences` - Update preferences
- **GET** `/api/v1/users/{userId}/reading-preferences` - Get reading preferences
- **PUT** `/api/v1/users/{userId}/reading-preferences` - Update reading preferences

### User Settings
- **GET** `/api/v1/users/{userId}/settings` - Get user settings
- **PUT** `/api/v1/users/{userId}/settings` - Update settings
- **PUT** `/api/v1/users/{userId}/settings/notifications` - Update notification settings
- **PUT** `/api/v1/users/{userId}/settings/privacy` - Update privacy settings

### User Search
- **GET** `/api/v1/users/search` - Search users by username/email
- **GET** `/api/v1/users/{userId}/public-profile` - Get public profile

### User Sessions
- **GET** `/api/v1/users/{userId}/sessions` - Get active sessions
- **DELETE** `/api/v1/users/{userId}/sessions/{sessionId}` - Revoke session
- **DELETE** `/api/v1/users/{userId}/sessions` - Revoke all sessions

### Admin Endpoints
- **GET** `/api/v1/admin/users` - List all users (paginated)
- **PUT** `/api/v1/admin/users/{userId}/ban` - Ban user
- **PUT** `/api/v1/admin/users/{userId}/unban` - Unban user
- **PUT** `/api/v1/admin/users/{userId}/role` - Update user role

---

## Key Features

### 🔐 Authentication & Authorization
- JWT-based authentication
- OAuth2 integration (Google, Facebook)
- Email verification
- Password reset functionality
- Multi-factor authentication (MFA)
- Session management with Redis
- Role-based access control (RBAC)

### 👤 User Profile Management
- Comprehensive user profiles
- Avatar upload and management
- Bio and personal information
- Social links
- Profile visibility settings
- User statistics display

### ⚙️ User Preferences
- Reading preferences (font, theme, layout)
- Genre preferences
- Language preferences
- Content filters
- Notification preferences
- Privacy settings

### 📱 Session Management
- Multiple device support
- Active session tracking
- Session revocation
- Remember me functionality
- Session timeout configuration

### 🔍 User Discovery
- Username search
- Email lookup (admin only)
- Public profile viewing
- User verification badges
- User reputation system

### 🛡️ Security Features
- Password encryption (BCrypt)
- Account lockout after failed attempts
- Rate limiting on authentication endpoints
- IP-based access logging
- Suspicious activity detection
- GDPR compliance features

---

## Database Schema

The User Service uses the following key entities:

- **User** - Core user account information
- **UserProfile** - Extended profile information
- **UserPreferences** - User preferences and settings
- **ReadingPreferences** - Reading-specific preferences
- **UserSession** - Active user sessions
- **UserRole** - User roles and permissions
- **PasswordResetToken** - Password reset tokens
- **EmailVerificationToken** - Email verification tokens
- **UserActivity** - User activity logs
- **BlockedUser** - User blocking relationships

---

## Next Steps

Once this basic setup is working:
1. ✅ Create database entities (User, UserProfile, UserPreferences, etc.)
2. ✅ Set up Flyway migrations
3. ✅ Create repositories and services
4. ✅ Implement JWT authentication
5. ✅ Add Feign clients for inter-service communication
6. ✅ Set up Redis caching for session management
7. ✅ Implement email service for verification
8. ✅ Add OAuth2 providers
9. ✅ Implement MFA
10. ✅ Add security auditing

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

**Problem: Redis connection fails**
- Verify Redis is running: `docker ps | grep redis`
- Check Redis connection: `redis-cli ping`
- Verify Redis host and port in application.yml

**Problem: Build fails**
- Ensure Java 21 is installed: `java -version`
- Check Maven: `./mvnw -version`
- Clean and rebuild: `./mvnw clean install -U`

**Problem: JWT token validation fails**
- Check JWT_SECRET environment variable
- Verify token expiration settings
- Check system clock synchronization
- Review token format in requests

**Problem: Email verification not working**
- Check email service configuration
- Verify SMTP settings
- Review email template configuration
- Check spam folder

---

## Performance Tips
1. **Session Caching**: Use Redis for session storage
2. **Profile Caching**: Cache frequently accessed profiles
3. **Rate Limiting**: Implement rate limits on auth endpoints
4. **Database Indexing**: Index username, email, and user_id columns
5. **Connection Pooling**: Configure appropriate connection pool sizes

---

## Security Best Practices
1. **Password Policy**: Enforce strong password requirements
2. **Rate Limiting**: Prevent brute force attacks
3. **Account Lockout**: Lock accounts after failed attempts
4. **Audit Logging**: Log all authentication events
5. **Token Rotation**: Rotate JWT tokens regularly
6. **HTTPS Only**: Force HTTPS in production
7. **CORS Configuration**: Properly configure CORS policies

---

## Inter-Service Communication
The User Service communicates with:
- **Engagement Service**: User profile data for comments/reviews
- **Gamification Service**: User achievements and levels
- **Analytics Service**: User behavior tracking
- **Content Service**: Author verification and permissions

---

## Monitoring
The User Service exposes metrics through:
- Spring Boot Actuator endpoints (`/actuator/metrics`)
- Custom authentication metrics (login success/failure rates)
- Session count and activity
- Failed login attempts
- API response times

---

## License
This project is part of the Yushan Platform ecosystem.
