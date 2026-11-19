package com.hiretrack.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration (Phase 2).
 * 
 * KEY CONCEPTS:
 * 1. SecurityFilterChain: Defines which requests are protected and how
 * 2. AuthenticationManager: Handles authentication (username/password)
 * 3. PasswordEncoder: BCrypt hashing for passwords
 * 4. UserDetailsService: Loads user from database
 * 5. CSRF protection: Enabled for state-changing operations
 * 6. Session management: Server-side sessions in PostgreSQL
 * 
 * SECURITY MODEL:
 * - Public endpoints: /api/v1/auth/register, /api/v1/auth/login, /actuator/health
 * - All other endpoints: require authentication (401 if not authenticated)
 * - Authorization (role checks): enforced in service layer (not here)
 * - CSRF: Required for POST/PUT/DELETE (token in cookie, sent via header)
 * 
 * Next.js comparison:
 * - This is like Next.js middleware + NextAuth configuration combined
 * - SecurityFilterChain = middleware pattern matching
 * - AuthenticationManager = NextAuth authorize callback
 * - Session management = NextAuth session strategy
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    /**
     * SecurityFilterChain: defines security rules for HTTP requests.
     * 
     * Flow:
     * 1. CSRF token validation (for POST/PUT/DELETE)
     * 2. Check if endpoint requires authentication
     * 3. If authenticated, populate SecurityContext
     * 4. Pass request to controller
     * 
     * @param http HttpSecurity builder (fluent API)
     * @return configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthContextFilter authContextFilter) throws Exception {
        http
            // CSRF protection: enabled, token stored in cookie
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Cookie name: XSRF-TOKEN (JavaScript can read, include in X-XSRF-TOKEN header)
            )
            
            // CORS: allow Next.js frontend (same-origin proxy in Phase 3)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Session management (server-side, stored in PostgreSQL via Spring Session)
            .sessionManagement(session -> session
                // Session fixation protection: change session ID on login
                .sessionFixation(fixation -> fixation.changeSessionId())
                // Max sessions per user: unlimited (user can login from multiple devices)
                .maximumSessions(10)
            )
            
            // Logout configuration
            .logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .invalidateHttpSession(true)  // Delete session from database
                .deleteCookies("SESSION")     // Clear session cookie
            )
            
            // Add custom filter to populate AuthContext after authentication
            .addFilterAfter(authContextFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * PasswordEncoder bean: BCrypt with work factor 12.
     * 
     * BCrypt is a one-way hash (cannot decrypt).
     * Work factor 12 = 2^12 iterations (slower = more secure against brute force).
     * OWASP recommends work factor 10+.
     * 
     * Next.js equivalent:
     *   await bcrypt.hash(password, 12)
     *   await bcrypt.compare(password, hash)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    /**
     * AuthenticationManager bean: handles authentication requests.
     * 
     * Uses DaoAuthenticationProvider:
     * 1. Load user from database (via UserDetailsService)
     * 2. Verify password (via PasswordEncoder)
     * 3. Return Authentication object if valid
     * 
     * This is used by login logic (not automatic form login).
     * 
     * Note: Spring Security 7 changed DaoAuthenticationProvider constructor.
     * It now requires UserDetailsService in the constructor.
     * 
     * @param userDetailsService loads user from database
     * @param passwordEncoder verifies password
     * @return configured AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        
        return new ProviderManager(authProvider);
    }
    
    /**
     * CORS configuration: allow Next.js frontend (Phase 3).
     * 
     * Phase 2: Localhost only (for testing with curl/Postman).
     * Phase 3: Next.js runs on localhost:3000, Spring on localhost:8080.
     *          Next.js proxies /api/v1/* to Spring (same-origin to browser).
     * 
     * Production: Frontend and backend on same domain (no CORS needed).
     */
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);  // Allow cookies
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
