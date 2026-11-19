package com.hiretrack.membership.entity;

import com.hiretrack.organization.entity.Organization;
import com.hiretrack.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Membership entity: links users to organizations with a role.
 * 
 * Key design:
 * - Many-to-many between User and Organization, with role and status attributes
 * - Unique constraint: (user_id, organization_id) enforced by database
 * - Role is per-organization (user can be ADMIN in one org, RECRUITER in another)
 * - Status allows soft suspension (INACTIVE) without deleting the membership
 * 
 * Database enforces:
 * - CHECK constraint on role (only valid enum values)
 * - CHECK constraint on status
 * - Foreign keys to users and organizations (CASCADE on delete)
 */
@Entity
@Table(
    name = "memberships",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "organization_id"})
)
public class Membership {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status status = Status.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    protected Membership() {
    }
    
    public Membership(User user, Organization organization, Role role) {
        this.user = user;
        this.organization = organization;
        this.role = role;
        this.status = Status.ACTIVE;
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Organization getOrganization() {
        return organization;
    }
    
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Role enum: matches database CHECK constraint.
     * Maps to permission matrix in CLAUDE.md.
     */
    public enum Role {
        ADMIN,           // Full organization management
        RECRUITER,       // Create jobs, manage candidates
        HIRING_MANAGER,  // View own jobs, manage candidates for own jobs
        INTERVIEWER      // Submit feedback for assigned interviews
    }
    
    /**
     * Status enum: ACTIVE or INACTIVE (soft suspension).
     */
    public enum Status {
        ACTIVE,
        INACTIVE
    }
}
