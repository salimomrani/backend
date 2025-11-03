package com.iconsulting.backend.features.article.service;

import com.iconsulting.backend.features.article.dto.ArticleDto;
import com.iconsulting.backend.features.article.dto.CreateArticleRequest;
import com.iconsulting.backend.features.article.dto.UpdateArticleRequest;

import java.util.List;

public interface ArticleService {

    ArticleDto createArticle(CreateArticleRequest createArticleRequest);

    List<ArticleDto> getAllArticles();

    ArticleDto getArticleById(Long id);

    ArticleDto updateArticle(Long id, UpdateArticleRequest updateArticleRequest);

    void deleteArticle(Long id);
}
