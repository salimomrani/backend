# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **📖 For universal development best practices**, see [BEST_PRACTICE.md](./BEST_PRACTICE.md) - comprehensive guidelines on commits, code quality, testing, security, and more.

## Project Overview

Spring Boot 3.5.7 REST API application using Java 17, demonstrating a feature-based architecture with user management CRUD operations. Uses PostgreSQL database with Docker for local development and AWS RDS for production.

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
- pgAdmin (optional): http://localhost:5050
  - Email: `admin@backend.com`
  - Password: `admin`

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

### Spring Profiles

The application uses Spring profiles to manage different environments:

**Development Profile (`dev`)**:
- PostgreSQL running in Docker container
- **Docker Compose auto-start enabled** - containers start automatically with the application
- Auto-activates by default via `SPRING_PROFILES_ACTIVE=dev`
- Configuration in `application-dev.yml`
- DDL mode: `update` (auto-creates tables from entities)
- SQL logging enabled at DEBUG level

**Production Profile (`prod`)**:
- PostgreSQL on AWS RDS
- Configuration in `application-prod.yml`
- Uses environment variables for sensitive credentials
- DDL mode: `validate` (requires manual migrations)
- Optimized connection pooling with HikariCP
- Reduced logging for performance

### Local Development Setup

#### Option 1: Automatic Docker Compose (Recommended)

Spring Boot démarre automatiquement les containers Docker Compose lors du lancement de l'application avec le profil `dev` :

```bash
# Simplement démarrer l'application - Docker Compose démarre automatiquement!
./mvnw spring-boot:run

# Les containers PostgreSQL et pgAdmin sont démarrés automatiquement
# Ils s'arrêtent aussi automatiquement quand vous arrêtez l'application
```

**Comment ça marche**:
- Spring Boot détecte le fichier `docker-compose.yml` à la racine
- Démarre automatiquement les services PostgreSQL et pgAdmin
- Attend que PostgreSQL soit prêt avant de démarrer l'application
- Arrête les containers quand l'application s'arrête (Ctrl+C)
- Configuration dans `application-dev.yml` sous `spring.docker.compose`

**Désactiver le démarrage automatique**:
Si vous préférez gérer Docker manuellement, modifiez `application-dev.yml`:
```yaml
spring:
  docker:
    compose:
      enabled: false
```

#### Option 2: Gestion Manuelle de Docker Compose

Si vous avez désactivé le démarrage automatique ou préférez le contrôle manuel:

```bash
# Démarrer PostgreSQL et pgAdmin manuellement
docker-compose up -d

# Vérifier que les containers fonctionnent
docker ps

# Voir les logs
docker-compose logs -f postgres

# Arrêter les containers
docker-compose down

# Arrêter et supprimer les volumes (efface la base de données)
docker-compose down -v
```

#### Database Connection Details (Local Docker)
- Host: `localhost`
- Port: `5432`
- Database: `backenddb`
- Username: `backend_user`
- Password: `backend_password`

### Production Deployment (AWS RDS)

1. **Set Environment Variables**:
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   export DB_URL=jdbc:postgresql://your-rds-endpoint.region.rds.amazonaws.com:5432/backenddb
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   ```

2. **Run Application**:
   ```bash
   java -jar target/backend-0.0.1-SNAPSHOT.jar
   ```

### Database Migration Best Practices

- **Development**: Use `ddl-auto: update` for rapid prototyping
- **Production**: Use `ddl-auto: validate` and manage schema with tools like Flyway or Liquibase
- Never use `create-drop` or `create` in production
- Always backup database before manual schema changes

## Testing

The project uses Spring Boot Test. Test files are located in `src/test/java/`.

## Docker

### Building Docker Image Locally
```bash
# Build the Docker image
docker build -t backend:local .

# Run the container
docker run -p 8080:8080 backend:local

# Run with environment variables
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  backend:local
```

### Multi-Stage Dockerfile
The project uses a multi-stage Dockerfile for optimized builds:
- **Stage 1 (build)**: Uses `eclipse-temurin:17-jdk-alpine` to compile and package the application
- **Stage 2 (runtime)**: Uses `eclipse-temurin:17-jre-alpine` with a non-root user for security
- Includes health check on `/actuator/health` endpoint
- Final image exposes port 8080

## CI/CD Pipeline

### GitLab CI Configuration
The project uses GitLab CI with two stages defined in `.gitlab-ci.yml`:

**Build Stage**:
- Runs on every push to `main` and on merge requests
- Uses Maven to compile and test (`./mvnw clean install`)
- Caches Maven dependencies (`.m2/repository/`)
- Generates JUnit test reports
- Artifacts: JAR file and test reports (expires in 1 week)

**Docker Stage** (only on `main` branch):
- Builds Docker image with two tags: `latest` and `main-{SHORT_SHA}`
- Pushes to Docker Hub: `iconsultingdev/blog-backend`
- Requires `DOCKER_USERNAME` and `DOCKER_PASSWORD` variables configured in GitLab
- Set to `allow_failure: true` (won't block pipeline if Docker credentials are missing)

### GitLab Project URLs
- **Repository**: https://gitlab.com/salimomrani1/backend
- **Pipelines**: https://gitlab.com/salimomrani1/backend/-/pipelines
- **Merge Requests**: https://gitlab.com/salimomrani1/backend/-/merge_requests
- **CI/CD Settings**: https://gitlab.com/salimomrani1/backend/-/settings/ci_cd

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
   # Using GitLab CLI (glab)
   glab mr create --title "Add user authentication" --description "Implements JWT-based authentication"

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
