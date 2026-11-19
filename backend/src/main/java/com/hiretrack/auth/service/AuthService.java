package com.hiretrack.auth.service;

import com.hiretrack.auth.dto.AuthResponse;
import com.hiretrack.auth.dto.LoginRequest;
import com.hiretrack.auth.dto.RegisterRequest;
import com.hiretrack.common.exception.ConflictException;
import com.hiretrack.membership.entity.Membership;
import com.hiretrack.membership.repository.MembershipRepository;
import com.hiretrack.organization.entity.Organization;
import com.hiretrack.organization.repository.OrganizationRepository;
import com.hiretrack.user.entity.User;
import com.hiretrack.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AuthService: handles user registration and login.
 * 
 * YOUR TASK: Implement register() and login() methods.
 * 
 * KEY CONCEPTS:
 * 1. Email normalization: Always .toLowerCase() before database operations
 * 2. Transaction boundaries: @Transactional ensures register creates all 3 entities or none
 * 3. Password security: Never log passwords, always use PasswordEncoder
 * 4. User enumeration protection: Return generic error for both bad email and bad password
 * 
 * DEPENDENCIES:
 * - Repositories: user, organization, membership
 * - PasswordEncoder: for hashing passwords (BCrypt)
 * - AuthenticationManager: for authenticating after registration
 */
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    
    public AuthService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            MembershipRepository membershipRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }
    
    /**
     * Register a new user and create their organization.
     * 
     * Flow:
     * 1. Normalize email to lowercase
     * 2. Check if email exists → throw ConflictException("Email already registered")
     * 3. Hash password with passwordEncoder.encode()
     * 4. Create User entity and save
     * 5. Create Organization entity and save
     * 6. Create Membership entity (link user to org with ADMIN role) and save
     * 7. Authenticate user with authenticationManager (creates session)
     * 8. Load membership to get organization details
     * 9. Return AuthResponse with all user and org details
     * 
     * @Transactional: If any step fails, rollback everything
     * 
     * SECURITY NOTES:
     * - Email is case-insensitive (database index enforces this)
     * - Password is hashed with BCrypt (work factor 12)
     * - Never return or log the password or hash
     * 
     * ERROR HANDLING:
     * - Duplicate email: ConflictException (409)
     * - Validation errors: Already handled by @Valid in controller (400)
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // TODO: Implement registration logic
        // Hint: Start by normalizing email: String email = request.email().toLowerCase();
        // Hint: Check if email exists: if (userRepository.existsByEmail(email)) throw new ConflictException(...)
        // Hint: Hash password: String passwordHash = passwordEncoder.encode(request.password());
        // Hint: Create User, Organization, Membership entities
        // Hint: Authenticate: authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()))
        // Hint: Set authentication in SecurityContext after authenticating
        // Hint: Return AuthResponse with user and org details
        throw new UnsupportedOperationException("Implement AuthService.register()");
    }
    
    /**
     * Login an existing user.
     * 
     * Flow:
     * 1. Normalize email to lowercase
     * 2. Authenticate with authenticationManager (throws BadCredentialsException if invalid)
     * 3. Load user from database
     * 4. Load user's active memberships
     * 5. Select first active membership (Phase 2 simple approach)
     * 6. Return AuthResponse
     * 
     * ERROR HANDLING:
     * - BadCredentialsException: Catch and re-throw with "Invalid credentials" (user enumeration protection)
     * - User not found after authentication: Should never happen (internal error)
     * - No active memberships: Return error or allow (your choice)
     * 
     * SECURITY NOTES:
     * - authenticationManager calls UserDetailsService and PasswordEncoder internally
     * - SecurityContext is populated automatically
     * - Session is created by Spring Security
     */
    public AuthResponse login(LoginRequest request) {
        // TODO: Implement login logic
        // Hint: Normalize email
        // Hint: Try-catch BadCredentialsException and throw new BadCredentialsException("Invalid credentials")
        // Hint: Use authenticationManager.authenticate()
        // Hint: Load user and memberships
        // Hint: Return AuthResponse
        throw new UnsupportedOperationException("Implement AuthService.login()");
    }
}
