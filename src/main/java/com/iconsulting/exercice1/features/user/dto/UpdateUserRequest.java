package com.iconsulting.exercice1.features.user.dto;

import com.iconsulting.exercice1.features.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Record DTO pour la mise à jour d'un utilisateur
 * Tous les champs sont optionnels (nullable)
 * Seuls les champs non-null seront mis à jour
 */
public record UpdateUserRequest(
        @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
        String firstName,

        @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
        String lastName,

        @Email(message = "L'email doit être valide")
        String email,

        @Size(max = 20, message = "Le numéro de téléphone ne doit pas dépasser 20 caractères")
        String phone,

        Boolean active,

        User.UserRole role,

        Boolean emailVerified
) {
}
