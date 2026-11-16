# Security

## OWASP ASVS Level 1 Self-Assessment

*(Checklist will be filled in Phase 12 with evidence from tests and code)*

### V2: Authentication
- [ ] Passwords hashed with BCrypt (work factor ≥10)
- [ ] Session management with secure cookies (HttpOnly, Secure, SameSite)
- [ ] Account enumeration protection on login/register
- [ ] Password policy enforced (minimum length, complexity)
- [ ] Session fixation protection
- [ ] Logout invalidates session server-side

### V4: Access Control
- [ ] Authorization enforced on backend for every protected operation
- [ ] Tenant isolation: organization derived from session, never from client input
- [ ] IDOR protection: every lookup checks organization ownership
- [ ] Role-based permissions enforced
- [ ] Frontend role checks are cosmetic only (real checks on backend)

### V5: Validation, Sanitization and Encoding
- [ ] Input validation with Bean Validation (@Valid, @NotNull, etc.)
- [ ] No SQL injection (parameterized queries only, JPA handles this)
- [ ] No XSS (Spring escapes output by default)
- [ ] File upload validation (size, type, safe names)

### V8: Data Protection
- [ ] No secrets in code, logs, or responses
- [ ] Passwords never logged or returned in API responses
- [ ] Session IDs never logged
- [ ] HTTPS in production (cookies with Secure flag)
- [ ] Sensitive data encrypted at rest (database encryption via Neon)

### V9: Communications
- [ ] HTTPS required in production
- [ ] CORS configured or same-origin proxy
- [ ] CSRF protection (Spring Security default for state-changing operations)

### V11: Business Logic
- [ ] Rate limiting on public endpoints (apply endpoint)
- [ ] Last admin cannot be removed or demoted
- [ ] Role escalation prevented (Recruiter cannot make themselves Admin)

## OWASP Top 10 Mitigation

### A01:2021 - Broken Access Control
**Mitigation:**
- Every protected endpoint checks authentication (Spring Security)
- Organization context derived from session membership
- Repository queries filtered by `organization_id`
- Automated tenant-isolation test suite (Phase 2)

**Evidence:**
- Tenant isolation tests: `(Phase 2 - TBD count)`
- IDOR tests: `(Phase 2 - TBD count)`

### A02:2021 - Cryptographic Failures
**Mitigation:**
- BCrypt for password hashing (Phase 2)
- HTTPS in production (Secure cookies)
- Database encryption at rest (Neon)
- No secrets in git (`.env` in `.gitignore`)

**Evidence:**
- Password hashing test: `(Phase 2)`
- Cookie security config: `(Phase 2)`

### A03:2021 - Injection
**Mitigation:**
- JPA with parameterized queries (no string concatenation)
- Bean Validation prevents malicious input
- Spring Data JPA repository methods (no raw SQL unless necessary)

**Evidence:**
- All database access via JPA repositories
- No `@Query` with concatenated strings

### A04:2021 - Insecure Design
**Mitigation:**
- Threat modeling per phase (see CLAUDE.md security requirements)
- Multi-tenancy designed from Phase 2 (not retrofitted)
- Session-based auth (no token exposure in localStorage)

**Evidence:**
- Design documented in ARCHITECTURE.md
- Security requirements in CLAUDE.md

### A05:2021 - Security Misconfiguration
**Mitigation:**
- `ddl-auto=validate` (schema controlled by Flyway)
- Actuator health endpoint only (no sensitive endpoints exposed)
- Spring Security defaults enabled
- Dependency versions managed by Spring Boot BOM

**Evidence:**
- `application.yml` configuration reviewed
- Actuator limited to `/health` only

### A06:2021 - Vulnerable and Outdated Components
**Mitigation:**
- Spring Boot 4.1.2 (latest stable, November 2026)
- Dependency versions managed by BOM
- GitHub Dependabot enabled (Phase 12)

**Evidence:**
- `pom.xml` uses Spring Boot parent 4.1.2
- CI checks: `(Phase 12 - dependency scan)`

### A07:2021 - Identification and Authentication Failures
**Mitigation:**
- Server-side session management (Spring Session JDBC)
- BCrypt password hashing
- Session fixation protection (Spring Security default)
- No credential stuffing (rate limiting on login)

**Evidence:**
- Authentication tests: `(Phase 2)`
- Rate limiting implementation: `(Phase 2)`

### A08:2021 - Software and Data Integrity Failures
**Mitigation:**
- Flyway checksums prevent migration tampering
- CI runs on every PR (code review required)
- No deserialization of untrusted data

**Evidence:**
- Flyway `flyway_schema_history` tracks checksums
- GitHub Actions workflow: `.github/workflows/backend-ci.yml`

### A09:2021 - Security Logging and Monitoring Failures
**Mitigation:**
- All unexpected errors logged server-side (GlobalExceptionHandler)
- Actuator health endpoint for monitoring
- Structured logging (Logback with JSON format in prod)

**Evidence:**
- `GlobalExceptionHandler` logs exceptions
- Health endpoint: `/actuator/health`

### A10:2021 - Server-Side Request Forgery (SSRF)
**Mitigation:**
- No user-controlled URLs in backend requests
- File uploads validated (size, type) before storage
- Signed URLs for resume downloads (Phase 6)

**Evidence:**
- File upload validation: `(Phase 6)`
- No outbound HTTP client used by user input

## Dependency Scanning

**Tool:** GitHub Dependabot (enabled in Phase 12)

**Last scan:** TBD  
**Known vulnerabilities:** None (Phase 1 - greenfield)

## Known Gaps and Future Work

### Phase 1 Gaps
- No authentication yet (Phase 2)
- No authorization (Phase 2)
- No rate limiting (Phase 2)
- No tenant isolation (Phase 2)

### Future Enhancements (Post-MVP)
- Multi-factor authentication (MFA)
- Audit log for sensitive actions
- Advanced rate limiting (Redis-based)
- Penetration testing
- Security headers (CSP, X-Frame-Options)
- WAF (if deployed behind CDN)

## Security Testing

### Phase 1 Tests
- ✅ Health endpoint returns 200
- ✅ Validation errors return ProblemDetail
- ✅ Not-found errors return ProblemDetail
- ✅ Unexpected errors logged server-side only

**Test count:** 5 integration tests (Testcontainers)

### Phase 2 Tests (Planned)
- [ ] Unauthenticated request returns 401
- [ ] Insufficient role returns 403
- [ ] Cross-organization read fails
- [ ] Cross-organization update fails
- [ ] Cross-organization delete fails
- [ ] Role escalation prevented
- [ ] Last admin cannot be removed

**Target test count:** 15+ tenant-isolation tests

## Incident Response

*(To be defined when production-ready)*

**Contact:** TBD  
**Procedure:** TBD

## Compliance

### Data Privacy
- GDPR considerations: candidate data deletion (Phase 10)
- Data retention policy: TBD
- Privacy policy: required before real candidate data

### Hiring AI Regulations
- AI is advisory, not decision-making
- Human reviews every hiring decision
- Evidence stored for AI assessments (Phase 11)

## Reporting a Vulnerability

*(To be defined when public)*

**Email:** TBD  
**PGP Key:** TBD

For now, report to project maintainer directly.
