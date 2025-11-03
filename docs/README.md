# Backend API Specifications - Spring Boot

This directory contains the specifications for the Spring Boot backend API that serves the Angular blog application.

## 📋 Overview

**Base URL:** `http://localhost:8080/api`

**Tech Stack:**
- Spring Boot 3.x
- Spring Data JPA
- Spring Web
- Spring Validation
- PostgreSQL / MySQL (database)
- Lombok (optional, for reducing boilerplate)

## 📁 Structure

```
specs-back/
├── entities/          # JPA Entity specifications
│   ├── User.java.md
│   └── Article.java.md
├── dtos/             # Data Transfer Objects
│   ├── UserDto.java.md
│   ├── CreateArticleDto.java.md
│   └── UpdateArticleDto.java.md
├── controllers/      # REST Controllers
│   ├── UserController.java.md
│   └── ArticleController.java.md
├── services/         # Service layer interfaces
│   ├── UserService.java.md
│   └── ArticleService.java.md
└── repositories/     # JPA Repositories
    ├── UserRepository.java.md
    └── ArticleRepository.java.md
```

## 🔗 API Endpoints

### Users API
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Articles API
- `GET /api/posts` - Get all articles (with pagination)
- `GET /api/posts/{slug}` - Get article by slug
- `POST /api/posts` - Create new article
- `PUT /api/posts/{slug}` - Update article
- `DELETE /api/posts/{slug}` - Delete article
- `GET /api/posts?tags={tag}` - Filter articles by tag

## 📦 Required Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok (optional) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- OpenAPI/Swagger (optional) -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
</dependencies>
```

## ⚙️ Configuration (application.yml)

```yaml
spring:
  application:
    name: blog-backend

  datasource:
    url: jdbc:postgresql://localhost:5432/blog_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  # CORS Configuration
  web:
    cors:
      allowed-origins: "http://localhost:4200"
      allowed-methods: "*"
      allowed-headers: "*"
      allow-credentials: true

server:
  port: 8080
  servlet:
    context-path: /api

# Swagger UI (if using springdoc-openapi)
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

## 🚀 Getting Started

1. Create Spring Boot project with dependencies listed above
2. Implement entities from `specs-back/entities/`
3. Implement DTOs from `specs-back/dtos/`
4. Implement repositories from `specs-back/repositories/`
5. Implement services from `specs-back/services/`
6. Implement controllers from `specs-back/controllers/`
7. Configure CORS and database connection
8. Run and test with Swagger UI at `http://localhost:8080/api/swagger-ui.html`

## 📝 Notes

- All timestamps are in ISO 8601 format (e.g., `2024-01-15T10:30:00Z`)
- Use `slug` for articles URL-friendly identifier
- Implement proper validation with `@Valid` annotation
- Use `@RestControllerAdvice` for global exception handling
- Consider implementing pagination with `Pageable` for list endpoints
