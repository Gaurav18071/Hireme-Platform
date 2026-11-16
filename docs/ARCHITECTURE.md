# Architecture

## Overview

HireTrack is a **modular monolith** (not microservices). One Spring Boot application, one PostgreSQL database, organized into domain modules.

## Why Modular Monolith?

**Advantages:**
- Simpler deployment (one JAR, one database)
- Easier debugging and testing
- Lower operational overhead (no service mesh, no distributed tracing complexity)
- Faster development iteration
- Can extract modules to services later if needed (but YAGNI applies)

**Trade-offs:**
- Single point of failure (mitigated with health checks, quick restarts)
- Vertical scaling only initially (but sufficient for target load)

## Package Structure

```
com.hiretrack
├── HiretrackApplication.java       Main entry point (@SpringBootApplication)
│
├── common                          Cross-cutting concerns
│   ├── config                      Spring configuration beans
│   └── exception                   Global exception handling
│
├── health                          Health check endpoints (Phase 1)
│   ├── controller                  REST endpoints
│   ├── service                     Business logic
│   └── dto                         Data transfer objects
│
├── user                            User management (Phase 2)
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   └── dto
│
├── organization                    Multi-tenancy (Phase 2)
│   └── (same structure)
│
├── job                             Job postings (Phase 4)
│   └── (same structure)
│
└── candidate                       Candidates and applications (Phase 5)
    └── (same structure)
```

**Key principles:**
- Each domain package is self-contained (controller → service → repository → entity)
- `common` for shared infrastructure (exception handling, security config)
- No circular dependencies between domain modules
- DTOs at boundaries (never expose JPA entities via API)

## Layered Architecture

```
┌─────────────────────────────────────────┐
│          REST Controllers               │  @RestController
│  - HTTP request/response                │  - Validate input (@Valid)
│  - Serialize JSON                       │  - Return DTOs
│  - HTTP status codes                    │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│            Services                     │  @Service
│  - Business logic                       │  - @Transactional
│  - Orchestration                        │  - Permission checks
│  - Validation (business rules)          │  - No HTTP knowledge
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Repositories                   │  JpaRepository
│  - Database queries                     │  - CRUD operations
│  - Simple finder methods                │  - Custom queries
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          PostgreSQL Database            │
└─────────────────────────────────────────┘
```

**Responsibilities:**

| Layer | Does | Doesn't |
|-------|------|---------|
| **Controller** | Receive HTTP, validate DTO, call service, return DTO/status | Business logic, database access |
| **Service** | Business rules, transactions, orchestration | HTTP concerns, JSON |
| **Repository** | Database queries (find, save, delete) | Business logic |

## Database Strategy

### Schema Ownership
- **Flyway migrations** are the single source of truth
- `hibernate.ddl-auto=validate` ensures JPA entities match schema
- Never edit applied migrations; add new ones
- Migrations are sequential and immutable

### Conventions (from CLAUDE.md)
- `snake_case` for tables and columns
- UUID primary keys (`id UUID PRIMARY KEY DEFAULT gen_random_uuid()`)
- `created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `set_updated_at()` trigger on every table with `updated_at`

### Tenant Isolation
- Every tenant-owned table has `organization_id UUID NOT NULL` with foreign key
- Index on `organization_id` for query performance
- Uniqueness scoped to organization: `UNIQUE (organization_id, email)`
- Never global uniqueness (except user email before organization assignment)

### Example (Phase 2+):
```sql
CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_jobs_organization_id ON jobs(organization_id);

