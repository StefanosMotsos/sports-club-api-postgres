# Sports Club REST App(Postgres)

## Overview
`sports-club-rest-postgres` is a Spring Boot REST API for managing a Sports Club registry. It uses:
* MySQL + Flyway database migrations
* Spring Security with JWT Bearer authentication
* Spring Data JPA for persistence
* OpenAPI/Swagger UI for API documentation

## Tech Stack
* Language: Java 21
* Framework: Spring Boot `3.5.11`
* Build tool: Gradle `8.14.4` (wrapper)
* Persistence: Spring Data JPA (Hibernate)
* Migrations: Flyway
* Security: Spring Security + JWT (`io.jsonwebtoken:jjwt-*`)
* API docs: springdoc OpenAPI starter (`springdoc-openapi-starter-webmvc-ui`)

## Requirements
* Java 21+
* MySQL server
* A database/schema matching the Flyway migrations in `src/main/resources/db/migration`
* A writable working directory (used for logs and uploads; file uploads default to `uploads/`)

## Setup
1. Create a MySQL database:
   * Default: `sportsappdb`
   * Or set `MYSQL_DB` to another name
2. Start the application; Flyway migrations will run automatically on startup (`spring.flyway.enabled=true`).

## Configuration / Environment Variables
The app reads MySQL connection settings from environment variables (with defaults shown):
* `MYSQL_HOST` (default: `localhost`)
* `MYSQL_PORT` (default: `3306`)
* `MYSQL_DB` (default: `sportsappdb` in `dev`, `schoolapp9csrpro` in `staging`)
* `MYSQL_USER` (default: `user9csr`)
* `MYSQL_PASSWORD` (default: `12345`)

Other notable configuration in `src/main/resources`:
* `app.security.secret-key` (JWT signing key) and `app.security.jwt-expiration`
* `allowed.origins` (CORS)
* `file.upload.dir` (upload destination; default: `uploads/`)
* `spring.servlet.multipart.max-file-size` and `spring.servlet.multipart.max-request-size` (default: `5MB`)

You can also pass Spring properties directly at runtime, e.g. `--spring.profiles.active=prod`.

Environment variable names follow Spring Boot's relaxed binding, e.g.:
* `APP_SECURITY_SECRET_KEY` -> `app.security.secret-key`
* `APP_SECURITY_JWT_EXPIRATION` -> `app.security.jwt-expiration`
* `ALLOWED_ORIGINS` -> `allowed.origins`
* `FILE_UPLOAD_DIR` -> `file.upload.dir`

### Profile note (`dev` vs `prod`/`staging`)
`dev` is the default profile (`spring.profiles.active=dev` in `application.properties`) and it includes the required security/CORS/upload settings.
`prod`/`staging` only define DB-related properties; if you run them, make sure you also provide:
* `app.security.secret-key` / `app.security.jwt-expiration` (JWT)
* `allowed.origins` (CORS)
* `file.upload.dir` (file uploads)
* `spring.servlet.multipart.max-*` (optional, but defaults to `5MB` in `dev`)

## Setup / Run Commands
### Run in development (default profile: `dev`)
Windows (Gradle wrapper):
```powershell
.\gradlew.bat bootRun
```

### Run with a different Spring profile
Note: `prod`/`staging` profiles don't include security/CORS/upload properties. If you run with them, also provide:
* `app.security.secret-key`
* `app.security.jwt-expiration`
* `allowed.origins`
* `file.upload.dir`

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=prod"
```

### Build a runnable jar and start it
```powershell
.\gradlew.bat bootJar
java -jar .\build\libs\*.jar --spring.profiles.active=dev
```

### Run tests
```powershell
.\gradlew.bat test
```

## Gradle Scripts (Useful Commands)
* `.\gradlew.bat bootRun` - start the API
* `.\gradlew.bat test` - run unit/integration tests
* `.\gradlew.bat build` - full build
* `.\gradlew.bat bootJar` - build an executable jar
* `.\gradlew.bat clean` - clean build artifacts

## Entry Points
* Spring Boot main class: `club.sportsapp.SportsAppApplication` (`src/main/java/.../SportsAppApplication.java`)
* Startup runner: `club.sportsapp.runner.ReportingResultsRunner`
  * Supports optional args:
    * `--generate-report` (generates an in-memory report job)
    * `--get-status` (queries the in-memory job status)

## REST API Entry Points
Base URL (default): `http://localhost:8080`

