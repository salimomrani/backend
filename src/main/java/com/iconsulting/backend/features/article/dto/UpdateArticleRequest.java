package com.iconsulting.backend.features.article.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArticleRequest {

    @Size(min = 5, max = 200, message = "Le titre doit contenir entre 5 et 200 caractères")
    private String title;

    @Size(min = 10, message = "Le contenu doit contenir au moins 10 caractères")
    private String content;

    private Long authorId; // Optionnel : permet de changer l'auteur
}
