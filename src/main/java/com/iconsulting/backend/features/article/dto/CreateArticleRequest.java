package com.iconsulting.backend.features.article.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateArticleRequest {

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Content is mandatory")
    private String content;

    private String author;
}
