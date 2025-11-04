package com.iconsulting.backend.features.auth.controller;

import com.iconsulting.backend.common.constants.ApiConstants;
import com.iconsulting.backend.common.dto.ApiResponse;
import com.iconsulting.backend.common.exception.ResourceNotFoundException;
import com.iconsulting.backend.features.auth.dto.AuthResponse;
import com.iconsulting.backend.features.auth.dto.LoginRequest;
import com.iconsulting.backend.features.auth.dto.RegisterRequest;
import com.iconsulting.backend.features.auth.service.AuthService;
import com.iconsulting.backend.features.user.dto.UserDto;
import com.iconsulting.backend.features.user.entity.User;
import com.iconsulting.backend.features.user.mapper.UserMapper;
import com.iconsulting.backend.features.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST pour l'authentification
 * Endpoints publics : login, register
 * Endpoints protégés : me, refresh
 */
@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API d'authentification et gestion des tokens JWT")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Inscription d'un nouvel utilisateur
     */
    @PostMapping("/register")
    @Operation(
        summary = "Créer un nouveau compte",
        description = "Inscrit un nouvel utilisateur et retourne un token JWT"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Compte créé avec succès, token JWT généré"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Données invalides ou email déjà utilisé"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse authResponse = authService.register(request);

        ApiResponse<AuthResponse> response = new ApiResponse<>(
            true,
            "User registered successfully",
            authResponse
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Connexion d'un utilisateur existant
     */
    @PostMapping("/login")
    @Operation(
        summary = "Se connecter",
        description = "Authentifie un utilisateur et retourne un token JWT"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Connexion réussie, token JWT généré"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Identifiants incorrects"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);

        ApiResponse<AuthResponse> response = new ApiResponse<>(
            true,
            "Login successful",
            authResponse
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Rafraîchir le token d'accès
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Rafraîchir le token",
        description = "Génère un nouveau access token à partir d'un refresh token valide"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token rafraîchi avec succès"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Refresh token invalide ou expiré"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Parameter(description = "Refresh token JWT")
            @RequestParam String refreshToken) {

        AuthResponse authResponse = authService.refreshToken(refreshToken);

        ApiResponse<AuthResponse> response = new ApiResponse<>(
            true,
            "Token refreshed successfully",
            authResponse
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les informations de l'utilisateur connecté
     */
    @GetMapping("/me")
    @Operation(
        summary = "Obtenir l'utilisateur connecté",
        description = "Retourne les informations de l'utilisateur actuellement authentifié",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Utilisateur trouvé"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Non authentifié"
        )
    })
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        // Get user from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Extract email handling case where principal can be String or UserDetails
        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        // Load user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        UserDto userDto = userMapper.toDto(user);

        ApiResponse<UserDto> response = new ApiResponse<>(
            true,
            "Current user retrieved successfully",
            userDto
        );

        return ResponseEntity.ok(response);
    }

    /**
     * User logout
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Logout",
        description = "Logs out the user and invalidates all existing JWT tokens",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Logout successful"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Not authenticated"
        )
    })
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Get user from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Extract email handling case where principal can be String or UserDetails
        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        // Logout user (updates lastLogout timestamp)
        authService.logout(email);

        ApiResponse<Void> response = new ApiResponse<>(
            true,
            "Logout successful",
            null
        );

        return ResponseEntity.ok(response);
    }
}
