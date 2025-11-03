# ArticleService

## Service Interface

```java
package com.blog.backend.service;

import com.blog.backend.dto.ArticleDto;
import com.blog.backend.dto.CreateArticleDto;
import com.blog.backend.dto.UpdateArticleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ArticleService {

    /**
     * Get all articles with pagination
     * @param pageable pagination information
     * @return Page of articles
     */
    Page<ArticleDto> getAllArticles(Pageable pageable);

    /**
     * Get article by slug
     * @param slug the article slug
     * @return ArticleDto if found
     * @throws ResourceNotFoundException if article not found
     */
    ArticleDto getArticleBySlug(String slug);

    /**
     * Get articles by tag with pagination
     * @param tag the tag to filter by
     * @param pageable pagination information
     * @return Page of articles with the specified tag
     */
    Page<ArticleDto> getArticlesByTag(String tag, Pageable pageable);

    /**
     * Get recent articles (for landing page)
     * @param limit the maximum number of articles to return
     * @return List of recent articles
     */
    List<ArticleDto> getRecentArticles(int limit);

    /**
     * Create new article
     * @param createArticleDto the article data
     * @return Created ArticleDto
     * @throws ResourceNotFoundException if author not found
     * @throws DuplicateResourceException if slug already exists
     */
    ArticleDto createArticle(CreateArticleDto createArticleDto);

    /**
     * Update article
     * @param slug the article slug
     * @param updateArticleDto the updated article data
     * @return Updated ArticleDto
     * @throws ResourceNotFoundException if article not found
     */
    ArticleDto updateArticle(String slug, UpdateArticleDto updateArticleDto);

    /**
     * Delete article
     * @param slug the article slug
     * @throws ResourceNotFoundException if article not found
     */
    void deleteArticle(String slug);

    /**
     * Increment likes count
     * @param slug the article slug
     * @return Updated ArticleDto
     * @throws ResourceNotFoundException if article not found
     */
    ArticleDto likeArticle(String slug);

    /**
     * TODO: Implement these methods when Comment entity is created
     * ArticleDto addComment(String slug, CreateCommentDto commentDto);
     * void deleteComment(String slug, UUID commentId);
     */
}
```

## Service Implementation (Partial)

```java
package com.blog.backend.service.impl;

import com.blog.backend.dto.ArticleDto;
import com.blog.backend.dto.CreateArticleDto;
import com.blog.backend.dto.UpdateArticleDto;
import com.blog.backend.entity.Article;
import com.blog.backend.entity.User;
import com.blog.backend.exception.DuplicateResourceException;
import com.blog.backend.exception.ResourceNotFoundException;
import com.blog.backend.mapper.ArticleMapper;
import com.blog.backend.repository.ArticleRepository;
import com.blog.backend.repository.UserRepository;
import com.blog.backend.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleDto> getAllArticles(Pageable pageable) {
        return articleRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(articleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDto getArticleBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with slug: " + slug));
        return articleMapper.toDto(article);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleDto> getArticlesByTag(String tag, Pageable pageable) {
        return articleRepository.findByTag(tag, pageable)
                .map(articleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleDto> getRecentArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return articleRepository.findAll(pageable)
                .stream()
                .map(articleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ArticleDto createArticle(CreateArticleDto createArticleDto) {
        // Find author
        User author = userRepository.findById(createArticleDto.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + createArticleDto.getAuthorId()));

        // Create article entity
        Article article = new Article();
        article.setTitle(createArticleDto.getTitle());
        article.setExcerpt(createArticleDto.getExcerpt());
        article.setContentMarkdown(createArticleDto.getContentMarkdown());
        article.setCoverImageUrl(createArticleDto.getCoverImageUrl());
        article.setTags(createArticleDto.getTags());
        article.setAuthor(author);
        // Slug is auto-generated in @PrePersist

        // Save article
        Article savedArticle = articleRepository.save(article);
        return articleMapper.toDto(savedArticle);
    }

    @Override
    public ArticleDto updateArticle(String slug, UpdateArticleDto updateArticleDto) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with slug: " + slug));

        // Update fields if provided
        if (updateArticleDto.getTitle() != null) {
            article.setTitle(updateArticleDto.getTitle());
            // Slug will be regenerated in @PreUpdate
        }
        if (updateArticleDto.getExcerpt() != null) {
            article.setExcerpt(updateArticleDto.getExcerpt());
        }
        if (updateArticleDto.getContentMarkdown() != null) {
            article.setContentMarkdown(updateArticleDto.getContentMarkdown());
        }
        if (updateArticleDto.getCoverImageUrl() != null) {
            article.setCoverImageUrl(updateArticleDto.getCoverImageUrl());
        }
        if (updateArticleDto.getTags() != null) {
            article.setTags(updateArticleDto.getTags());
        }

        Article updatedArticle = articleRepository.save(article);
        return articleMapper.toDto(updatedArticle);
    }

    @Override
    public void deleteArticle(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with slug: " + slug));
        articleRepository.delete(article);
    }

    @Override
    public ArticleDto likeArticle(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with slug: " + slug));

        article.setLikes(article.getLikes() + 1);
        Article updatedArticle = articleRepository.save(article);
        return articleMapper.toDto(updatedArticle);
    }
}
```

## Notes

- Service layer handles business logic for articles
- Uses pagination for list endpoints (better performance)
- `getRecentArticles` for landing page recent articles section
- Slug is auto-generated from title in entity `@PrePersist`
- `likeArticle` increments the likes counter
- **TODO:** Implement proper like/unlike system with user tracking
- **TODO:** Add Comment entity and methods for comment management
- **TODO:** Add search functionality for articles
- **TODO:** Implement authentication to get authorId from security context
