package com.iconsulting.backend.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'inscription (register)")
public class RegisterRequest {

    @Schema(
        description = "Prénom de l'utilisateur (2-50 caractères)",
        example = "Jean",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;

    @Schema(
        description = "Nom de l'utilisateur (2-50 caractères)",
        example = "Dupont",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName;

    @Schema(
        description = "Email de l'utilisateur (doit être unique)",
        example = "jean.dupont@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @Schema(
        description = "Mot de passe (minimum 6 caractères)",
        example = "password123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @Schema(
        description = "Numéro de téléphone (optionnel, max 20 caractères)",
        example = "+33612345678",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 20, message = "Le numéro de téléphone ne doit pas dépasser 20 caractères")
    private String phone;
}
