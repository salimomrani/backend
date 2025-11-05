package com.iconsulting.backend.config;

import com.iconsulting.backend.features.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de la sécurité Spring Security
 * Configure JWT, CORS, endpoints publics/protégés, et méthode d'authentification
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Active @PreAuthorize, @PostAuthorize, @Secured
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Configuration de la chaîne de filtres de sécurité
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF (pas nécessaire pour API REST stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Configurer CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configurer les autorisations des endpoints
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics (pas d'authentification requise)
                        .requestMatchers(
                                "/api/v1/auth/**",           // Authentification (login, register)
                                "/swagger-ui/**",             // Swagger UI
                                "/swagger-ui.html",           // Swagger UI HTML
                                "/api-docs/**",               // OpenAPI docs
                                "/v3/api-docs/**",            // OpenAPI v3
                                "/h2-console/**",             // Console H2 (dev uniquement)
                                "/actuator/health/**",        // Health checks (Kubernetes probes)
                                "/actuator/info",             // Application info
                                "/error"                      // Page d'erreur
                        ).permitAll()

                        // GET sur /api/v1/articles est public (récupération de tous les articles)
                        .requestMatchers(HttpMethod.GET, "/api/v1/articles").permitAll()
                        // GET sur /api/v1/articles/{id} est public (récupération d'un article par ID)
                        .requestMatchers(HttpMethod.GET, "/api/v1/articles/{id}").permitAll()

                        // Tous les autres endpoints nécessitent une authentification
                        .anyRequest().authenticated()
                )

                // Configuration de la gestion de session (STATELESS pour JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configurer l'authentication provider
                .authenticationProvider(authenticationProvider())

                // Ajouter le filtre JWT avant le filtre d'authentification UsernamePassword
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration CORS (Cross-Origin Resource Sharing)
     * Permet aux frontends (React, Vue, Angular) de communiquer avec l'API
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200", "http://localhost:5173")); // React, Angular, Vite
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Provider d'authentification utilisant UserDetailsService et BCrypt
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager pour gérer l'authentification
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Encodeur de mot de passe BCrypt (force 10 = balance sécurité/performance)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
