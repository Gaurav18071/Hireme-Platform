# CLAUDE.md: HireTrack AI (greenfield build)

Persistent project rules. Read this at the start of every session.
Items marked **[PROPOSED]** are defaults I (the owner) have not finalized. Do not treat them as approved until the marker is removed.

## 1. What this project is

HireTrack AI is a multi-tenant recruitment platform (ATS). Companies publish jobs, candidates apply through a public careers site, recruiters move applications through a hiring pipeline, interviewers submit feedback, and AI assists (never replaces) human screening.

This is a **new build from scratch**. The earlier Next.js + Prisma prototype is archived as `hiretrack-ai-v1` and is reference only. Do not copy its auth, schema, or data model. UI ideas and components may be reused by copying them deliberately.

## 2. Stack

- **Backend:** Java 21, Spring Boot (latest supported release; verify at spring.io in Phase 1 and record it here), Spring Web, Spring Security, Spring Data JPA/Hibernate, Bean Validation, Maven, Flyway, OpenAPI (springdoc), Actuator
- **Frontend:** Next.js (App Router), React, TypeScript, Tailwind CSS, shadcn/ui, React Hook Form, Zod. No Prisma and no Auth.js: the frontend only talks to the Spring API.
- **Database:** PostgreSQL. Local development and tests use Docker (Docker Compose, Testcontainers). Neon is used for staging and production.
- **Storage:** Cloudflare R2 behind a storage interface (later phase)
- **Background work:** PostgreSQL-backed job table with `FOR UPDATE SKIP LOCKED` (later phase)
- **AI:** provider behind an interface (last phase)

## 3. Repository layout

```
hiretrack/
├── backend/        Spring Boot modular monolith (Maven)
├── frontend/       Next.js app
├── docs/           architecture, security, performance, decisions
├── loadtests/      k6 scripts
└── .github/workflows/
```

Keep the repo outside OneDrive or any sync folder (for example `C:\dev\hiretrack`).

## 4. Hard rules

1. No microservices. One deployable Spring Boot app with domain modules.
2. Flyway owns the schema from the first migration. No `ddl-auto=update`; use `validate`. Never edit a migration that has been applied; add a new one.
3. Never run destructive database commands against staging or production without explicit approval and a verified backup.
4. Never hardcode secrets, commit `.env` files, or log signed URLs, tokens, passwords, or resume content.
5. Controllers are thin, services hold business rules, repositories only persist. Never expose JPA entities through the API; use DTOs.
6. No `any` in TypeScript. No unnecessary dependencies; say why when adding one.
7. Never claim tests pass without running them. Never fabricate results.
8. Do not start a phase or implement anything until I approve the proposal.

## 5. Data model rules (apply from the first migration)

- Tables and columns in `snake_case`; primary keys are UUIDs; `created_at` and `updated_at` on every table.
- Every tenant-owned table has `organization_id NOT NULL` with a foreign key and an index.
- Uniqueness is scoped to the organization (for example, candidate email is unique per `(organization_id, email)`, never globally).
- Enumerated values use `VARCHAR` with `CHECK` constraints (or a native enum), mirrored by Java enums.
- Range checks (ratings, scores) are enforced in the database as well as in code.
- Application stage changes write an immutable row to a status-history table in the same transaction.
- Composite foreign keys or service checks must prevent cross-organization relationships (for example, an application linking a job and a candidate from different organizations).

## 6. Security requirements (always apply)

- Authorization is enforced on the backend for every protected operation. Frontend role checks are cosmetic only.
- Tenant isolation: derive the organization from the authenticated membership, never from client input. Every ID-based lookup also checks organization ownership (IDOR protection).
- Roles are per organization membership, not a global role on the user.
- Authentication: **[PROPOSED]** Spring Security with server-side sessions (Spring Session JDBC on Postgres) in an httpOnly, Secure, SameSite cookie, BCrypt password hashing, and Next.js proxying `/api/v1/*` to Spring so the browser sees one origin. No tokens in localStorage.
- Resumes are private; serve them only through short-lived signed URLs after an authorization check. Validate file size, type, and name; generate safe object keys.
- Rate-limit and de-duplicate the public apply endpoint.
- AI output is advisory. A human makes every hiring decision. Store the evidence behind every AI assessment.

## 7. Permission matrix [PROPOSED]

| Action | Admin | Recruiter | Hiring Manager | Interviewer |
|---|---|---|---|---|
| Manage organization and members | Yes | No | No | No |
| Create / edit / publish jobs | Yes | Yes | No | No |
| Manage candidates | Yes | Yes | View only, own jobs | No |
| View applications | Yes | Yes | Own jobs | Assigned interviews only |
| Move application stages | Yes | Yes | Own jobs | No |
| Schedule interviews | Yes | Yes | Own jobs | No |
| Submit interview feedback | Yes | No | Own jobs | Assigned only |

