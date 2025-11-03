# ArticleController

## REST Controller

```java
package com.blog.backend.controller;

import com.blog.backend.dto.ArticleDto;
import com.blog.backend.dto.CreateArticleDto;
import com.blog.backend.dto.UpdateArticleDto;
import com.blog.backend.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * GET /api/posts
     * Get all articles with pagination and optional tag filter
     */
    @GetMapping
    public ResponseEntity<Page<ArticleDto>> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String tag) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ArticleDto> articles;
        if (tag != null && !tag.isEmpty()) {
            articles = articleService.getArticlesByTag(tag, pageable);
        } else {
            articles = articleService.getAllArticles(pageable);
        }

        return ResponseEntity.ok(articles);
    }

    /**
     * GET /api/posts/recent
     * Get recent articles (for landing page)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ArticleDto>> getRecentArticles(
            @RequestParam(defaultValue = "6") int limit) {
        List<ArticleDto> articles = articleService.getRecentArticles(limit);
        return ResponseEntity.ok(articles);
    }

    /**
     * GET /api/posts/{slug}
     * Get article by slug
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ArticleDto> getArticleBySlug(@PathVariable String slug) {
        ArticleDto article = articleService.getArticleBySlug(slug);
        return ResponseEntity.ok(article);
    }

    /**
     * POST /api/posts
     * Create new article
     */
    @PostMapping
    public ResponseEntity<ArticleDto> createArticle(@Valid @RequestBody CreateArticleDto createArticleDto) {
        ArticleDto createdArticle = articleService.createArticle(createArticleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdArticle);
    }

    /**
     * PUT /api/posts/{slug}
     * Update article
     */
    @PutMapping("/{slug}")
    public ResponseEntity<ArticleDto> updateArticle(
            @PathVariable String slug,
            @Valid @RequestBody UpdateArticleDto updateArticleDto) {
        ArticleDto updatedArticle = articleService.updateArticle(slug, updateArticleDto);
        return ResponseEntity.ok(updatedArticle);
    }

    /**
     * DELETE /api/posts/{slug}
     * Delete article
     */
    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> deleteArticle(@PathVariable String slug) {
        articleService.deleteArticle(slug);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/posts/{slug}/like
     * Increment article likes
     */
    @PostMapping("/{slug}/like")
    public ResponseEntity<ArticleDto> likeArticle(@PathVariable String slug) {
        ArticleDto article = articleService.likeArticle(slug);
        return ResponseEntity.ok(article);
    }
}
```

## API Endpoints Summary

| Method | Endpoint | Description | Request Params | Response |
|--------|----------|-------------|----------------|----------|
| GET | `/api/posts` | Get all articles with pagination | `page`, `size`, `sortBy`, `sortDir`, `tag` | `Page<ArticleDto>` |
| GET | `/api/posts/recent` | Get recent articles | `limit` (default: 6) | `List<ArticleDto>` |
| GET | `/api/posts/{slug}` | Get article by slug | - | `ArticleDto` |
| POST | `/api/posts` | Create new article | - | `ArticleDto` (201) |
| PUT | `/api/posts/{slug}` | Update article | - | `ArticleDto` |
| DELETE | `/api/posts/{slug}` | Delete article | - | 204 No Content |
| POST | `/api/posts/{slug}/like` | Like article | - | `ArticleDto` |

## Example Requests

### Get All Articles with Pagination
```
GET /api/posts?page=0&size=10&sortBy=createdAt&sortDir=desc
```

### Get Articles by Tag
```
GET /api/posts?tag=angular&page=0&size=10
```

### Get Recent Articles
```
GET /api/posts/recent?limit=6
```

### Create Article
```json
POST /api/posts
Content-Type: application/json

{
  "title": "Getting Started with Angular",
  "excerpt": "Learn the basics of Angular framework",
  "contentMarkdown": "# Introduction\n\nAngular is a powerful framework...",
  "coverImageUrl": "https://example.com/angular.jpg",
  "tags": ["angular", "typescript", "web-development"],
  "authorId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Update Article
```json
PUT /api/posts/getting-started-with-angular
Content-Type: application/json

{
  "title": "Getting Started with Angular 18",
  "tags": ["angular", "typescript", "web-development", "angular18"]
}
```

### Like Article
```
POST /api/posts/getting-started-with-angular/like
```

## Response Format - Paginated Response

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "slug": "getting-started-with-angular",
      "title": "Getting Started with Angular",
      "excerpt": "Learn the basics of Angular framework",
      "contentMarkdown": "# Introduction\n\n...",
      "coverImageUrl": "https://example.com/angular.jpg",
      "tags": ["angular", "typescript"],
      "author": {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "username": "johndoe",
        "avatarUrl": "https://example.com/avatar.jpg"
      },
      "likes": 42,
      "commentsCount": 5,
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 42,
  "totalPages": 5,
  "last": false,
  "first": true,
  "number": 0,
  "size": 10,
  "numberOfElements": 10,
  "empty": false
}
```

## Notes

- Endpoint is `/posts` (not `/articles`) to match Angular frontend expectations
- Supports pagination with customizable page size and sorting
- Tag filtering via query parameter
- `/recent` endpoint for landing page recent articles section
- Uses `slug` instead of `id` for better SEO and user-friendly URLs
- Like functionality increments counter (no user tracking yet)
- **TODO:** Implement proper like/unlike with user authentication
- **TODO:** Add comment endpoints when Comment entity is created
- **TODO:** Add search endpoint for full-text search
