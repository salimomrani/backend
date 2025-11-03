# Implementation Guide

## 📋 Step-by-Step Implementation

### Phase 1: Project Setup

1. **Create Spring Boot Project**
   - Use Spring Initializr (https://start.spring.io/)
   - Select dependencies: Web, JPA, PostgreSQL, Validation, Lombok
   - Java version: 17 or higher
   - Spring Boot: 3.x

2. **Configure Database**
   - Install PostgreSQL (or use Docker)
   - Create database: `blog_db`
   - Configure `application.yml` with database credentials

3. **Project Structure**
```
src/main/java/com/blog/backend/
├── entity/
│   ├── User.java
│   └── Article.java
├── dto/
│   ├── UserDto.java
│   ├── UserPreviewDto.java
│   ├── CreateUserDto.java
│   ├── UpdateUserDto.java
│   ├── ArticleDto.java
│   ├── CreateArticleDto.java
│   └── UpdateArticleDto.java
├── repository/
│   ├── UserRepository.java
│   └── ArticleRepository.java
├── service/
│   ├── UserService.java
│   ├── UserServiceImpl.java
│   ├── ArticleService.java
│   └── ArticleServiceImpl.java
├── controller/
│   ├── UserController.java
│   └── ArticleController.java
├── mapper/
│   ├── UserMapper.java
│   └── ArticleMapper.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── ErrorResponse.java
│   ├── ValidationErrorResponse.java
│   └── GlobalExceptionHandler.java
└── config/
    └── WebConfig.java
```

### Phase 2: Implementation Order

1. ✅ **Entities** (User, Article)
2. ✅ **DTOs** (Request/Response objects)
3. ✅ **Repositories** (JPA interfaces)
4. ✅ **Mappers** (Entity ↔ DTO conversion)
5. ✅ **Services** (Business logic)
6. ✅ **Controllers** (REST endpoints)
7. ✅ **Exception Handling** (Global handler)
8. ✅ **CORS Configuration** (Frontend integration)

### Phase 3: Testing

1. **Manual Testing with Postman/Insomnia**
   - Import API endpoints
   - Test CRUD operations for users
   - Test CRUD operations for articles
   - Test pagination and filtering

2. **Swagger UI Testing** (if springdoc-openapi is added)
   - Access: http://localhost:8080/api/swagger-ui.html
   - Interactive API documentation
   - Test endpoints directly from browser

3. **Integration with Angular Frontend**
   - Start Spring Boot backend
   - Start Angular frontend
   - Test end-to-end functionality

### Phase 4: Enhancements

1. **Unit Tests**
   - Service layer tests
   - Repository tests
   - Controller tests with MockMvc

2. **Database Migrations**
   - Add Flyway or Liquibase
   - Version control database schema

3. **Logging**
   - Configure SLF4J/Logback
   - Add logging to services and controllers

## 🔧 Configuration Files

### application.yml (Development)

```yaml
spring:
  application:
    name: blog-backend

  datasource:
    url: jdbc:postgresql://localhost:5432/blog_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update  # Change to 'validate' in production
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8080
  servlet:
    context-path: /api

logging:
  level:
    com.blog.backend: DEBUG
    org.hibernate.SQL: DEBUG
```

### application-prod.yml (Production)

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate  # Never use 'update' in production
    show-sql: false

logging:
  level:
    com.blog.backend: INFO
```

## ✅ Implementation Checklist

### Core Features
- [ ] User CRUD operations
- [ ] Article CRUD operations
- [ ] Article pagination
- [ ] Filter articles by tag
- [ ] Recent articles endpoint
- [ ] Like article functionality
- [ ] Global exception handling
- [ ] CORS configuration
- [ ] Input validation

### Database
- [ ] PostgreSQL setup
- [ ] Entity relationships configured
- [ ] Indexes on frequently queried columns
- [ ] Database connection pooling

### Testing
- [ ] Manual API testing
- [ ] Integration tests
- [ ] Unit tests for services
- [ ] Frontend integration testing

## 📝 TODOs and Future Enhancements

### Authentication & Authorization
```
TODO: Implement Spring Security
- User registration and login
- JWT token authentication
- Password encryption with BCrypt
- Role-based access control (Admin, Author, Reader)
- Protect endpoints (only authors can edit their articles)
```

### Comment System
```
TODO: Implement Comment entity and functionality
- Create Comment entity (id, content, articleId, authorId, createdAt)
- CommentRepository, CommentService, CommentController
- Endpoints: GET /posts/{slug}/comments, POST /posts/{slug}/comments
- Link comments to articles and users
- Increment commentsCount when comment is added
```

### Like System Enhancement
```
TODO: Track which users liked which articles
- Create Like/Favorite entity (userId, articleId, createdAt)
- Prevent duplicate likes from same user
- Add unlike functionality
- Endpoint: POST /posts/{slug}/unlike
```

### Search Functionality
```
TODO: Implement article search
- Full-text search in title, excerpt, and content
- Search endpoint: GET /posts/search?q={query}
- Consider Elasticsearch for better performance
```

### File Upload
```
TODO: Implement image upload for avatars and article covers
- Configure file storage (local or cloud like S3)
- Upload endpoint: POST /upload
- Validate file types and sizes
- Generate unique filenames
- Return public URL for uploaded files
```

### Pagination Enhancement
```
TODO: Improve pagination responses
- Add pagination metadata to responses
- Implement cursor-based pagination for better performance
- Add sorting options (by likes, comments, date)
```

### Performance Optimization
```
TODO: Optimize database queries
- Add database indexes on frequently queried columns
- Implement caching (Redis) for frequently accessed data
- Use @EntityGraph to optimize lazy loading
- Monitor and optimize N+1 query problems
```

### Monitoring & Logging
```
TODO: Add monitoring and logging
- Spring Boot Actuator for health checks
- Structured logging with correlation IDs
- Log aggregation (ELK stack)
- Application metrics (Prometheus/Grafana)
```

### Docker Deployment
```
TODO: Containerize Spring Boot application
- Create Dockerfile
- Docker Compose for local development (app + database)
- Environment-specific configurations
- Health checks in Docker
```

### CI/CD Integration
```
TODO: Add backend to CI/CD pipeline
- Build and test on every push
- Run unit and integration tests
- Build Docker image
- Deploy to Kubernetes alongside frontend
```

## 🚀 Quick Start Commands

```bash
# Clone/Create Spring Boot project
# ... (from Spring Initializr)

# Run PostgreSQL with Docker
docker run --name blog-postgres \
  -e POSTGRES_DB=blog_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15

# Run Spring Boot application
./mvnw spring-boot:run

# Or with Gradle
./gradlew bootRun

# Access Swagger UI (if configured)
# http://localhost:8080/api/swagger-ui.html

# Test API endpoints
curl http://localhost:8080/api/users
curl http://localhost:8080/api/posts
```

## 📚 Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring REST Docs](https://spring.io/guides/gs/rest-service/)
- [Hibernate ORM](https://hibernate.org/orm/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## ⚠️ Important Notes

1. **Security**: The current specification does NOT include authentication. This is suitable for development but NOT for production.
2. **Database**: Using `ddl-auto: update` is convenient for development but risky for production. Use migrations instead.
3. **CORS**: Current CORS config allows localhost:4200. Update for production domains.
4. **Error Handling**: Global exception handler provides consistent error responses.
5. **Validation**: Input validation is configured with `@Valid` annotations.
6. **UUID vs Long**: This spec uses UUID for IDs. You can switch to Long if preferred.

## 🎯 Next Steps

1. Implement all entities, repositories, services, and controllers
2. Test with Postman or Swagger UI
3. Integrate with Angular frontend
4. Add authentication and authorization
5. Implement comment system
6. Add file upload functionality
7. Deploy to production environment
