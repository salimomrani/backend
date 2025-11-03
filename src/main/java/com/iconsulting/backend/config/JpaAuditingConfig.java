package com.iconsulting.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Configuration de l'audit JPA
 * Active l'audit automatique des entités (createdBy, updatedBy, etc.)
 * Intégré avec Spring Security pour récupérer l'utilisateur authentifié
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Fournit l'utilisateur courant pour l'audit
     * Récupère l'email de l'utilisateur authentifié depuis le SecurityContext
     * Si aucun utilisateur n'est authentifié, retourne "system" par défaut
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            // Si l'utilisateur est "anonymousUser", retourner "system"
            if ("anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }

            // Récupérer l'email depuis UserDetails
            if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                return Optional.of(userDetails.getUsername()); // username = email
            }

            // Fallback : récupérer le nom directement
            return Optional.ofNullable(authentication.getName());
        };
    }
}
