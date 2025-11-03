# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **📖 For universal development best practices**, see [BEST_PRACTICE.md](./BEST_PRACTICE.md) - comprehensive guidelines on commits, code quality, testing, security, and more.

## Project Overview

Spring Boot 3.5.7 REST API application using Java 17, demonstrating a feature-based architecture with user management CRUD operations. Uses H2 in-memory database for development.

## Build and Run Commands

### Maven Commands
```bash
# Clean and build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=BackendApplicationTests

# Package without running tests
./mvnw package -DskipTests
```

### Application URLs
- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI docs: http://localhost:8080/api-docs
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:backenddb`
  - Username: `sa`
  - Password: (empty)

## Architecture

### Feature-Based Structure
The codebase follows a **feature-based** organization pattern where each feature is self-contained:

```
src/main/java/com/iconsulting/backend/
├── features/
│   └── user/              # Self-contained user feature
│       ├── controller/    # REST endpoints
│       ├── service/       # Business logic
│       ├── repository/    # Data access (Spring Data JPA)
│       ├── entity/        # JPA entities
│       ├── dto/           # Request/response DTOs
│       └── mapper/        # MapStruct mappers (Entity ↔ DTO)
├── common/
│   ├── dto/              # Shared ApiResponse, PageResponse
│   ├── exception/        # GlobalExceptionHandler, custom exceptions
│   └── constants/        # API constants (paths, pagination defaults)
└── config/               # CORS, OpenAPI configuration
```

### Key Architectural Patterns

**DTO Pattern with MapStruct**:
- Entities (JPA) are separate from DTOs (API layer)
- MapStruct automatically generates mapping code at compile time
- Mappers must be configured in `pom.xml` annotation processor paths (lines 108-120)

**Service Layer Pattern**:
- Service interfaces define contracts
- Service implementations contain business logic with `@Transactional`
- Services use constructor injection via Lombok's `@RequiredArgsConstructor`

**Global Exception Handling**:
- `GlobalExceptionHandler` intercepts all exceptions
- Returns formatted `ErrorResponse` with timestamp, status, message
- Handles validation errors from `@Valid` annotations

**Standard Response Wrapper**:
- `ApiResponse<T>` wraps all successful responses with `success`, `message`, `data`
- Page endpoints return Spring's `Page<T>` directly

### Annotation Processors
The project uses two annotation processors that must be configured together in the Maven compiler plugin:
1. **Lombok** (1.18.30) - Reduces boilerplate with `@Data`, `@RequiredArgsConstructor`, `@Slf4j`
2. **MapStruct** (1.5.5.Final) - Generates type-safe bean mappings
3. prefere to use record when it is possible and use @Builder for complex DTOs

## Adding New Features

To add a new feature (e.g., "product"):

1. Create feature package: `features/product/`
2. Add subdirectories: `entity/`, `dto/`, `repository/`, `service/`, `controller/`, `mapper/`
3. Define JPA entity with Lombok annotations
4. Create Spring Data JPA repository interface
5. Define request/response DTOs
6. Create MapStruct mapper interface (annotated with `@Mapper(componentModel = "spring")`)
7. Implement service interface with business logic
8. Create REST controller using `ApiConstants.API_BASE_PATH` prefix
9. Add Swagger annotations (`@Tag`, `@Operation`) for documentation

## Database Configuration

H2 in-memory database with JPA/Hibernate:
- DDL mode: `update` (auto-creates tables from entities)
- SQL logging enabled at DEBUG level
- Console accessible at `/h2-console` for development

## Testing

The project uses Spring Boot Test. Test files are located in `src/test/java/`.

## Git Workflow - Best Practices

### Branch Strategy

**NEVER push directly to `main` branch.** Always use feature branches and merge requests.

#### Branch Naming Convention
Follow conventional branch naming:
- `feature/` - New features (e.g., `feature/user-authentication`)
- `fix/` - Bug fixes (e.g., `fix/user-validation-error`)
- `refactor/` - Code refactoring (e.g., `refactor/service-layer`)
- `chore/` - Maintenance tasks (e.g., `chore/update-dependencies`)
- `docs/` - Documentation updates (e.g., `docs/api-documentation`)
- `test/` - Test additions/modifications (e.g., `test/user-service-tests`)

#### Workflow Steps

1. **Create a feature branch from `main`**:
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/my-new-feature
   ```

2. **Make your changes and commit**:
   ```bash
   git add .
   git commit -m "feat: add user authentication endpoint"
   ```

3. **Push the branch to GitLab**:
   ```bash
   git push -u origin feature/my-new-feature
   ```

4. **Create a Merge Request (MR)**:
   ```bash
   # Using GitLab CLI
   gh mr create --title "Add user authentication" --description "Implements JWT-based authentication"

   # Or via GitLab Web UI
   # Navigate to: https://gitlab.com/salimomrani1/backend/-/merge_requests/new
   ```

5. **Wait for CI/CD pipeline to pass** before merging

6. **After merge, delete the feature branch**:
   ```bash
   git checkout main
   git pull origin main
   git branch -d feature/my-new-feature
   ```

### Commit Message Convention

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring
- `docs`: Documentation changes
- `test`: Adding or updating tests
- `chore`: Maintenance tasks
- `ci`: CI/CD changes
- `perf`: Performance improvements
- `style`: Code style changes (formatting, no logic change)

**Examples**:
```bash
feat(user): add email verification endpoint
fix(auth): resolve token expiration issue
refactor(service): extract validation logic to separate class
docs(readme): update setup instructions
test(user): add integration tests for user creation
chore(deps): update Spring Boot to 3.5.7
ci(gitlab): add Docker build stage
```

### Protected Branches

The `main` branch should be protected in GitLab:
- ✅ Require merge request approvals (optional but recommended)
- ✅ Require passing CI/CD pipeline
- ✅ No direct pushes allowed
- ✅ No force push allowed

### Merge Request Guidelines

When creating a merge request:
1. **Descriptive title**: Summarize the change clearly
2. **Detailed description**: Explain what, why, and how
3. **Link related issues**: Reference issue numbers (e.g., `Closes #123`)
4. **Self-review**: Review your own changes first
5. **Update tests**: Add/update tests for new functionality
6. **Documentation**: Update relevant documentation
7. **CI passing**: Ensure all pipeline checks pass before requesting review
