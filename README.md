# NorIntegrate

A platform that helps immigrants navigate the settlement process in Norway. Provides a REST API for human users and an MCP server for AI agents. Both share domain logic through a common library module.

This project demonstrates a full migration from Java 8 + Spring 3 to Java 25 + Spring Boot 4. Architecture decisions are documented in [`docs/adr/`](docs/adr/).

---

## Architecture

```
norintegrate/
├── norintegrate-common/   ← Shared domain: entities, services, repositories (not deployed)
├── norintegrate-api/      ← REST API for human users          (port 8080)
└── norintegrate-mcp/      ← MCP server for AI agents          (port 8081)
```

Both deployable modules depend on `norintegrate-common` and never depend on each other. The database is PostgreSQL 16 — the sole persistence layer.

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java | 25 (LTS) | Temurin distribution recommended |
| Docker | any recent | Required for local Postgres and integration tests |
| Gradle | 9.4.0 | Provided by the wrapper — no install needed |

**Install Java 25 on macOS:**
```bash
# Homebrew
brew install --cask temurin@25

# SDKMAN
sdk install java 25-tem
```

---

## Quick Start (Docker Compose)

The fastest way to run the full stack locally:

```bash
git clone <repo-url> norintegrate
cd norintegrate

# Copy and configure environment
cp .env.example .env
# Edit .env — set JWT_ISSUER_URI to your OAuth provider issuer URL
# e.g. https://accounts.google.com or https://token.actions.githubusercontent.com

# Start everything (Postgres + API + MCP)
docker-compose up
```

The API will be available at `http://localhost:8080`. Swagger UI is at `http://localhost:8080/swagger-ui.html`.

