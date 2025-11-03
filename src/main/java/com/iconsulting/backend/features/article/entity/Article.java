package com.iconsulting.backend.features.article.entity;

import com.iconsulting.backend.common.entity.BaseEntity;
import com.iconsulting.backend.features.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entité JPA représentant un article de blog
 * Chaque article est lié à un auteur (User) via une relation ManyToOne
 */
@Entity
@Table(
    name = "articles",
    indexes = {
        @Index(name = "idx_article_author", columnList = "author_id"),
        @Index(name = "idx_article_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "author") // Évite les boucles infinies dans les logs
public class Article extends BaseEntity {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 200, message = "Le titre doit contenir entre 5 et 200 caractères")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Le contenu est obligatoire")
    @Size(min = 10, message = "Le contenu doit contenir au moins 10 caractères")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_article_author"))
    private User author;
}
