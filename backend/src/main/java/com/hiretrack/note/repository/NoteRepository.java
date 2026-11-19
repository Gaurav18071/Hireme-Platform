package com.hiretrack.note.repository;

import com.hiretrack.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * NoteRepository: TEST RESOURCE for tenant isolation (Phase 2 only).
 * 
 * CRITICAL DESIGN:
 * Every query method explicitly filters by organizationId.
 * This is the EXPLICIT FILTERING approach (vs Hibernate Filter "magic").
 * 
 * Why explicit?
 * - Clear: Every call visibly enforces tenant isolation
 * - Auditable: Easy to grep for "organizationId" in service calls
 * - Safe: No hidden "magic" that can be accidentally disabled
 * 
 * Trade-off: Verbose. If this becomes too repetitive in Phase 4+, 
 * consider Hibernate @Filter as an alternative.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    
    /**
     * Find all notes in an organization.
     * 
     * @param organizationId current organization ID (from AuthContext)
     * @return list of notes (empty if none)
     */
    List<Note> findByOrganizationId(UUID organizationId);
    
    /**
     * Find a note by ID and organization (IDOR protection).
     * Returns empty if note exists but belongs to a different org.
     * 
     * @param id note ID
     * @param organizationId current organization ID
     * @return Optional<Note> or empty if not found or wrong org
     */
    Optional<Note> findByIdAndOrganizationId(UUID id, UUID organizationId);
    
    /**
     * Delete a note by ID and organization (IDOR protection).
     * No-op if note doesn't exist or belongs to another org.
     * 
     * @param id note ID
     * @param organizationId current organization ID
     */
    void deleteByIdAndOrganizationId(UUID id, UUID organizationId);
}
