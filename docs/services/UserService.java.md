# UserService

## Service Interface

```java
package com.blog.backend.service;

import com.blog.backend.dto.CreateUserDto;
import com.blog.backend.dto.UpdateUserDto;
import com.blog.backend.dto.UserDto;

import java.util.List;
import java.util.UUID;

public interface UserService {

    /**
     * Get all users
     * @return List of all users
     */
    List<UserDto> getAllUsers();

    /**
     * Get user by ID
     * @param id the user ID
     * @return UserDto if found
     * @throws ResourceNotFoundException if user not found
     */
    UserDto getUserById(UUID id);

    /**
     * Get user by username
     * @param username the username
     * @return UserDto if found
     * @throws ResourceNotFoundException if user not found
     */
    UserDto getUserByUsername(String username);

    /**
     * Create new user
     * @param createUserDto the user data
     * @return Created UserDto
     * @throws DuplicateResourceException if username or email already exists
     */
    UserDto createUser(CreateUserDto createUserDto);

    /**
     * Update user
     * @param id the user ID
     * @param updateUserDto the updated user data
     * @return Updated UserDto
     * @throws ResourceNotFoundException if user not found
     * @throws DuplicateResourceException if new username/email already exists
     */
    UserDto updateUser(UUID id, UpdateUserDto updateUserDto);

    /**
     * Delete user
     * @param id the user ID
     * @throws ResourceNotFoundException if user not found
     */
    void deleteUser(UUID id);
}
```

## Service Implementation

```java
package com.blog.backend.service.impl;

import com.blog.backend.dto.CreateUserDto;
import com.blog.backend.dto.UpdateUserDto;
import com.blog.backend.dto.UserDto;
import com.blog.backend.entity.User;
import com.blog.backend.exception.DuplicateResourceException;
import com.blog.backend.exception.ResourceNotFoundException;
import com.blog.backend.mapper.UserMapper;
import com.blog.backend.repository.UserRepository;
import com.blog.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toDto(user);
    }

    @Override
    public UserDto createUser(CreateUserDto createUserDto) {
        // Check if username already exists
        if (userRepository.existsByUsername(createUserDto.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + createUserDto.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(createUserDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + createUserDto.getEmail());
        }

        // Create new user
        User user = new User();
        user.setUsername(createUserDto.getUsername());
        user.setEmail(createUserDto.getEmail());
        user.setBio(createUserDto.getBio());
        user.setAvatarUrl(createUserDto.getAvatarUrl());

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto updateUser(UUID id, UpdateUserDto updateUserDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update username if provided
        if (updateUserDto.getUsername() != null && !updateUserDto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateUserDto.getUsername())) {
                throw new DuplicateResourceException("Username already exists: " + updateUserDto.getUsername());
            }
            user.setUsername(updateUserDto.getUsername());
        }

        // Update email if provided
        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateUserDto.getEmail())) {
                throw new DuplicateResourceException("Email already exists: " + updateUserDto.getEmail());
            }
            user.setEmail(updateUserDto.getEmail());
        }

        // Update other fields
        if (updateUserDto.getBio() != null) {
            user.setBio(updateUserDto.getBio());
        }
        if (updateUserDto.getAvatarUrl() != null) {
            user.setAvatarUrl(updateUserDto.getAvatarUrl());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
```

## Exception Classes

```java
// ResourceNotFoundException.java
package com.blog.backend.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// DuplicateResourceException.java
package com.blog.backend.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
```

## Notes

- Service layer handles business logic
- Uses `@Transactional` for database transactions
- Validates username and email uniqueness before creation/update
- Throws custom exceptions for better error handling
- Mapper converts between entities and DTOs
- Read operations use `@Transactional(readOnly = true)` for optimization
