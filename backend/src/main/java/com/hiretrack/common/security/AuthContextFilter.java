package com.hiretrack.common.security;

import com.hiretrack.membership.entity.Membership;
import com.hiretrack.membership.repository.MembershipRepository;
import com.hiretrack.user.entity.User;
import com.hiretrack.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * AuthContextFilter: populates AuthContext after authentication.
 * 
 * CRITICAL FILTER for tenant isolation.
 * 
 * Flow:
 * 1. Check if user is authenticated (SecurityContext has Authentication)
 * 2. Load user from database (to get user ID)
 * 3. Load active memberships (to determine organizations)
 * 4. Select current organization (Phase 2: first active membership)
 * 5. Populate AuthContext (userId, organizationId, role)
 * 
 * Why OncePerRequestFilter?
 * - Guarantees the filter runs once per request (even with forwards/includes)
 * - Base class handles common filter boilerplate
 * 
 * Next.js comparison:
 * - Like middleware that extracts session → sets request headers
 * - Spring: extracts SecurityContext → sets AuthContext bean
 * 
 * Security notes:
 * - Organization ID is NEVER read from client input
 * - It's derived from user's membership (server-side data only)
 * - This prevents IDOR attacks (user cannot fake organizationId)
 */
@Component
public class AuthContextFilter extends OncePerRequestFilter {
    
    private final AuthContext authContext;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    
    public AuthContextFilter(
            AuthContext authContext,
            UserRepository userRepository,
            MembershipRepository membershipRepository) {
        this.authContext = authContext;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // Get authentication from SecurityContext (set by Spring Security)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // If user is authenticated, populate AuthContext
        if (authentication != null && authentication.isAuthenticated() 
                && !authentication.getPrincipal().equals("anonymousUser")) {
            
            String email = authentication.getName();  // Email is the username
            
            // Load user from database
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
            
            // Load active memberships
            List<Membership> memberships = membershipRepository.findActiveByUserId(user.getId());
            
            if (!memberships.isEmpty()) {
                // Phase 2: Use first active membership
                // Phase 3+: Read from session or user preferences
                Membership currentMembership = memberships.get(0);
                
                // Populate AuthContext (available to all services in this request)
                authContext.setContext(
                    user.getId(),
                    currentMembership.getOrganization().getId(),
                    currentMembership.getRole().name()
                );
            }
            // If user has no active memberships, authContext remains uninitialized
            // Protected endpoints will fail with 403 or custom error
        }
        
        // Continue filter chain (pass to next filter or controller)
        filterChain.doFilter(request, response);
    }
}
