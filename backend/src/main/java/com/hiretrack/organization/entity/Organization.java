package com.hiretrack.organization.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Organization entity (tenant).
 * 
 * Each organization is a separate company using HireTrack.
 * Jobs, candidates, and applications belong to an organization.
 * 
 * Phase 2: Simple table with just name.
 * Later phases: Add metadata (industry, size, settings).
 */
@Entity
@Table(name = "organizations")
public class Organization {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 255)
    private String name;
    
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
    
    protected Organization() {
    }
    
    public Organization(String name) {
        this.name = name;
    }
    
    public UUID getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
