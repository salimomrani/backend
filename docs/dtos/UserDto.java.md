# User DTOs

## UserDto (Response)

```java
package com.blog.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String bio;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
```

## UserPreviewDto (Response - for article author)

```java
package com.blog.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreviewDto {
    private UUID id;
    private String username;
    private String avatarUrl;
}
```

## CreateUserDto (Request)

```java
package com.blog.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    private String avatarUrl;

    // TODO: Add password field if authentication is needed
    // @NotBlank(message = "Password is required")
    // @Size(min = 8, message = "Password must be at least 8 characters")
    // private String password;
}
```

## UpdateUserDto (Request)

```java
package com.blog.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDto {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    private String avatarUrl;
}
```

## Mapper Example

```java
package com.blog.backend.mapper;

import com.blog.backend.dto.UserDto;
import com.blog.backend.dto.UserPreviewDto;
import com.blog.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getBio(),
            user.getAvatarUrl(),
            user.getCreatedAt()
        );
    }

    public UserPreviewDto toPreviewDto(User user) {
        return new UserPreviewDto(
            user.getId(),
            user.getUsername(),
            user.getAvatarUrl()
        );
    }
}
```

## Notes

- `UserDto` is used for full user information responses
- `UserPreviewDto` is used when embedding user info in articles (lighter payload)
- All fields in Update DTOs are optional (partial updates)
- Validation annotations ensure data integrity
- **TODO:** Add password field and authentication DTOs if needed
