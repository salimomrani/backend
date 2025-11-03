package com.iconsulting.backend.features.article.mapper;

import com.iconsulting.backend.features.article.dto.ArticleDto;
import com.iconsulting.backend.features.article.dto.CreateArticleRequest;
import com.iconsulting.backend.features.article.dto.UpdateArticleRequest;
import com.iconsulting.backend.features.article.entity.Article;
import com.iconsulting.backend.features.user.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    /**
     * Convertit une entité Article en ArticleDto
     * Mappe automatiquement l'auteur (User) vers AuthorDto
     */
    @Mapping(target = "author.id", source = "author.id")
    @Mapping(target = "author.firstName", source = "author.firstName")
    @Mapping(target = "author.lastName", source = "author.lastName")
    @Mapping(target = "author.email", source = "author.email")
    ArticleDto toDto(Article article);

    /**
     * Convertit CreateArticleRequest en Article
     * Ignore le champ author car il sera défini manuellement dans le service via authorId
     * Les champs de BaseEntity (id, timestamps, version) sont gérés automatiquement par JPA
     */
    @Mapping(target = "author", ignore = true) // Sera défini dans le service
    Article toEntity(CreateArticleRequest createArticleRequest);

    /**
     * Met à jour une entité Article depuis UpdateArticleRequest
     * Ignore les propriétés null pour ne mettre à jour que les champs fournis
     * Les champs de BaseEntity et author sont gérés par JPA et le service
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true) // Sera géré dans le service si authorId est fourni
    void updateFromDto(UpdateArticleRequest updateArticleRequest, @MappingTarget Article article);

    /**
     * Mappe User vers AuthorDto
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    ArticleDto.AuthorDto userToAuthorDto(User user);
}
