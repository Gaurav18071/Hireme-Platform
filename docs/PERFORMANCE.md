# Performance

## Load Testing

*(Results will be added in Phase 12 with k6 scripts)*

### Test Environment
- **Application:** TBD (CPU, RAM, JVM settings)
- **Database:** TBD (Neon tier, connection pool size)
- **Dataset:** TBD (number of organizations, users, jobs, candidates)
- **Tool:** k6

### Baseline Results

**Target load:**
- Concurrent users: TBD
- Requests/sec: TBD
- Test duration: TBD

**Latency:**
- p50: TBD ms
- p95: TBD ms
- p99: TBD ms

**Error rate:** TBD%

### Endpoints Tested
1. `GET /api/v1/ping` - Health check baseline
2. `POST /api/v1/auth/login` - Authentication
3. `GET /api/v1/jobs` - Job listing (read-heavy)
4. `GET /api/v1/me` - Current user

## Optimizations Applied

### Phase 1
- None yet (baseline infrastructure)

### Future Optimizations (When Needed)

**Database:**
1. Indexes on `organization_id` (Phase 2)
2. Indexes on common filters (status, created_at)
3. Materialized views for dashboard queries
4. Connection pooling tuning (HikariCP)

**Application:**
1. Spring Cache with Redis (job listings)
2. Lazy loading disabled (`open-in-view: false`)
3. DTO projections instead of full entities
4. Pagination on all list endpoints

**Infrastructure:**
1. CDN for static assets (Phase 3 - frontend)
2. Database read replicas (if read-heavy)
3. Horizontal scaling (stateless app + session in DB)

## Current Metrics

### Phase 1 Baseline
- **Startup time:** TBD (measure with Actuator)
- **Memory usage:** TBD (JVM heap)
- **Database connections:** Default HikariCP pool

### JVM Configuration

**Development:**
```
-Xms512m -Xmx1g
```

**Production (TBD):**
```
-Xms1g -Xmx2g
```

## Database Query Performance

### Index Strategy

**Phase 1:**
- None (no tenant data yet)

**Phase 2+:**
- Index on every `organization_id` column
- Composite index on `(organization_id, created_at)` for time-based queries
- Unique indexes for constraints

**Example:**
```sql
CREATE INDEX idx_jobs_organization_id ON jobs(organization_id);
CREATE INDEX idx_jobs_org_created ON jobs(organization_id, created_at);
```

### N+1 Query Prevention

**Strategy:**
- `open-in-view: false` forces eager loading decisions upfront
- Use `@EntityGraph` or `JOIN FETCH` for related entities
- DTO projections when possible (avoid loading full entities)

**Example:**
```java
// Bad (N+1)
List<Job> jobs = jobRepository.findAll();
jobs.forEach(job -> job.getOrganization().getName()); // N queries

// Good (single query)
@Query("SELECT j FROM Job j JOIN FETCH j.organization WHERE j.organizationId = :orgId")
List<Job> findAllByOrganizationWithOrg(@Param("orgId") UUID orgId);
```

## Monitoring

### Metrics (Phase 12)
- Request rate (requests/sec)
- Response time (p50, p95, p99)
- Error rate (%)
- Database connection pool usage
- JVM heap usage

### Tools
- Spring Boot Actuator `/actuator/metrics`
- Micrometer (metrics collection)
- Prometheus (metrics storage)
- Grafana (visualization)

### Alerts (Production)
- Response time p99 > 2 seconds
- Error rate > 1%
- Database connection pool exhausted
- Memory usage > 80%

## Known Performance Bottlenecks

### Phase 1
- None identified yet

### Future Considerations
1. **Resume storage (Phase 6):** Large file uploads may block requests
   - Solution: Async processing, signed upload URLs
2. **AI analysis (Phase 11):** LLM calls are slow (seconds)
   - Solution: Background job queue
3. **Dashboard queries (Phase 8):** Complex aggregations
   - Solution: Materialized views, caching

## Capacity Planning

### Target Scale (MVP)
- Organizations: 100
- Users per org: 10-50
- Jobs per org: 20
- Applications per job: 100

### Estimated Load
- Peak concurrent users: 500
- Requests/sec: 100
- Database size: < 10 GB

### Infrastructure Sizing (TBD)
- **Application:** 2 CPU, 4 GB RAM
- **Database:** Neon tier TBD
- **Storage:** R2 for resumes

## Load Test Scripts

*(k6 scripts will be added in `loadtests/` directory)*

### Example Script Structure
```javascript
// loadtests/login-load.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 }, // Ramp up
    { duration: '5m', target: 100 }, // Stay at peak
    { duration: '2m', target: 0 },   // Ramp down
  ],
};

export default function () {
  let res = http.post('http://localhost:8080/api/v1/auth/login', {
    email: 'test@example.com',
    password: 'password123',
  });
  
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
  
  sleep(1);
}
```

## Benchmark History

*(Table will be populated as optimizations are made)*

| Date | Version | Change | Req/sec | p95 (ms) | p99 (ms) |
|------|---------|--------|---------|----------|----------|
| TBD  | 1.0     | Baseline | TBD | TBD | TBD |
