package com.hiretrack.common.security;

import com.hiretrack.user.entity.User;
import com.hiretrack.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CustomUserDetailsService: loads user from database for authentication.
 * 
 * Spring Security contract:
 * - Input: username (in our case, email)
 * - Output: UserDetails (username, password hash, authorities/roles)
 * - Exception: UsernameNotFoundException if not found
 * 
 * Note: This returns minimal authorities (empty list).
 * Real authorization (role checks) happens in service layer via AuthContext.
 * 
 * Why not use Spring Security authorities?
 * - Roles are per-organization, not global
 * - A user can be ADMIN in one org, RECRUITER in another
 * - SecurityContext holds global authorities; we need request-scoped roles
 * 
 * Next.js equivalent:
 *   authorize: async (credentials) => {
 *     const user = await prisma.user.findUnique({ where: { email } });
 *     if (!user) return null;
 *     // verify password...
 *     return user;
 *   }
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.toLowerCase())
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        
        // Return Spring Security's UserDetails implementation
        // Note: authorities list is empty (roles handled by AuthContext)
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPasswordHash())
            .authorities(List.of())  // Empty: roles are per-organization
            .build();
    }
}
