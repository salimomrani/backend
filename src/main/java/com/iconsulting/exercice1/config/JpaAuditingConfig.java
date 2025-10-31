package com.iconsulting.exercice1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Configuration de l'audit JPA
 * Active l'audit automatique des entités (createdBy, updatedBy, etc.)
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Fournit l'utilisateur courant pour l'audit
     * TODO: Intégrer avec Spring Security pour récupérer l'utilisateur authentifié
     * Pour l'instant retourne "system" par défaut
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // TODO: Récupérer l'utilisateur authentifié depuis SecurityContext
            // return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            //         .map(Authentication::getName);

            return Optional.of("system");
        };
    }
}
