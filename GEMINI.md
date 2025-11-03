# Gemini Project Context: Java Spring Boot Backend

This document provides a comprehensive overview of the project to be used as instructional context for Gemini.

## Project Overview

This is a Java Spring Boot backend application designed to serve as a REST API. It utilizes a standard layered architecture, separating concerns into controllers, services, repositories, and DTOs/entities.

### Key Technologies

*   **Backend Framework:** Spring Boot 3.5.7
*   **Language:** Java 17
*   **Database:** PostgreSQL (for development/production), H2 (for tests)
*   **Data Access:** Spring Data JPA / Hibernate
*   **API Documentation:** SpringDoc OpenAPI (Swagger UI)
*   **Code Helpers:** Lombok, MapStruct
*   **Containerization:** Docker (via Docker Compose)

### Architecture

*   **Controllers:** Handle incoming HTTP requests (e.g., `UserController`).
*   **Services:** Contain the core business logic (e.g., `UserService`).
*   **Repositories:** Manage data persistence using Spring Data JPA (e.g., `UserRepository`).
*   **Entities:** Define the database schema (e.g., `User`).
*   **DTOs:** Data Transfer Objects used for API request/response payloads to decouple the API from the database schema.
*   **Mappers:** MapStruct is used for automatic mapping between Entities and DTOs.
*   **Configuration:** Application settings are managed in `application.yml` with profile-specific overrides (`application-dev.yml`, `application-prod.yml`).

## Building and Running

### Prerequisites

*   Java 17
*   Maven
*   Docker and Docker Compose

### Development Mode

The project is configured to use `spring-boot-docker-compose`, which automatically starts the required PostgreSQL database container when the application is run.

1.  **Run the application:**
    *   Using the Maven wrapper:
        ```bash
        ./mvnw spring-boot:run
        ```
    *   Or from your IDE by running the `BackendApplication` main class.

2.  **Access the API:**
    *   The application will be available at `http://localhost:8080`.

3.  **API Documentation:**
    *   Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

### Building for Production

1.  **Package the application:**
    ```bash
    ./mvnw clean package
    ```
2.  This will create an executable JAR file in the `target/` directory.

### Running Tests

Execute the test suite using Maven:

```bash
./mvnw test
```

## Development Conventions

*   **Layered Architecture:** Adhere to the Controller-Service-Repository pattern.
*   **DTOs:** Use DTOs for all API communication. Do not expose JPA entities directly in controllers.
*   **Mapping:** Use MapStruct for entity-DTO mapping.
*   **Configuration:** Use `.yml` files for configuration. Keep secrets and environment-specific settings out of the main `application.yml`.
*   **Dependencies:** Manage dependencies through the `pom.xml` file.
