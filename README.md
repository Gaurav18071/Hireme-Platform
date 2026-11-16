# HireTrack AI

Multi-tenant recruitment platform (ATS) for companies to manage hiring pipelines.

## Tech Stack

- **Backend:** Spring Boot 4.1.2, Java 21, PostgreSQL, Flyway
- **Testing:** JUnit 5, Mockito, MockMvc, Testcontainers
- **Infrastructure:** Docker, GitHub Actions

## Prerequisites

- JDK 21+
- Maven 3.6.3+
- Docker Desktop
- Git

## Local Setup

1. **Clone the repository:**
   ```powershell
   git clone <repository-url>
   cd hireme
   ```

2. **Start PostgreSQL:**
   ```powershell
   docker compose up -d
   ```

3. **Run the application:**
   ```powershell
   cd backend
   mvnw spring-boot:run
   ```
   
   Or with Maven directly:
   ```powershell
   mvn spring-boot:run
   ```

4. **Run tests:**
   ```powershell
   cd backend
   mvnw verify
   ```

## Environment Variables

For local development, you can hardcode credentials in `application-dev.yml` or set:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:DB_USERNAME="hiretrack"
$env:DB_PASSWORD="localpassword"
```

## API Endpoints (Phase 1)

- `GET /actuator/health` - Health check
- `GET /api/v1/ping` - Example endpoint returning `{"message":"pong","timestamp":"...","version":"1.0.0"}`

## Testing the API

Using PowerShell:

```powershell
# Health check
Invoke-RestMethod -Uri http://localhost:8080/actuator/health

# Ping endpoint
Invoke-RestMethod -Uri http://localhost:8080/api/v1/ping
```

## Project Status

- [x] **Phase 1:** Backend foundation, Flyway, tests, CI
- [ ] **Phase 2:** Users, organizations, authentication, RBAC
- [ ] **Phase 3:** Frontend scaffold (Next.js)
- [ ] **Phase 4:** Jobs (CRUD, publish/close)
- [ ] **Phase 5+:** Candidates, applications, resume storage...

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Security](docs/SECURITY.md)
- [Performance](docs/PERFORMANCE.md)

## Development

### Database Migrations

Migrations are in `backend/src/main/resources/db/migration/`. Flyway runs them automatically on startup.

**Current migrations:**
- `V1__init.sql` - Creates `set_updated_at()` trigger function for auto-updating timestamps

### Running Tests

```powershell
# All tests
cd backend
mvnw verify

# Unit tests only
mvnw test

# Integration tests only
mvnw verify -DskipUTs
```

### Stopping Services

```powershell
# Stop PostgreSQL
docker compose down

# Remove data volume (fresh start)
docker compose down -v
```

## CI/CD

GitHub Actions runs on every push to `main` and every pull request:
- Compiles the code
- Runs unit tests
- Runs integration tests (Testcontainers)

See `.github/workflows/backend-ci.yml` for configuration.

## Troubleshooting

**Docker not running:**
```
Error response from daemon: This error may indicate that the docker daemon is not running.
```
→ Start Docker Desktop and wait for it to be ready.

**Port 5432 already in use:**
```
Bind for 0.0.0.0:5432 failed: port is already allocated.
```
→ Stop other PostgreSQL instances or change the port in `docker-compose.yml`.

**Tests fail with database connection error:**
→ Ensure Docker is running. Testcontainers needs Docker to start PostgreSQL containers.