## 8. Phase order [PROPOSED]

0. Setup: tools, repo, short Java fundamentals block
1. Backend foundation: Spring Boot, Docker Compose Postgres, Flyway, health, error handling, tests, CI
2. Users, organizations, membership, authentication, RBAC, tenant-isolation tests
3. Frontend scaffold: login, session, API client, dashboard shell (vertical slice with Phase 2)
4. Jobs (CRUD, publish/close, public listing API)
5. Candidates and applications with status history (resume metadata only)
6. Resume storage (R2, signed URLs, validation)
7. Public careers site and apply flow
8. Dashboard data
9. Interviews and scorecards
10. Background jobs and notifications
11. AI analysis
12. Production hardening, load tests, security scan, launch checklist

**MVP:** phases 0 to 8. Phases 9 to 11 are release two.

## 9. Learning mode (primary goal: I am learning Java and Spring Boot)

I am a TypeScript/Next.js developer. The point is for **me** to learn Java, Spring Boot, JPA, and Spring Security well enough to explain every line in an interview.

- Explain each new concept before using it, and map it to what I know (JPA entity vs Prisma model, Spring bean vs module singleton, Bean Validation vs Zod, filter chain vs Next.js middleware).
- Work in small steps. Do not generate whole modules. Give the design, the file list, and key skeletons, and let me write the implementation.
- Review my code: bugs, non-idiomatic Java, security issues, better approaches, and why.
- Write complete code only when I ask, and then explain it line by line.
- Point out common Java mistakes when relevant (lazy loading and N+1, `equals`/`hashCode` on entities, exposing entities, transaction boundaries, null handling).
- End each phase with 3 to 5 interview questions and wait for my answers.
- If I am stuck: hint, then bigger hint, then solution.

## 10. Production readiness (target: real customers)

A phase is not done until its checklist items are met.

- Separate dev, staging, and production environments with their own databases and secrets. CI (GitHub Actions) runs build, unit tests, and Testcontainers integration tests on every pull request; deploy only from a green main.
- Database changes use expand/contract so a deploy can be rolled back.
- Dockerfile, health and readiness endpoints, graceful shutdown, sensible HikariCP pool size and timeouts (respect Neon limits and cold starts). Production hosting must not sleep.
- Backups: Neon point-in-time recovery, with a restore tested once before launch.
- Security headers, strict CORS or same-origin proxy, dependency scanning in CI, secrets only from the host's secret store, audit log for sensitive actions.
- A tenant-isolation test suite that must pass in CI.
- Structured logs with correlation IDs and no PII; metrics (Micrometer), error tracking, uptime monitoring.
- Indexes on `organization_id` and common filters; no N+1 queries on list endpoints.
- Privacy policy and terms before real candidate data; retention rules and a working candidate-deletion process (including files); data processing agreements for third-party processors; check hiring-AI regulations before enabling AI screening for customers.
- Launch with a pilot of 1 to 3 friendly organizations.

## 11. Showcase evidence (the project must prove my skills on a resume)

Every claim about this project must be measured, reproducible, and documented. Never invent or round up a number.

- `docs/PERFORMANCE.md`: k6 scripts, environment, dataset size, real results (concurrent users, requests/sec, p95/p99, error rate), before/after for each optimization.
- `docs/SECURITY.md`: OWASP ASVS Level 1 self-assessment, OWASP Top 10 mapping, scan results, and known gaps.
- `docs/ARCHITECTURE.md` and short decision records.
- README: live demo link, demo organization credentials (seeded only in dev/demo environments), screenshots, links to the documents above.
- CI shows test count and coverage. Record the number of tenant-isolation tests.
- Update the evidence documents in the same pull request as the work. Use wording like "self-assessed against OWASP ASVS Level 1", never "bank-level" or "enterprise-grade".

## 12. Workflow for each phase

Understand, then propose (design, files, database changes, API, security notes), then wait for my approval, then implement in small steps, then verify with real command output, then report.

Report: files created / modified, decisions and why, database and API changes, security notes, tests executed (real results), known issues, suggested commit message, and interview questions. Stop after each approved step.

## 13. API conventions

- Base path `/api/v1`; REST conventions; request and response DTOs; Bean Validation
- Pagination, filtering, and sorting on list endpoints
- One global exception handler returning a consistent error shape (use Spring's `ProblemDetail`), with no internal details leaked
- OpenAPI documentation
- Tests: JUnit 5, Mockito, Spring Boot Test, Testcontainers; frontend with Playwright and TypeScript type checks