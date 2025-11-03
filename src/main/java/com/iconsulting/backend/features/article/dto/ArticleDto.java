package com.iconsulting.backend.features.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
    private Long id;
    private String title;
    private String content;
    private AuthorDto author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * DTO imbriqué pour les informations de l'auteur
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }
}
