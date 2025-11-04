package com.iconsulting.backend.features.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Données requises pour créer un nouvel article")
public class CreateArticleRequest {

    @Schema(
        description = "Titre de l'article (5-200 caractères)",
        example = "Introduction à Spring Boot",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 200, message = "Le titre doit contenir entre 5 et 200 caractères")
    private String title;

    @Schema(
        description = "Contenu complet de l'article (minimum 10 caractères)",
        example = "Spring Boot est un framework Java qui facilite la création d'applications...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Le contenu est obligatoire")
    @Size(min = 10, message = "Le contenu doit contenir au moins 10 caractères")
    private String content;
}
