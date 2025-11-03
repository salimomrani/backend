# ArticleRepository

## Repository Interface

```java
package com.blog.backend.repository;

import com.blog.backend.entity.Article;
import com.blog.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    /**
     * Find article by slug
     * @param slug the article slug
     * @return Optional containing the article if found
     */
    Optional<Article> findBySlug(String slug);

    /**
     * Find all articles by author
     * @param author the author
     * @param pageable pagination information
     * @return Page of articles
     */
    Page<Article> findByAuthor(User author, Pageable pageable);

    /**
     * Find articles by tag (using custom query)
     * @param tag the tag to filter by
     * @param pageable pagination information
     * @return Page of articles containing the tag
     */
    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t = :tag")
    Page<Article> findByTag(@Param("tag") String tag, Pageable pageable);

    /**
     * Find all articles ordered by creation date (newest first)
     * @param pageable pagination information
     * @return Page of articles
     */
    Page<Article> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Check if slug already exists
     * @param slug the slug to check
     * @return true if slug exists, false otherwise
     */
    boolean existsBySlug(String slug);

    /**
     * Find recent articles (limit provided by Pageable)
     * @param pageable pagination information with size limit
     * @return List of recent articles
     */
    List<Article> findTop10ByOrderByCreatedAtDesc();
}
```

## Custom Queries (if needed)

```java
// Example of more complex query for searching articles
@Query("SELECT DISTINCT a FROM Article a " +
       "LEFT JOIN a.tags t " +
       "WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
       "OR LOWER(a.excerpt) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
       "OR LOWER(t) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
Page<Article> searchArticles(@Param("searchTerm") String searchTerm, Pageable pageable);
```

## Notes

- Extends `JpaRepository` for standard CRUD operations
- `findBySlug` allows finding articles by URL-friendly slug
- `findByTag` uses JPQL query to search in tags collection
- Pagination support with `Pageable` parameter
- `findTop10ByOrderByCreatedAtDesc` for recent articles (landing page)
- Custom search query can be added for full-text search
- **TODO:** Consider adding full-text search with Elasticsearch for better performance
- **TODO:** Add method to find trending articles (by likes, recent comments, etc.)
