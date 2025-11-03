package com.iconsulting.backend.features.article.service;

import com.iconsulting.backend.common.exception.ResourceNotFoundException;
import com.iconsulting.backend.features.article.dto.ArticleDto;
import com.iconsulting.backend.features.article.dto.CreateArticleRequest;
import com.iconsulting.backend.features.article.dto.UpdateArticleRequest;
import com.iconsulting.backend.features.article.entity.Article;
import com.iconsulting.backend.features.article.mapper.ArticleMapper;
import com.iconsulting.backend.features.article.repository.ArticleRepository;
import com.iconsulting.backend.features.user.entity.User;
import com.iconsulting.backend.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ArticleDto createArticle(CreateArticleRequest createArticleRequest) {
        // Récupérer l'auteur
        User author = userRepository.findById(createArticleRequest.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + createArticleRequest.getAuthorId()));

        // Créer l'article et définir l'auteur
        Article article = articleMapper.toEntity(createArticleRequest);
        article.setAuthor(author);

        Article savedArticle = articleRepository.save(article);
        return articleMapper.toDto(savedArticle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleDto> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(articleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDto getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
        return articleMapper.toDto(article);
    }

    @Override
    @Transactional
    public ArticleDto updateArticle(Long id, UpdateArticleRequest updateArticleRequest) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));

        // Mettre à jour les champs de base (title, content)
        articleMapper.updateFromDto(updateArticleRequest, article);

        // Si authorId est fourni, mettre à jour l'auteur
        if (updateArticleRequest.getAuthorId() != null) {
            User newAuthor = userRepository.findById(updateArticleRequest.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + updateArticleRequest.getAuthorId()));
            article.setAuthor(newAuthor);
        }

        Article updatedArticle = articleRepository.save(article);
        return articleMapper.toDto(updatedArticle);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article not found with id: " + id);
        }
        articleRepository.deleteById(id);
    }
}
