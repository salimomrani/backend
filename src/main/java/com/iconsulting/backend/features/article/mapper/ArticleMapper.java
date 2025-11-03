package com.iconsulting.backend.features.article.mapper;

import com.iconsulting.backend.features.article.dto.ArticleDto;
import com.iconsulting.backend.features.article.dto.CreateArticleRequest;
import com.iconsulting.backend.features.article.dto.UpdateArticleRequest;
import com.iconsulting.backend.features.article.entity.Article;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    ArticleDto toDto(Article article);

    Article toEntity(CreateArticleRequest createArticleRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(UpdateArticleRequest updateArticleRequest, @MappingTarget Article article);
}