CREATE TRIGGER trigger_set_updated_at
    BEFORE UPDATE ON jobs
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
```

## API Conventions

### REST Principles
- Base path: `/api/v1`
- HTTP verbs: GET (read), POST (create), PUT (update), DELETE (delete)
- Resource-oriented URLs: `/api/v1/jobs`, `/api/v1/jobs/{id}`

### Request/Response
- **Request:** JSON with DTOs, validated with Bean Validation (`@Valid`, `@NotNull`, etc.)
- **Response:** JSON DTOs (never JPA entities)
- **Errors:** RFC 7807 ProblemDetail format

### Status Codes
- `200 OK` - Success (GET, PUT)
- `201 Created` - Resource created (POST)
- `204 No Content` - Success with no body (DELETE)
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Authenticated but insufficient permissions
- `404 Not Found` - Resource doesn't exist
- `500 Internal Server Error` - Unexpected server error

### Error Response Shape (ProblemDetail)
```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Invalid request content",
  "instance": "/api/v1/jobs",
  "errors": [
    {"field": "title", "message": "must not be blank"}
  ]
}
```

## Testing Strategy

### Test Types

| Type | Speed | Scope | When to Use |
|------|-------|-------|-------------|
| **Unit** | Milliseconds | Single class | Pure logic, no Spring |
| **MockMvc** | ~1-2 sec | Web layer | Controllers, validation, JSON |
| **Testcontainers** | ~5-10 sec | Full stack | End-to-end, database, Flyway |

### Unit Tests
- `@ExtendWith(MockitoExtension.class)`
- Mock dependencies with `@Mock`, inject with `@InjectMocks`
- Test business logic in isolation

### MockMvc Tests
- `@WebMvcTest(ControllerClass.class)` - loads web layer only
- Mock services with `@MockitoBean`
- Test HTTP behavior: status codes, JSON shape, validation

### Testcontainers Integration Tests
- `@SpringBootTest` - full application context
- `@Testcontainers` + `@Container` - real PostgreSQL in Docker
- `@DynamicPropertySource` - connect Spring to container
- Test everything: Flyway migrations, database queries, HTTP

## Security Architecture (Phase 2+)

### Authentication
- Server-side sessions with Spring Session JDBC
- BCrypt password hashing
- HttpOnly, Secure, SameSite cookies
- No JWT or tokens in localStorage

### Authorization
- Role-based access control (RBAC)
- Roles: Admin, Recruiter, Hiring Manager, Interviewer
- Permissions enforced in service layer
- Every request derives organization from authenticated user's membership

### Tenant Isolation
- Organization context derived from session, never from client input
- Every query filtered by `organization_id`
- Automated test suite proves cross-tenant access fails

## Deployment

### Local Development
- Docker Compose for PostgreSQL
- `mvn spring-boot:run` for Spring Boot
- Profile: `dev` (debug logging, show SQL)

### Staging/Production (Future)
- Neon PostgreSQL (pooled connection)
- Flyway with direct connection for migrations
- Profile: `prod` (minimal logging, no SQL output)
- Secrets from environment variables

## Future Considerations

### When to Extract Services
- If a domain becomes a performance bottleneck
- If a team boundary forms around a domain
- If scaling requirements differ significantly

**Not now.** Modular monolith is correct for current scale.

### Horizontal Scaling
- Stateless application (session in database, not memory)
- Load balancer distributes requests
- Database connection pooling (HikariCP)

### Caching (When Needed)
- Spring Cache with Redis
- Cache job listings (read-heavy)
- Invalidate on updates

## Technology Decisions

### Why Spring Boot?
- Mature ecosystem for Java backend
- Excellent tooling (Spring Boot DevTools, Actuator)
- Strong community and documentation
- Enterprise-ready (used by major companies)

### Why PostgreSQL?
- ACID guarantees for financial/hiring data
- JSON support (for flexible metadata)
- Full-text search (for job/candidate search later)
- Reliable, well-understood

### Why Flyway?
- Version control for database schema
- Team-safe (no conflicts on migrations)
- Auditable (every change is a file in git)
- Rollback-friendly (write explicit down migrations)

### Why Testcontainers?
- Tests against real PostgreSQL (not in-memory H2)
- Catches PostgreSQL-specific issues
- Same database in CI and local
- No manual setup (Docker does it)

## References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Testcontainers Documentation](https://testcontainers.com/)
