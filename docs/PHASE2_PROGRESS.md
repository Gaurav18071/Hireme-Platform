# Phase 2 Implementation Progress

## ✅ Completed (by Claude)

### 1. Database Migrations (Flyway)
- ✅ V2__create_users.sql
- ✅ V3__create_organizations.sql  
- ✅ V4__create_memberships.sql
- ✅ V5__create_spring_session.sql
- ✅ V6__create_notes_test_resource.sql

**Key features:**
- UUID primary keys with `gen_random_uuid()`
- Case-insensitive unique email index
- CHECK constraints on role and status enums
- Indexes for tenant isolation (organization_id)
- Spring Session tables for server-side sessions

### 2. JPA Entities
- ✅ User (global, not tenant-scoped)
- ✅ Organization (tenant)
- ✅ Membership (user-org-role link)
- ✅ Note (test resource for isolation)

**Key features:**
- `@PrePersist` and `@PreUpdate` for timestamps
- Protected no-arg constructors (JPA requirement)
- Enums for Role and Status

### 3. Repositories
- ✅ UserRepository (findByEmail, existsByEmail)
- ✅ OrganizationRepository (basic CRUD)
- ✅ MembershipRepository (findActiveByUserId, countActiveAdmins)
- ✅ NoteRepository (explicit organization_id filtering)

**Key features:**
- Custom query methods for tenant isolation
- JPQL for complex queries
- Explicit filtering (not Hibernate Filter magic)

### 4. DTOs (Request/Response)
- ✅ AuthResponse, LoginRequest, RegisterRequest
- ✅ UserResponse, OrganizationResponse
- ✅ MemberResponse, ChangeRoleRequest
- ✅ NoteRequest, NoteResponse

**Key features:**
- Java records (concise, immutable)
- Bean Validation annotations (@NotBlank, @Email, @Size, @Pattern)

### 5. Security Infrastructure
- ✅ SecurityConfig (SecurityFilterChain, PasswordEncoder, AuthenticationManager)
- ✅ CustomUserDetailsService (loads user by email)
- ✅ AuthContext (request-scoped bean for userId, organizationId, role)
- ✅ AuthContextFilter (populates AuthContext after authentication)

**Key features:**
- BCrypt work factor 12
- Session fixation protection (changeSessionId)
- CSRF protection (CookieCsrfTokenRepository)
- CORS for Next.js frontend (Phase 3)
- Explicit public endpoints (register, login, health)

### 6. Exception Handling
- ✅ NotFoundException (404 for tenant isolation)
- ✅ ForbiddenException (403 for role checks)
- ✅ ConflictException (409 for duplicates)
- ✅ GlobalExceptionHandler updated

### 7. Configuration
- ✅ Spring Security and Spring Session dependencies in pom.xml
- ✅ application.yml (session settings, cookie config)

---

## 🚧 Next Steps (for You to Implement)

### Services (Business Logic Layer)

You'll implement these services with the guidance below.

#### AuthService
**Location:** `com.hiretrack.auth.service.AuthService`

**Purpose:** Handle registration and login.

**Key methods:**
1. `register(RegisterRequest)` → AuthResponse
   - Normalize email to lowercase
   - Check if email exists (throw ConflictException)
   - Hash password with BCrypt
   - Create User, Organization, and Membership (all in one @Transactional)
   - Authenticate user (call AuthenticationManager)
   - Return AuthResponse

2. `login(LoginRequest)` → AuthResponse
   - Normalize email
   - Authenticate with AuthenticationManager (throws BadCredentialsException)
   - Load user's memberships
   - Return AuthResponse

**Dependencies:**
- UserRepository, OrganizationRepository, MembershipRepository
- PasswordEncoder, AuthenticationManager
- @Transactional on register method

**Security notes:**
- Always normalize email before database operations
- Never log passwords
- Return "Invalid credentials" for both bad email and bad password (user enumeration protection)

#### MembershipService
**Location:** `com.hiretrack.membership.service.MembershipService`

**Purpose:** Manage organization members and roles.

**Key methods:**
1. `listMembers()` → List<MemberResponse>
   - Check: current user is ADMIN (throw ForbiddenException)
   - Query: findActiveByOrganizationId(authContext.getOrganizationId())
   - Map to MemberResponse DTOs

2. `changeRole(UUID membershipId, ChangeRoleRequest)` → MemberResponse
   - Check: current user is ADMIN
   - Load membership: findByIdAndOrganizationId (404 if not found)
   - Check last-admin rule: if changing ADMIN to other role, count remaining admins
   - Update role, save
   - Return MemberResponse

**Dependencies:**
- MembershipRepository, AuthContext
- @Transactional on changeRole

**Business rules:**
- Only ADMIN can list members or change roles
- Cannot change last admin's role (throw ConflictException)

#### NoteService (Test Resource)
**Location:** `com.hiretrack.note.service.NoteService`

**Purpose:** CRUD for notes (proves tenant isolation).

**Key methods:**
1. `create(NoteRequest)` → NoteResponse
   - Create Note with authContext.getOrganizationId()
   - Save and return

2. `list()` → List<NoteResponse>
   - Query: findByOrganizationId(authContext.getOrganizationId())

3. `get(UUID id)` → NoteResponse
   - Query: findByIdAndOrganizationId (404 if wrong org)

4. `update(UUID id, NoteRequest)` → NoteResponse
   - Load note (404 if wrong org)
   - Update fields, save

5. `delete(UUID id)` → void
   - Delete: deleteByIdAndOrganizationId

