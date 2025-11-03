# UserController

## REST Controller

```java
package com.blog.backend.controller;

import com.blog.backend.dto.CreateUserDto;
import com.blog.backend.dto.UpdateUserDto;
import com.blog.backend.dto.UserDto;
import com.blog.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users
     * Get all users
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/{id}
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/users/username/{username}
     * Get user by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    /**
     * POST /api/users
     * Create new user
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        UserDto createdUser = userService.createUser(createUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * PUT /api/users/{id}
     * Update user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserDto updateUserDto) {
        UserDto updatedUser = userService.updateUser(id, updateUserDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * DELETE /api/users/{id}
     * Delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

## API Endpoints Summary

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/users` | Get all users | - | `List<UserDto>` |
| GET | `/api/users/{id}` | Get user by ID | - | `UserDto` |
| GET | `/api/users/username/{username}` | Get user by username | - | `UserDto` |
| POST | `/api/users` | Create new user | `CreateUserDto` | `UserDto` (201) |
| PUT | `/api/users/{id}` | Update user | `UpdateUserDto` | `UserDto` |
| DELETE | `/api/users/{id}` | Delete user | - | 204 No Content |

## Example Requests

### Create User
```json
POST /api/users
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "bio": "Software developer and blogger",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

### Update User
```json
PUT /api/users/550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json

{
  "bio": "Updated bio text",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

## Notes

- All endpoints are prefixed with `/api` from `application.yml` context-path
- `@CrossOrigin` enables CORS for Angular frontend (localhost:4200)
- `@Valid` annotation triggers validation on request bodies
- Returns appropriate HTTP status codes (200, 201, 204, 404, 409)
- UUID is used for user IDs (matches frontend model)
