package com.iconsulting.backend.features.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Données pour mettre à jour un article existant (tous les champs sont optionnels)")
public class UpdateArticleRequest {

    @Schema(
        description = "Nouveau titre de l'article (5-200 caractères, optionnel)",
        example = "Introduction complète à Spring Boot",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(min = 5, max = 200, message = "Le titre doit contenir entre 5 et 200 caractères")
    private String title;

    @Schema(
        description = "Nouveau contenu de l'article (minimum 10 caractères, optionnel)",
        example = "Spring Boot 3 est la dernière version majeure...",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(min = 10, message = "Le contenu doit contenir au moins 10 caractères")
    private String content;

    @Schema(
        description = "Nouvel identifiant de l'auteur (optionnel, permet de changer l'auteur)",
        example = "2",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long authorId;
}
