package com.iconsulting.backend.features.user.controller;

import com.iconsulting.backend.common.constants.ApiConstants;
import com.iconsulting.backend.common.dto.ApiResponse;
import com.iconsulting.backend.features.user.dto.CreateUserRequest;
import com.iconsulting.backend.features.user.dto.UpdateUserRequest;
import com.iconsulting.backend.features.user.dto.UserDto;
import com.iconsulting.backend.features.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST pour la gestion des utilisateurs
 */
@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API de gestion des utilisateurs")
public class UserController {

    private final UserService userService;

    /**
     * Crée un nouvel utilisateur
     */
    @PostMapping
    @Operation(summary = "Créer un utilisateur", description = "Crée un nouvel utilisateur dans le système")
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody CreateUserRequest createUserRequest) {

        UserDto userDto = userService.createUser(createUserRequest);

        ApiResponse<UserDto> response = new ApiResponse<>(
                true,
                ApiConstants.CREATED_SUCCESSFULLY,
                userDto
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Récupère un utilisateur par son ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur", description = "Récupère un utilisateur par son ID")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long id) {

        UserDto userDto = userService.getUserById(id);

        ApiResponse<UserDto> response = new ApiResponse<>(
                true,
                "User retrieved successfully",
                userDto
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère tous les utilisateurs (paginé)
     */
    @GetMapping
    @Operation(summary = "Lister les utilisateurs", description = "Récupère tous les utilisateurs avec pagination")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER) int page,
            @Parameter(description = "Taille de page") @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_BY) String sortBy,
            @Parameter(description = "Direction de tri") @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_DIRECTION) String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDto> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(users);
    }

    /**
     * Met à jour un utilisateur
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur", description = "Met à jour les informations d'un utilisateur")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {

        UserDto userDto = userService.updateUser(id, updateUserRequest);

        ApiResponse<UserDto> response = new ApiResponse<>(
                true,
                ApiConstants.UPDATED_SUCCESSFULLY,
                userDto
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Supprime un utilisateur
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur du système")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long id) {

        userService.deleteUser(id);

        ApiResponse<Void> response = new ApiResponse<>(
                true,
                ApiConstants.DELETED_SUCCESSFULLY
        );

        return ResponseEntity.ok(response);
    }
}
