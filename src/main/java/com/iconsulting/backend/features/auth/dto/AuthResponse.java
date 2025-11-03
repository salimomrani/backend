package com.iconsulting.backend.features.auth.dto;

import com.iconsulting.backend.features.user.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse d'authentification contenant le token JWT et les informations utilisateur")
public class AuthResponse {

    @Schema(description = "Token JWT d'accès (à inclure dans le header Authorization: Bearer <token>)")
    private String accessToken;

    @Schema(description = "Token de rafraîchissement pour obtenir un nouveau access token")
    private String refreshToken;

    @Schema(description = "Type de token (toujours 'Bearer')")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Durée de validité du token en secondes")
    private Long expiresIn;

    @Schema(description = "Informations de l'utilisateur authentifié")
    private UserDto user;
}
