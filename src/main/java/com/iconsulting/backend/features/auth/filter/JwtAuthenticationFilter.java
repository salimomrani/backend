package com.iconsulting.backend.features.auth.filter;

import com.iconsulting.backend.features.auth.service.JwtService;
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

/**
 * Filtre JWT qui intercepte toutes les requêtes HTTP entrantes
 * Vérifie la présence et la validité du token JWT dans le header Authorization
 * Si le token est valide, authentifie l'utilisateur dans le SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Récupérer le header Authorization
        final String authHeader = request.getHeader("Authorization");

        // Si pas de header ou format incorrect, passer au filtre suivant
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraire le token JWT (enlever "Bearer ")
            final String jwt = authHeader.substring(7);

            // Extraire l'email (username) depuis le token
            final String userEmail = jwtService.extractUsername(jwt);

            // Si l'email existe et qu'il n'y a pas encore d'authentification dans le contexte
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Charger les détails de l'utilisateur depuis la base de données
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Vérifier si le token est valide
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Créer un objet d'authentification
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Ajouter les détails de la requête
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Mettre l'authentification dans le SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("User {} authenticated successfully", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
