# Backend Architecture

## 🏗️ Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Angular Frontend                         │
│              http://localhost:4200                           │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP REST API
                     │ (JSON)
┌────────────────────▼────────────────────────────────────────┐
│                  Spring Boot Backend                         │
│              http://localhost:8080/api                       │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐   │
│  │          Controllers (REST Layer)                    │   │
│  │  - UserController                                    │   │
│  │  - ArticleController                                 │   │
│  │  (@RestController, @RequestMapping)                  │   │
│  └────────────────────┬────────────────────────────────┘   │
│                       │                                       │
│  ┌────────────────────▼────────────────────────────────┐   │
│  │          Services (Business Logic)                   │   │
│  │  - UserService / UserServiceImpl                     │   │
│  │  - ArticleService / ArticleServiceImpl               │   │
│  │  (@Service, @Transactional)                          │   │
│  └────────────────────┬────────────────────────────────┘   │
│                       │                                       │
│  ┌────────────────────▼────────────────────────────────┐   │
│  │          Repositories (Data Access)                  │   │
│  │  - UserRepository (extends JpaRepository)            │   │
│  │  - ArticleRepository (extends JpaRepository)         │   │
│  │  (@Repository)                                       │   │
│  └────────────────────┬────────────────────────────────┘   │
│                       │                                       │
└───────────────────────┼───────────────────────────────────────┘
                        │ JPA/Hibernate
┌───────────────────────▼───────────────────────────────────────┐
│                  PostgreSQL Database                          │
│                  localhost:5432/blog_db                       │
│                                                               │
│  Tables:                                                      │
│  - users                                                      │
│  - articles                                                   │
│  - article_tags                                               │
└───────────────────────────────────────────────────────────────┘
```

## 📦 Component Diagram

```
Frontend (Angular)
    │
    │ HTTP Requests
    │ (GET, POST, PUT, DELETE)
    │
    ▼
Controllers
    │
    │ Call service methods
    │ Validate requests (@Valid)
    │
    ▼
Services
    │
    │ Business logic
    │ Transaction management
    │
    ▼
Repositories
    │
    │ CRUD operations
    │ Custom queries
    │
    ▼
Database (PostgreSQL)
```

## 🔄 Request/Response Flow

### Example: Create Article

```
1. Angular Frontend
   POST /api/posts
   Body: CreateArticleDto
        │
        ▼
2. ArticleController
   @PostMapping
   Validates @Valid CreateArticleDto
        │
        ▼
3. ArticleService
   - Validate author exists
   - Create Article entity
   - Save to database
        │
        ▼
4. ArticleRepository
   save(article)
        │
        ▼
5. PostgreSQL Database
   INSERT INTO articles...
        │
        ▼
6. ArticleMapper
   Entity → ArticleDto
        │
        ▼
7. Response
   201 CREATED
   Body: ArticleDto
```

## 🗂️ Data Flow

```
HTTP Request (JSON)
    ↓
Controller receives DTO
    ↓
Service validates and processes
    ↓
Service converts DTO → Entity
    ↓
Repository saves Entity
    ↓
Database persists data
    ↓
Repository returns Entity
    ↓
Service converts Entity → DTO
    ↓
Controller returns DTO
    ↓
HTTP Response (JSON)
```

## 🎯 Design Patterns Used

### 1. Layered Architecture
- **Controller Layer**: HTTP endpoints and request/response handling
- **Service Layer**: Business logic and transactions
- **Repository Layer**: Data access and persistence
- **Entity Layer**: Database models

### 2. Data Transfer Object (DTO) Pattern
- Separate DTOs for requests and responses
- Decouples API contract from database structure
- `CreateArticleDto`, `UpdateArticleDto`, `ArticleDto`

### 3. Repository Pattern
- Abstraction over data access
- Spring Data JPA provides implementation
- Custom query methods when needed

### 4. Dependency Injection
- Constructor injection with `@RequiredArgsConstructor` (Lombok)
- Loose coupling between components
- Easy to test and mock

### 5. Exception Handling
- Global exception handler (`@RestControllerAdvice`)
- Consistent error responses
- Custom exceptions (`ResourceNotFoundException`, `DuplicateResourceException`)

## 🔐 Security Considerations (TODO)

```
TODO: Current implementation has NO authentication

