package com.hiretrack.note.entity;

import com.hiretrack.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Note entity: TEST RESOURCE for tenant isolation (Phase 2 only).
 * 
 * This entity exists ONLY to prove tenant isolation works.
 * It will be removed or replaced once Jobs are implemented in Phase 4.
 * 
 * Key design:
 * - Every note has organization_id (tenant-scoped)
 * - Repository queries always filter by organization_id
 * - IDOR protection: attempting to access another org's note returns 404
 */
@Entity
@Table(name = "notes")
public class Note {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
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
    
    protected Note() {
    }
    
    public Note(Organization organization, String title, String content) {
        this.organization = organization;
        this.title = title;
        this.content = content;
    }
    
    public UUID getId() {
        return id;
    }
    
    public Organization getOrganization() {
        return organization;
    }
    
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