**Dependencies:**
- NoteRepository, OrganizationRepository, AuthContext
- @Transactional

#### UserService
**Location:** `com.hiretrack.user.service.UserService`

**Purpose:** Current user info.

**Key method:**
1. `getCurrentUser()` → UserResponse
   - Query: findById(authContext.getUserId())
   - Map to UserResponse

**Dependencies:**
- UserRepository, AuthContext

---

### Controllers (HTTP Layer)

#### AuthController
**Location:** `com.hiretrack.auth.controller.AuthController`

**Endpoints:**
- POST /api/v1/auth/register (@Valid RegisterRequest) → 201, AuthResponse
- POST /api/v1/auth/login (@Valid LoginRequest) → 200, AuthResponse
- POST /api/v1/auth/logout → 204 (handled by Spring Security, just document)

**Notes:**
- Use @RestController, @RequestMapping("/api/v1/auth")
- @Valid triggers Bean Validation
- Login: catch BadCredentialsException → return 401 with "Invalid credentials"
- Register creates session automatically (AuthenticationManager)

#### UserController
**Location:** `com.hiretrack.user.controller.UserController`

**Endpoints:**
- GET /api/v1/me → 200, UserResponse

**Notes:**
- Requires authentication (401 if not authenticated)
- Calls userService.getCurrentUser()

#### OrganizationController
**Location:** `com.hiretrack.organization.controller.OrganizationController`

**Endpoints:**
- GET /api/v1/organizations/current → 200, OrganizationResponse

**Notes:**
- Load organization by authContext.getOrganizationId()
- Include current user's role in response

#### MembershipController
**Location:** `com.hiretrack.membership.controller.MembershipController`

**Endpoints:**
- GET /api/v1/organizations/current/members → 200, List<MemberResponse>
- PUT /api/v1/memberships/{id}/role (@Valid ChangeRoleRequest) → 200, MemberResponse

**Notes:**
- Both endpoints require ADMIN role (403 if not)
- Service layer enforces role check

#### NoteController (Test Resource)
**Location:** `com.hiretrack.note.controller.NoteController`

**Endpoints:**
- POST /api/v1/notes (@Valid NoteRequest) → 201, NoteResponse
- GET /api/v1/notes → 200, List<NoteResponse>
- GET /api/v1/notes/{id} → 200, NoteResponse
- PUT /api/v1/notes/{id} (@Valid NoteRequest) → 200, NoteResponse
- DELETE /api/v1/notes/{id} → 204

---

## Implementation Strategy

### Step 1: Services First
Write and test services in this order:
1. AuthService (register and login)
2. UserService (getCurrentUser)
3. MembershipService (listMembers, changeRole)
4. NoteService (CRUD)

### Step 2: Controllers
Write controllers that delegate to services.
Use @RestController, @RequestMapping, @PostMapping, @GetMapping, etc.

### Step 3: Manual Testing (PowerShell)
Test endpoints with Invoke-RestMethod:
```powershell
# Register
$body = @{
    email = "alice@example.com"
    password = "password123456"
    fullName = "Alice Admin"
    organizationName = "Acme Corp"
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:8080/api/v1/auth/register `
    -Method Post `
    -Body $body `
    -ContentType "application/json" `
    -SessionVariable session

# Use session in subsequent requests
Invoke-RestMethod -Uri http://localhost:8080/api/v1/me `
    -WebSession $session
```

### Step 4: Integration Tests (Testcontainers)
Write tests for:
- Registration → login → /me flow
- Tenant isolation (Alice cannot read Bob's notes)
- Role enforcement (Recruiter cannot list members)
- Last-admin rule

---

## Key Concepts to Understand Before Writing Services

### 1. @Transactional
Place on service methods that modify data.
Spring starts a transaction, commits on success, rolls back on exception.

**Example:**
```java
@Transactional
public AuthResponse register(RegisterRequest request) {
    // All repository.save() calls are in one transaction
    // If any fails, all are rolled back
}
```

### 2. Dependency Injection (Constructor Injection)
Services receive dependencies through constructor.
Spring autowires them automatically.

**Example:**
```java
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
}
```

### 3. Mapping Entities to DTOs
Never return entities from controllers (Phase 1 rule: DTOs at boundaries).

**Example:**
```java
User user = userRepository.findById(id).orElseThrow();
return new UserResponse(user.getId(), user.getEmail(), user.getFullName());
```

### 4. AuthContext Usage
Inject AuthContext into services.
It's request-scoped (new instance per request).

**Example:**
```java
@Service
public class NoteService {
    private final AuthContext authContext;
    
    public List<Note> list() {
        UUID orgId = authContext.getOrganizationId();  // Derived from session
        return noteRepository.findByOrganizationId(orgId);
    }
}
```

### 5. Exception Handling
Throw custom exceptions; GlobalExceptionHandler maps them to HTTP status codes.

**Example:**
```java
if (!authContext.getRole().equals("ADMIN")) {
    throw new ForbiddenException("Admin role required");
}
```

---

## Next: Write AuthService

Start with AuthService.register().
Show me your implementation, and I'll review it for:
- Security issues (password handling, email normalization)
- Transaction boundaries (@Transactional)
- Error handling (ConflictException for duplicate email)
- Correct use of PasswordEncoder and AuthenticationManager

After you write AuthService, I'll review it and we'll move to the next service.

Remember:
- I'm here to review and guide (learning mode)
- Security mistakes are expensive; ask before committing
- Test each service as you write it
