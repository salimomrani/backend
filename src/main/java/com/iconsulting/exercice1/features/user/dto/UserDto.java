package com.iconsulting.exercice1.features.user.dto;

import com.iconsulting.exercice1.features.user.entity.User;

import java.time.LocalDateTime;

/**
 * Record DTO pour représenter un utilisateur dans les réponses API
 * Les records sont immutables et parfaits pour les DTOs
 * N'inclut jamais le mot de passe pour des raisons de sécurité
 */
public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        Boolean active,
        User.UserRole role,
        Boolean emailVerified,
        LocalDateTime lastLogin,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Long version
) {
}
