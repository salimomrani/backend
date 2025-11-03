package com.iconsulting.backend.features.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Données d'un article de blog avec les informations de l'auteur")
public class ArticleDto {

    @Schema(description = "Identifiant unique de l'article", example = "1")
    private Long id;

    @Schema(description = "Titre de l'article", example = "Introduction à Spring Boot")
    private String title;

    @Schema(description = "Contenu complet de l'article", example = "Spring Boot est un framework...")
    private String content;

    @Schema(description = "Informations de l'auteur de l'article")
    private AuthorDto author;

    @Schema(description = "Date de création de l'article", example = "2025-11-03T18:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification de l'article", example = "2025-11-03T18:30:00")
    private LocalDateTime updatedAt;

    /**
     * DTO imbriqué pour les informations de l'auteur
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Informations de l'auteur d'un article")
    public static class AuthorDto {

        @Schema(description = "Identifiant unique de l'auteur", example = "1")
        private Long id;

        @Schema(description = "Prénom de l'auteur", example = "Jean")
        private String firstName;

        @Schema(description = "Nom de l'auteur", example = "Dupont")
        private String lastName;

        @Schema(description = "Email de l'auteur", example = "jean.dupont@example.com")
        private String email;

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }
}
