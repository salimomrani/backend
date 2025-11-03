package com.iconsulting.backend.features.article.dto;

import lombok.Data;

@Data
public class UpdateArticleRequest {

    private String title;

    private String content;

    private String author;
}
