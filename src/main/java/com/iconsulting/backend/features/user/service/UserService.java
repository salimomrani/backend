package com.iconsulting.backend.features.user.service;

import com.iconsulting.backend.features.user.dto.CreateUserRequest;
import com.iconsulting.backend.features.user.dto.UpdateUserRequest;
import com.iconsulting.backend.features.user.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface du service User définissant les opérations métier
 */
public interface UserService {

    /**
     * Crée un nouvel utilisateur
     */
    UserDto createUser(CreateUserRequest createUserRequest);

    /**
     * Récupère un utilisateur par son ID
     */
    UserDto getUserById(Long id);

    /**
     * Récupère tous les utilisateurs (paginé)
     */
    Page<UserDto> getAllUsers(Pageable pageable);

    /**
     * Met à jour un utilisateur
     */
    UserDto updateUser(Long id, UpdateUserRequest updateUserRequest);

    /**
     * Supprime un utilisateur
     */
    void deleteUser(Long id);
}
