package com.hiretrack.membership.repository;

import com.hiretrack.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MembershipRepository: data access for Membership entities.
 * 
 * Critical for tenant isolation:
 * - findActiveByUserId: determines which organizations a user can access
 * - findActiveByOrganizationId: lists all members of an organization
 * - countActiveAdminsByOrganizationId: enforces last-admin rule
 */
@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    
    /**
     * Find all ACTIVE memberships for a user (for organization selection).
     * 
     * @param userId user ID
     * @return list of active memberships (empty if user has none)
     */
    @Query("SELECT m FROM Membership m WHERE m.user.id = :userId AND m.status = 'ACTIVE'")
    List<Membership> findActiveByUserId(@Param("userId") UUID userId);
    
    /**
     * Find all ACTIVE memberships for an organization (for member listing).
     * Admin-only endpoint uses this.
     * 
     * @param organizationId organization ID
     * @return list of active memberships
     */
    @Query("SELECT m FROM Membership m WHERE m.organization.id = :organizationId AND m.status = 'ACTIVE'")
    List<Membership> findActiveByOrganizationId(@Param("organizationId") UUID organizationId);
    
    /**
     * Find membership by ID and organization (for IDOR protection).
     * Used when updating a membership: ensure it belongs to current org.
     * 
     * @param id membership ID
     * @param organizationId current organization ID
     * @return Optional<Membership> or empty if not found
     */
    Optional<Membership> findByIdAndOrganizationId(UUID id, UUID organizationId);
    
    /**
     * Count ACTIVE ADMINs in an organization.
     * Used to enforce last-admin rule: cannot remove/demote last admin.
     * 
     * @param organizationId organization ID
     * @return count of active admins
     */
    @Query("SELECT COUNT(m) FROM Membership m WHERE m.organization.id = :organizationId " +
           "AND m.role = 'ADMIN' AND m.status = 'ACTIVE'")
    long countActiveAdminsByOrganizationId(@Param("organizationId") UUID organizationId);
    
    /**
     * Check if user has an existing membership in an organization.
     * Used to prevent duplicate memberships (though database also enforces this).
     * 
     * @param userId user ID
     * @param organizationId organization ID
     * @return true if membership exists
     */
    boolean existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
