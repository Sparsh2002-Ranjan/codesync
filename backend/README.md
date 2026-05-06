# CodeSync Backend

Full-stack Spring Boot backend for the CodeSync Online Code Collaboration Platform.

## Architecture

Single Spring Boot application with 9 service packages:

| Service | Package | Base URL |
|---|---|---|
| Auth/User Service | `com.codesync.auth` | `/api/v1/auth` |
| Project Service | `com.codesync.project` | `/api/v1/projects` |
| File/Editor Service | `com.codesync.file` | `/api/v1/files` |
| Collab Session Service | `com.codesync.collab` | `/api/v1/sessions` |
| Code Execution Service | `com.codesync.execution` | `/api/v1/executions` |
| Version/Snapshot Service | `com.codesync.version` | `/api/v1/versions` |
| Comment/Review Service | `com.codesync.comment` | `/api/v1/comments` |
| Notification Service | `com.codesync.notification` | `/api/v1/notifications` |
| Admin Controller (MVC) | `com.codesync.web` | `/api/v1/admin` |

---

## Prerequisites

- Java 21+
- Maven 3.9+
- MySQL 8.0+

---

## Setup Steps

### Step 1 — Create MySQL Database

Open MySQL command line client and run:

```sql
CREATE DATABASE codesync_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Or run the full schema file:
```bash
mysql -u root -p < src/main/resources/codesync_db.sql
```

### Step 2 — Configure Database Password

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 3 — Build and Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/codesync-backend-1.0.0.jar
```

---

## Default Accounts (auto-seeded on first run)

| Role | Email | Password |
|---|---|---|
| Admin | admin@codesync.io | admin123 |
| Developer | dev@codesync.io | dev123 |

---

## API Documentation (Swagger UI)

Once running, open:
```
http://localhost:8080/swagger-ui.html
```

All 9 services are documented there with try-it-out support.

---

## WebSocket Endpoints

Connect via SockJS at: `ws://localhost:8080/ws`

| Topic | Description |
|---|---|
| `/topic/session/{id}/cursors` | Live cursor positions |
| `/topic/session/{id}/edits` | Code edit deltas |
| `/topic/session/{id}/events` | Join/leave/kick events |
| `/user/{userId}/queue/notifications` | Private notifications |

Send to:
| Destination | Description |
|---|---|
| `/app/session/{id}/cursor` | Send cursor update |
| `/app/session/{id}/edit` | Send code edit |

---

## Key API Endpoints

### Auth
```
POST /api/v1/auth/register       Register new user
POST /api/v1/auth/login          Login → JWT token
GET  /api/v1/auth/profile/{id}   Get user profile
PUT  /api/v1/auth/profile/{id}   Update profile
PUT  /api/v1/auth/password/{id}  Change password
GET  /api/v1/auth/search?query=  Search users
```

### Projects
```
POST   /api/v1/projects                    Create project
GET    /api/v1/projects/public             Browse public projects
GET    /api/v1/projects/owner/{ownerId}    My projects
GET    /api/v1/projects/search?query=      Search projects
POST   /api/v1/projects/{id}/fork          Fork project
POST   /api/v1/projects/{id}/star          Star project
POST   /api/v1/projects/{id}/members       Add member
DELETE /api/v1/projects/{id}/members/{uid} Remove member
```

### Editor / Files
```
POST /api/v1/files                              Create file
GET  /api/v1/files/project/{projectId}/tree    File tree
PUT  /api/v1/files/{fileId}/content            Update content
PUT  /api/v1/files/{fileId}/rename             Rename
PUT  /api/v1/files/{fileId}/move               Move
DELETE /api/v1/files/{fileId}                  Soft delete
POST /api/v1/files/{fileId}/restore            Restore
GET  /api/v1/files/project/{id}/search?query=  Search in project
```

### Live Sessions
```
POST /api/v1/sessions                           Create session
POST /api/v1/sessions/{id}/join?userId=         Join session
POST /api/v1/sessions/{id}/leave?userId=        Leave session
POST /api/v1/sessions/{id}/end                  End session
POST /api/v1/sessions/{id}/kick/{participantId} Kick participant
GET  /api/v1/sessions/{id}/participants         Get active participants
```

### Code Execution
```
POST /api/v1/executions/submit         Submit code to sandbox
GET  /api/v1/executions/{jobId}        Poll job status
POST /api/v1/executions/{jobId}/cancel Cancel job
GET  /api/v1/executions/languages      Supported languages
```

### Version Control
```
POST /api/v1/versions/snapshots                Create snapshot
GET  /api/v1/versions/history/{fileId}         File history
GET  /api/v1/versions/snapshots/diff?s1=&s2=   Diff two snapshots
POST /api/v1/versions/snapshots/{id}/restore   Restore snapshot
POST /api/v1/versions/branches?name=           Create branch
PUT  /api/v1/versions/snapshots/{id}/tag?tag=  Tag snapshot
```

### Comments
```
POST /api/v1/comments                          Add comment
GET  /api/v1/comments/file/{fileId}            Get file comments
GET  /api/v1/comments/file/{fileId}/line/{n}   Line-specific comments
GET  /api/v1/comments/{id}/replies             Get replies
PUT  /api/v1/comments/{id}/resolve             Resolve comment
PUT  /api/v1/comments/{id}/unresolve           Unresolve comment
```

### Notifications
```
POST /api/v1/notifications                          Send notification
GET  /api/v1/notifications/recipient/{userId}       Get my notifications
GET  /api/v1/notifications/recipient/{id}/unread-count  Unread badge count
PUT  /api/v1/notifications/{id}/read               Mark read
PUT  /api/v1/notifications/recipient/{id}/read-all Mark all read
POST /api/v1/notifications/bulk                    Bulk send (Admin)
```

### Admin
```
GET  /api/v1/admin/dashboard             Platform analytics
GET  /api/v1/admin/users                 All users
PUT  /api/v1/admin/users/{id}/suspend    Suspend user
PUT  /api/v1/admin/users/{id}/reactivate Reactivate user
GET  /api/v1/admin/sessions              Active sessions
POST /api/v1/admin/sessions/{id}/terminate Terminate session
POST /api/v1/admin/notifications/broadcast Platform broadcast
```

---

## Connect Angular Frontend

In your Angular `environment.ts`:
```typescript
export const environment = {
  apiUrl: 'http://localhost:8080/api/v1',
  wsUrl:  'http://localhost:8080/ws'
};
```

Then replace mock service calls in `auth.service.ts`, `project.service.ts` etc.
with `HttpClient` calls to these endpoints.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2, Spring MVC |
| Security | Spring Security + JWT (jjwt 0.12) |
| Database | MySQL 8 via Spring Data JPA / Hibernate |
| Real-time | Spring WebSocket + STOMP + SockJS |
| Diff Algorithm | java-diff-utils (Myers algorithm) |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Build | Maven 3.9 |
| Java Version | Java 21 |
