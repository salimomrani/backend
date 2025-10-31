package com.iconsulting.exercice1.features.user.service;

import com.iconsulting.exercice1.common.exception.BadRequestException;
import com.iconsulting.exercice1.common.exception.ResourceNotFoundException;
import com.iconsulting.exercice1.features.user.dto.CreateUserRequest;
import com.iconsulting.exercice1.features.user.dto.UpdateUserRequest;
import com.iconsulting.exercice1.features.user.dto.UserDto;
import com.iconsulting.exercice1.features.user.entity.User;
import com.iconsulting.exercice1.features.user.mapper.UserMapper;
import com.iconsulting.exercice1.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service User
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto createUser(CreateUserRequest createUserRequest) {
        log.info("Creating new user with email: {}", createUserRequest.email());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(createUserRequest.email())) {
            throw new BadRequestException("Email already exists: " + createUserRequest.email());
        }

        // Mapper le DTO vers l'entité
        User user = userMapper.toEntity(createUserRequest);

        // Hasher le mot de passe avant la persistance
        String hashedPassword = passwordEncoder.encode(createUserRequest.password());
        user.setPassword(hashedPassword);

        log.debug("Password hashed successfully for user: {}", createUserRequest.email());

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toDto);
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequest updateUserRequest) {
        log.info("Updating user with ID: {}", id);

        // Récupérer l'utilisateur existant
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (updateUserRequest.email() != null &&
            !updateUserRequest.email().equals(user.getEmail()) &&
            userRepository.existsByEmail(updateUserRequest.email())) {
            throw new BadRequestException("Email already exists: " + updateUserRequest.email());
        }

        // Mapper les modifications
        userMapper.updateEntityFromDto(updateUserRequest, user);

        // Sauvegarder les modifications
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully with ID: {}", updatedUser.getId());
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }
}
