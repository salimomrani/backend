# Article DTOs

## ArticleDto (Response)

```java
package com.blog.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
    private UUID id;
    private String slug;
    private String title;
    private String excerpt;
    private String contentMarkdown;
    private String coverImageUrl;
    private List<String> tags;
    private UserPreviewDto author;
    private Integer likes;
    private Integer commentsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## CreateArticleDto (Request)

```java
package com.blog.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateArticleDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Excerpt is required")
    @Size(max = 500, message = "Excerpt must not exceed 500 characters")
    private String excerpt;

    @NotBlank(message = "Content is required")
    private String contentMarkdown;

    private String coverImageUrl;

    @NotNull(message = "Tags cannot be null")
    private List<String> tags = new ArrayList<>();

    // TODO: Author ID should come from authenticated user context
    // For now, it can be passed in the request or extracted from security context
    private UUID authorId;
}
```

## UpdateArticleDto (Request)

```java
package com.blog.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArticleDto {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 500, message = "Excerpt must not exceed 500 characters")
    private String excerpt;

    private String contentMarkdown;

    private String coverImageUrl;

    private List<String> tags;
}
```

## Mapper Example

```java
package com.blog.backend.mapper;

import com.blog.backend.dto.ArticleDto;
import com.blog.backend.entity.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleMapper {

    private final UserMapper userMapper;

    public ArticleDto toDto(Article article) {
        return new ArticleDto(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getExcerpt(),
            article.getContentMarkdown(),
            article.getCoverImageUrl(),
            article.getTags(),
            userMapper.toPreviewDto(article.getAuthor()),
            article.getLikes(),
            article.getCommentsCount(),
            article.getCreatedAt(),
            article.getUpdatedAt()
        );
    }
}
```

## Notes

- `ArticleDto` includes full article information with embedded author preview
- `CreateArticleDto` requires title, excerpt, and content
- `UpdateArticleDto` allows partial updates (all fields optional)
- Tags are validated to ensure not null but can be empty list
- Slug is auto-generated from title in the entity
- **TODO:** Implement authentication to automatically get authorId from security context
- **TODO:** Consider adding pagination DTOs for list responses
