package com.iconsulting.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration de sécurité pour l'application
 * Fournit le bean PasswordEncoder pour le hashage des mots de passe
 */
@Configuration
public class SecurityConfig {

    /**
     * Bean pour le hashage des mots de passe avec BCrypt
     * Force: 10 (balance entre sécurité et performance)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
