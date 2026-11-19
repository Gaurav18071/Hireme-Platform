package com.hiretrack.organization.repository;

import com.hiretrack.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * OrganizationRepository: data access for Organization entities.
 * 
 * Phase 2: Simple CRUD only (create on registration).
 * Later phases: Add search, filtering, metadata updates.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}