Recommended security layers:
┌─────────────────────────────────────┐
│  1. JWT Token Authentication        │
│     - Token in Authorization header │
│     - Verify token on each request  │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  2. Spring Security Filter Chain    │
│     - Authentication filter         │
│     - Authorization filter          │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  3. Role-Based Access Control       │
│     - ROLE_ADMIN                    │
│     - ROLE_AUTHOR                   │
│     - ROLE_READER                   │
└─────────────────────────────────────┘
```

## 🗃️ Database Schema

```sql
┌─────────────────────────┐
│        users            │
├─────────────────────────┤
│ id (UUID) PK            │
│ username (VARCHAR)      │
│ email (VARCHAR)         │
│ bio (TEXT)              │
│ avatar_url (VARCHAR)    │
│ created_at (TIMESTAMP)  │
└────────────┬────────────┘
             │
             │ 1
             │
             │ *
┌────────────▼────────────┐       ┌─────────────────────────┐
│       articles          │       │     article_tags        │
├─────────────────────────┤       ├─────────────────────────┤
│ id (UUID) PK            │   ┌───│ article_id (UUID) FK    │
│ slug (VARCHAR) UNIQUE   │───┘   │ tag (VARCHAR)           │
│ title (VARCHAR)         │       └─────────────────────────┘
│ excerpt (VARCHAR)       │
│ content_markdown (TEXT) │
│ cover_image_url (VARC.) │
│ author_id (UUID) FK     │
│ likes (INTEGER)         │
│ comments_count (INTEGER)│
│ created_at (TIMESTAMP)  │
│ updated_at (TIMESTAMP)  │
└─────────────────────────┘
```

## 📊 API Endpoint Structure

```
/api
├── /users
│   ├── GET    /                    # List all users
│   ├── GET    /{id}                # Get user by ID
│   ├── GET    /username/{username} # Get user by username
│   ├── POST   /                    # Create user
│   ├── PUT    /{id}                # Update user
│   └── DELETE /{id}                # Delete user
│
└── /posts
    ├── GET    /                    # List articles (paginated)
    ├── GET    /recent              # Get recent articles
    ├── GET    /{slug}              # Get article by slug
    ├── POST   /                    # Create article
    ├── PUT    /{slug}              # Update article
    ├── DELETE /{slug}              # Delete article
    └── POST   /{slug}/like         # Like article
```

## 🔧 Technology Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x |
| Language | Java 17+ |
| ORM | Hibernate / Spring Data JPA |
| Database | PostgreSQL 15 |
| Build Tool | Maven / Gradle |
| Validation | Jakarta Validation |
| Documentation | SpringDoc OpenAPI (optional) |
| Utilities | Lombok |

## 🚀 Deployment Architecture

```
Development:
┌──────────────┐    ┌──────────────┐
│   Angular    │────│ Spring Boot  │
│  localhost   │    │  localhost   │
│    :4200     │    │    :8080     │
└──────────────┘    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │  PostgreSQL  │
                    │  localhost   │
                    │    :5432     │
                    └──────────────┘

Production (Kubernetes):
┌─────────────────────────────────────┐
│         Kubernetes Cluster          │
│                                     │
│  ┌──────────────┐  ┌─────────────┐ │
│  │   Frontend   │  │   Backend   │ │
│  │   Pods       │  │   Pods      │ │
│  │ (Angular)    │  │ (Spring)    │ │
│  └──────────────┘  └──────┬──────┘ │
│                           │         │
│                    ┌──────▼──────┐  │
│                    │  PostgreSQL │  │
│                    │  Service    │  │
│                    └─────────────┘  │
└─────────────────────────────────────┘
         │
         │ Ingress (SSL/TLS)
         │
┌────────▼──────────┐
│   Load Balancer   │
│ blog.kubevpro.... │
└───────────────────┘
```

## 📝 Notes

- Clean separation of concerns
- Stateless REST API
- DTOs for input/output
- Entities for database mapping
- Exception handling at controller advice level
- CORS configured for frontend integration
- Pagination support for list endpoints
