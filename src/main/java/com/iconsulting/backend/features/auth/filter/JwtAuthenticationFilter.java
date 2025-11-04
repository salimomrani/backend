package com.iconsulting.backend.features.auth.filter;

import com.iconsulting.backend.features.auth.service.JwtService;
import com.iconsulting.backend.features.user.entity.User;
import com.iconsulting.backend.features.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT filter that intercepts all incoming HTTP requests
 * Checks the presence and validity of the JWT token in the Authorization header
 * If the token is valid, authenticates the user in the SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Get Authorization header
        final String authHeader = request.getHeader("Authorization");

        // If no header or incorrect format, pass to next filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token (remove "Bearer ")
            final String jwt = authHeader.substring(7);

            // Extract email (username) from token
            final String userEmail = jwtService.extractUsername(jwt);

            // If email exists and there's no authentication in context yet
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details from database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Check if token is valid
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Check if token was issued after last logout
                    boolean tokenStillValid = isTokenIssuedAfterLogout(jwt, userEmail);

                    if (tokenStillValid) {
                        // Create authentication object
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                        // Add request details
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // Set authentication in SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("User {} authenticated successfully", userEmail);
                    } else {
                        log.debug("Token for user {} was issued before logout, rejecting", userEmail);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Checks if the token was issued after the user's last logout
     * If the user has never logged out (lastLogout == null), the token is valid
     */
    private boolean isTokenIssuedAfterLogout(String token, String userEmail) {
        try {
            // Retrieve user from database
            User user = userRepository.findByEmail(userEmail).orElse(null);

            if (user == null) {
                return false;
            }

            // If user has never logged out, token is valid
            LocalDateTime lastLogout = user.getLastLogout();
            if (lastLogout == null) {
                return true;
            }

            // Extract token issued date
            Date tokenIssuedAt = jwtService.extractIssuedAt(token);

            // Convert LocalDateTime to Date for comparison
            Date lastLogoutDate = Date.from(lastLogout.atZone(ZoneId.systemDefault()).toInstant());

            // Token is valid if issued after last logout
            return tokenIssuedAt.after(lastLogoutDate);

        } catch (Exception e) {
            log.error("Error checking token issued date for user {}: {}", userEmail, e.getMessage());
            return false;
        }
    }
}
