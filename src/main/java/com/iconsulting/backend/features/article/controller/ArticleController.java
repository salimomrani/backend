package com.iconsulting.backend.features.article.controller;

import com.iconsulting.backend.common.constants.ApiConstants;
import com.iconsulting.backend.common.dto.ApiResponse;
import com.iconsulting.backend.features.article.dto.ArticleDto;
import com.iconsulting.backend.features.article.dto.CreateArticleRequest;
import com.iconsulting.backend.features.article.dto.UpdateArticleRequest;
import com.iconsulting.backend.features.article.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST pour la gestion des articles
 */
@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/articles")
@RequiredArgsConstructor
@Tag(name = "Articles", description = "API de gestion des articles de blog")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * Crée un nouvel article
     */
    @PostMapping
    @Operation(
        summary = "Créer un article",
        description = "Crée un nouvel article avec un auteur (User). L'auteur doit exister dans la base de données."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Article créé avec succès"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Données invalides (validation échouée)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Auteur (User) introuvable"
        )
    })
    public ResponseEntity<ApiResponse<ArticleDto>> createArticle(
            @Valid @RequestBody CreateArticleRequest createArticleRequest) {

        ArticleDto createdArticle = articleService.createArticle(createArticleRequest);

        ApiResponse<ArticleDto> response = new ApiResponse<>(
            true,
            ApiConstants.CREATED_SUCCESSFULLY,
            createdArticle
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Récupère tous les articles
     */
    @GetMapping
    @Operation(
        summary = "Lister tous les articles",
        description = "Récupère la liste complète des articles avec les informations de leurs auteurs"
    )
    public ResponseEntity<ApiResponse<List<ArticleDto>>> getAllArticles() {
        List<ArticleDto> articles = articleService.getAllArticles();

        ApiResponse<List<ArticleDto>> response = new ApiResponse<>(
            true,
            "Articles retrieved successfully",
            articles
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère un article par son ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Récupérer un article",
        description = "Récupère un article par son ID avec les informations de l'auteur"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Article trouvé"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Article introuvable"
        )
    })
    public ResponseEntity<ApiResponse<ArticleDto>> getArticleById(
            @Parameter(description = "ID de l'article") @PathVariable Long id) {

        ArticleDto article = articleService.getArticleById(id);

        ApiResponse<ArticleDto> response = new ApiResponse<>(
            true,
            "Article retrieved successfully",
            article
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Met à jour un article
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Mettre à jour un article",
        description = "Met à jour les informations d'un article. L'auteur peut être modifié en fournissant un nouveau authorId."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Article mis à jour avec succès"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Données invalides (validation échouée)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Article ou auteur introuvable"
        )
    })
    public ResponseEntity<ApiResponse<ArticleDto>> updateArticle(
            @Parameter(description = "ID de l'article") @PathVariable Long id,
            @Valid @RequestBody UpdateArticleRequest updateArticleRequest) {

        ArticleDto updatedArticle = articleService.updateArticle(id, updateArticleRequest);

        ApiResponse<ArticleDto> response = new ApiResponse<>(
            true,
            ApiConstants.UPDATED_SUCCESSFULLY,
            updatedArticle
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Supprime un article
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Supprimer un article",
        description = "Supprime définitivement un article du système"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Article supprimé avec succès"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Article introuvable"
        )
    })
    public ResponseEntity<ApiResponse<Void>> deleteArticle(
            @Parameter(description = "ID de l'article") @PathVariable Long id) {

        articleService.deleteArticle(id);

        ApiResponse<Void> response = new ApiResponse<>(
            true,
            ApiConstants.DELETED_SUCCESSFULLY
        );

        return ResponseEntity.ok(response);
    }
}
