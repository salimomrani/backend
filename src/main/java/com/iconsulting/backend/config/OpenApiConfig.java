package com.iconsulting.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI/Swagger pour la documentation automatique de l'API
 * Configure l'authentification JWT Bearer Token
 * Accessible via: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Backend REST API")
                        .version("1.0.0")
                        .description("""
                                API REST sécurisée avec JWT pour la gestion des utilisateurs et articles.

                                ### Comment utiliser l'authentification:
                                1. Créer un compte via POST /api/v1/auth/register
                                2. Se connecter via POST /api/v1/auth/login pour obtenir un token JWT
                                3. Cliquer sur le bouton **Authorize** 🔓
                                4. Entrer le token JWT (ne pas inclure "Bearer ")
                                5. Tester les endpoints protégés
                                """)
                        .contact(new Contact()
                                .name("iConsulting")
                                .email("contact@iconsulting.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                // Ajouter le security scheme JWT Bearer Token
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Entrer le token JWT obtenu après login (sans 'Bearer ')")))
                // Ajouter le security requirement global (tous les endpoints protégés par défaut)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