> **First run note:** Docker Compose does not apply the database schema automatically. See [Database Setup](#database-setup) below.

---

## Local Development (without Docker for the app)

This is the recommended workflow during active development.

### 1. Start the database

```bash
docker-compose up postgres -d
```

### 2. Apply the database schema

```bash
psql -h localhost -U norintegrate -d norintegrate -f docs/schema.sql
psql -h localhost -U norintegrate -d norintegrate -f docs/seed.sql
```

Default credentials: `norintegrate / norintegrate` (as configured in `docker-compose.yml`).

### 3. Configure environment

```bash
cp .env.example .env
# Edit .env and set JWT_ISSUER_URI
```

The application reads `DB_USERNAME`, `DB_PASSWORD`, and `JWT_ISSUER_URI` from environment variables (with defaults for the first two):

```yaml
# application.yml defaults
spring.datasource.username: ${DB_USERNAME:norintegrate}
spring.datasource.password: ${DB_PASSWORD:norintegrate}
spring.security.oauth2.resourceserver.jwt.issuer-uri: ${JWT_ISSUER_URI}
```

### 4. Run the API

```bash
export $(cat .env | xargs)
./gradlew :norintegrate-api:bootRun
```

---

## Database Setup

The schema is managed manually via SQL files in `docs/`. The application uses `ddl-auto: validate` in production — it will refuse to start if the schema does not match the JPA entities.

| File | Purpose |
|------|---------|
| `docs/schema.sql` | Creates all 7 tables, triggers, and constraints |
| `docs/seed.sql` | Inserts 3 visa types (SKILLED_WORKER, FAMILY_REUNIFICATION, STUDENT) with 17 procedures |

Apply to any target database:
```bash
psql -h <host> -U <user> -d <dbname> -f docs/schema.sql
psql -h <host> -U <user> -d <dbname> -f docs/seed.sql
```

---

## Running Tests

Integration tests require Docker (for Testcontainers). A PostgreSQL 16 container is started automatically and shared across all test classes.

```bash
# Run all tests for the API module
./gradlew :norintegrate-api:test

# Run all tests across all modules
./gradlew test

# Run with code style check
./gradlew :norintegrate-api:spotlessCheck :norintegrate-api:test
```

Test reports are written to `norintegrate-api/build/reports/tests/test/index.html`.

---

## Code Style

Code is formatted with [Google Java Format](https://github.com/google/google-java-format) enforced via [Spotless](https://github.com/diffplug/spotless):

```bash
# Check formatting
./gradlew spotlessCheck

# Apply formatting
./gradlew spotlessApply
```

---

## Project Structure

```
norintegrate/
├── norintegrate-common/
│   └── src/main/java/com/norintegrate/common/
│       ├── checklist/       ChecklistTemplate, ChecklistService, DependencyResolver
│       ├── procedure/       Procedure, ProcedureDependency, DocumentRequirement, ProcedureService
│       ├── visa/            VisaType, VisaTypeService
│       ├── progress/        AppUser, UserProgress, ProgressService
│       └── municipality/    MunicipalityInfo, SsbKlassClient, MunicipalityService
│
├── norintegrate-api/
│   └── src/main/java/com/norintegrate/api/
│       ├── checklist/       ChecklistController
│       ├── procedure/       ProcedureController, ProcedureAdminController
│       ├── visa/            VisaTypeController
│       ├── progress/        ProgressController, AccountController
│       ├── municipality/    MunicipalityController
│       ├── config/          SecurityConfig, WebConfig, OpenApiConfig
│       └── exception/       GlobalExceptionHandler
│
├── norintegrate-mcp/
│   └── src/main/java/com/norintegrate/mcp/
│       ├── tool/            Integration guide, procedure detail, municipality search tools
│       └── config/          MCP server configuration
│
├── docs/
│   ├── adr/                 Architecture Decision Records
│   ├── schema.sql           PostgreSQL DDL
│   ├── api-spec/            OpenAPI specification
│   └── seed.sql             Reference data (3 visa types, 17 procedures)
│
├── docker/
│   ├── api.Dockerfile       Multi-stage build for norintegrate-api
│   └── mcp.Dockerfile       Multi-stage build for norintegrate-mcp
│
├── .github/workflows/
│   ├── api.yml              Path-filtered CI for norintegrate-api
│   ├── common.yml           Path-filtered CI for norintegrate-common
│   └── mcp.yml              Path-filtered CI for norintegrate-mcp
│
└── docker-compose.yml       Local development stack
```

---

## API Reference

### Public endpoints — no authentication required

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/visa-types` | List all visa types |
| `GET` | `/api/v1/visa-types/{id}` | Get a single visa type |
| `GET` | `/api/v1/procedures` | List all settlement procedures |
| `GET` | `/api/v1/procedures/{id}` | Get a procedure with details |
| `GET` | `/api/v1/procedures/{id}/documents` | List required documents for a procedure |
| `GET` | `/api/v1/checklist/{visaTypeId}` | Get ordered checklist with dependency resolution |
| `GET` | `/api/v1/checklist/{visaTypeId}?completed=1,2,3` | Checklist with completed procedures excluded |
| `GET` | `/api/v1/municipalities?query=Oslo` | Search municipalities via SSB Klass API |
| `GET` | `/api/v1/municipalities/{code}` | Look up a municipality by code |

### Protected endpoints — OAuth 2.0 JWT required

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/progress` | Get current user's procedure progress |
| `POST` | `/api/v1/progress/{procedureId}/complete` | Mark a procedure complete |
| `DELETE` | `/api/v1/progress/{procedureId}/complete` | Mark a procedure incomplete |
| `DELETE` | `/api/v1/account` | Delete account and all progress |

### Admin endpoints — authentication required

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/admin/procedures` | Create a procedure |
| `PUT` | `/api/v1/admin/procedures/{id}` | Update a procedure |
| `DELETE` | `/api/v1/admin/procedures/{id}` | Delete a procedure |
| `POST` | `/api/v1/admin/procedures/{id}/dependencies` | Add a prerequisite relationship |
| `DELETE` | `/api/v1/admin/procedures/{prerequisiteId}/dependencies/{dependentId}` | Remove a prerequisite |

Full interactive documentation is available via Swagger UI at `/swagger-ui.html` when the API is running.

---

## Environment Variables

| Variable | Default | Required | Description |
|----------|---------|----------|-------------|
| `JWT_ISSUER_URI` | — | Yes | OAuth 2.0 issuer URL (e.g. `https://accounts.google.com`) |
| `DB_USERNAME` | `norintegrate` | No | PostgreSQL username |
| `DB_PASSWORD` | `norintegrate` | No | PostgreSQL password |

Copy `.env.example` to `.env` and fill in `JWT_ISSUER_URI` before running.

---

## CI/CD

Each module has a dedicated GitHub Actions workflow triggered only when its relevant files change. See ADR-006 (GitHub Actions) and ADR-009 (monorepo + path filtering) for the rationale.

| Workflow | Triggers on changes to |
|----------|----------------------|
| `api.yml` | `norintegrate-api/`, `norintegrate-common/`, root `build.gradle.kts` |
| `common.yml` | `norintegrate-common/`, root `build.gradle.kts`, `settings.gradle.kts` |
| `mcp.yml` | `norintegrate-mcp/`, `norintegrate-common/`, root `build.gradle.kts` |

Each workflow runs `spotlessCheck` and `build` (which includes all tests) on `ubuntu-latest` with Java 25 (Temurin).

---

## Architecture Decision Records

| ADR | Decision |
|-----|---------|
| [ADR-001](docs/adr/ADR-001-java-8-to-java-25-lts-migration.md) | Java 8 → Java 25 LTS |
| [ADR-002](docs/adr/ADR-002-spring-boot-3-to-spring-boot-4.md) | Spring Boot 3 → Spring Boot 4 |
| [ADR-003](docs/adr/ADR-003-rest-api-and-mcp-server-separation.md) | REST API and MCP server as separate modules |
| [ADR-004](docs/adr/ADR-004-oracle-to-postgresql-migration.md) | Oracle → PostgreSQL 16 |
| [ADR-005](docs/adr/ADR-005-decision-not-to-use-spring-batch.md) | No Spring Batch |
| [ADR-006](docs/adr/ADR-006-jenkins-to-github-actions-cicd-migration.md) | Jenkins → GitHub Actions |
| [ADR-007](docs/adr/ADR-007-terraform-infrastructure-strategy.md) | Terraform for infrastructure (plan-only) |
| [ADR-008](docs/adr/ADR-008-gradle-kotlin-dsl-adoption.md) | Gradle Kotlin DSL |
| [ADR-009](docs/adr/ADR-009-monorepo-with-path-filtered-cicd.md) | Monorepo with path-filtered CI |
| [ADR-010](docs/adr/ADR-010-repository-pattern-for-data-source-abstraction.md) | Repository pattern for SSB Klass API |
| [ADR-011](docs/adr/ADR-011-integration-testing-strategy.md) | Integration testing with Testcontainers |
| [ADR-012](docs/adr/ADR-012-infrastructure-repository-separation.md) | Infrastructure in separate repository |