### API documentation
* Swagger UI: `GET /swagger-ui.html` (also typically `/swagger-ui/index.html`)
* OpenAPI spec: `GET /v3/api-docs/**`

### Authentication (JWT)
* `POST /api/v1/auth/authenticate`
  * Request body: `AuthenticationRequestDTO` (fields: `username`, `password`)
  * Response: `AuthenticationResponseDTO` (JWT token)
* Protected endpoints require header:
  * `Authorization: Bearer <token>`

### Users
* `POST /api/v1/users` - register user (public)
* `GET /api/v1/users/{uuid}` - get user by UUID (requires authority `VIEW_USER`)

### Members
Public (no JWT required per security config):
* `POST /api/v1/members` - create a member
* `POST /api/v1/members/{uuid}/membership-file` - upload membership document (`multipart/form-data` with form field `file`)

Protected (JWT required):
* `PUT /api/v1/members/{uuid}` - update member (`EDIT_MEMBER`)
* `DELETE /api/v1/members/{uuid}` - soft-delete (`DELETE_MEMBER`)
* `GET /api/v1/members/{uuid}` - get member by UUID (`VIEW_MEMBER` or `VIEW_ONLY_MEMBER`)
* `GET /api/v1/members` - list members with pagination + filters (`VIEW_MEMBERS`)
  * Filters are bound from query params into `MemberFilters` (e.g. `vat`, `membershipId`, `lastname`, `deleted`, `sport`)

### Reports (test / async job)
These endpoints are permitted without JWT (`/api/v1/reports/**`):
* `POST /api/v1/reports`
  * Starts an async in-memory job and returns `202 Accepted` with `{ "jobId": "..." }`
* `GET /api/v1/reports/{jobId}`
  * Returns `JobStatusDTO` or `404` if unknown

## File Uploads
* Upload endpoint: `POST /api/v1/members/{uuid}/membership-file`
* Request: `multipart/form-data` with `file` part
* Max file size: `5MB` (default)
* Stored under: `file.upload.dir` (default `uploads/`)

## Database / Migrations
Flyway migration scripts are in:
* `src/main/resources/db/migration/V*.sql`

Migrations include:
* Initial schema (roles/capabilities/users/sports/attachments/personal_information/members)
* Seed data for sports and roles/capabilities
* Inserts for the `VIEW_USER` capability and assignment to `ADMIN`
* Adds member activity status (`ACTIVE`/`SUSPENDED`/`INACTIVE`)
* Adds membership types (`BASIC`/`EXTRA`/`PREMIUM`) and `members.membership_type_id` foreign key

## Tests
* Test framework: JUnit 5
* Test(s) present:
  * `SportAppApplicationTests` (`@SpringBootTest`) - verifies the Spring application context loads

Run with:
```powershell
.\gradlew.bat test
```

## Project Structure
* `src/main/java/club/sportsapp/`
  * `api/` - REST controllers
  * `authentication/` - auth services + JWT utilities
  * `security/` - Spring Security configuration + JWT filter
  * `service/` - business services
  * `repository/` - JPA repositories
  * `model/` - entities + static reference models
  * `dto/` - request/response DTOs
  * `validator/` - input validation helpers
  * `core/` - shared concerns (errors, logging, OpenAPI config)
  * `runner/` - command-line runner used for report generation/testing
* `src/main/resources/`
  * `application*.properties` - Spring configuration for `dev`, `prod`, `staging`
  * `db/migration/` - Flyway migration scripts
* `src/test/java/` - application context load tests

## License
* Repository does not contain a top-level `LICENSE*`/`COPYING*` file (none found in the working tree).
* `gradlew` / `gradlew.bat` include an Apache License 2.0 header.
* OpenAPI metadata (`OpenApiConfig`) sets the API documentation license to `CC0 1.0 Universal`.
