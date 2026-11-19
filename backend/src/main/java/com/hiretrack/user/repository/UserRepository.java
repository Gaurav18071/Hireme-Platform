package com.hiretrack.user.repository;

import com.hiretrack.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * UserRepository: data access for User entities.
 * 
 * Spring Data JPA generates implementations at runtime.
 * Methods follow naming conventions: findBy + PropertyName.
 * 
 * Similar to Prisma:
 *   await prisma.user.findUnique({ where: { email } })
 * 
 * Note: Users are NOT tenant-scoped (they exist globally).
 * Tenant filtering happens at the Membership level.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by email (case-sensitive in Java, but database index is case-insensitive).
     * 
     * @param email normalized email (lowercase)
     * @return Optional<User> or empty if not found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if email exists (for registration duplicate check).
     * 
     * @param email normalized email
     * @return true if email exists
     */
    boolean existsByEmail(String email);
}
