package com.hiretrack.common.security;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

/**
 * AuthContext: holds the authenticated user's context for the current request.
 * 
 * CRITICAL DESIGN:
 * - Request-scoped bean: new instance per HTTP request
 * - Populated by AuthContextFilter after authentication
 * - Injected into services (never passed as method parameters)
 * - Contains userId, organizationId, role (derived from session, NOT from client)
 * 
 * Why request-scoped?
 * - Singleton would leak data between requests
 * - Prototype would require manual lifecycle management
 * - Request-scoped: Spring creates one per request, destroys after response
 * 
 * Similar to Next.js pattern:
 *   middleware sets headers → API route reads headers
 *   
 * Spring pattern:
 *   filter sets AuthContext → services inject AuthContext
 * 
 * Why ScopedProxyMode.TARGET_CLASS?
 * Services are singletons but need request-scoped AuthContext.
 * Spring creates a proxy that resolves to the current request's instance.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthContext {
    
    private UUID userId;
    private UUID organizationId;
    private String role;
    
    /**
     * Set authentication context (called by AuthContextFilter).
     */
    public void setContext(UUID userId, UUID organizationId, String role) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.role = role;
    }
    
    /**
     * Get current user's ID.
     * @throws IllegalStateException if called on an unauthenticated request
     */
    public UUID getUserId() {
        if (userId == null) {
            throw new IllegalStateException("AuthContext not initialized (user not authenticated)");
        }
        return userId;
    }
    
    /**
     * Get current organization's ID (tenant isolation).
     * @throws IllegalStateException if called on an unauthenticated request
     */
    public UUID getOrganizationId() {
        if (organizationId == null) {
            throw new IllegalStateException("AuthContext not initialized (user not authenticated)");
        }
        return organizationId;
    }
    
    /**
     * Get current user's role in the current organization.
     * @throws IllegalStateException if called on an unauthenticated request
     */
    public String getRole() {
        if (role == null) {
            throw new IllegalStateException("AuthContext not initialized (user not authenticated)");
        }
        return role;
    }
    
    /**
     * Check if context is initialized (user is authenticated).
     */
    public boolean isAuthenticated() {
        return userId != null;
    }
}
