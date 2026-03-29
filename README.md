# NorIntegrate

A platform that helps immigrants navigate the settlement process in Norway. Provides a REST API for human users and an MCP server for AI agents. Both share domain logic through a common library module.

This project demonstrates a full migration from Java 8 + Spring 3 to Java 25 + Spring Boot 4. Architecture decisions are documented in [`docs/adr/`](docs/adr/).

---

## Architecture

```
norintegrate/
├── norintegrate-common/   ← Shared domain: entities, services, repositories (not deployed)
├── norintegrate-api/      ← REST API for human users          (port 8080)
├── norintegrate-mcp/      ← MCP server for AI agents          (port 8081)
└── norintegrate-web/      ← Next.js frontend                  (port 3000)
```

Both deployable modules depend on `norintegrate-common` and never depend on each other. A Next.js frontend (`norintegrate-web/`) communicates with the API over HTTP. The database is PostgreSQL 18 — the sole persistence layer.

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java | 25 (LTS) | Temurin distribution recommended |
| Node.js | 22 (LTS) | Required for the frontend (`norintegrate-web/`) |
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

The API will be available at `http://localhost:8080`, the frontend at `http://localhost:3000`, and Swagger UI at `http://localhost:8080/swagger-ui.html`.

On first start, PostgreSQL automatically applies `docs/schema.sql` and `docs/seed.sql` via the `docker-entrypoint-initdb.d` mechanism. To re-initialize with a fresh database:

```bash
docker compose down -v   # removes the data volume
docker compose up        # schema + seed re-applied on fresh volume
```

---

## Local Development (without Docker for the app)

This is the recommended workflow during active development.

### 1. Start the database

```bash
docker compose up postgres -d
```

Schema and seed data are applied automatically on first start (via init volumes in `docker-compose.yml`). To re-apply from scratch, run `docker compose down -v` first.

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

Integration tests require Docker (for Testcontainers). A PostgreSQL 18 container is started automatically and shared across all test classes.

```bash
# Run all tests for the API module
./gradlew :norintegrate-api:test

# Run all tests for the MCP module (includes E2E MCP protocol tests)
./gradlew :norintegrate-mcp:test

# Run all tests across all modules
./gradlew test

# Run with code style check
./gradlew :norintegrate-api:spotlessCheck :norintegrate-api:test

# Generate coverage report for norintegrate-common (≥80% enforced)
./gradlew :norintegrate-common:jacocoTestReport
```

Test reports are written to `norintegrate-api/build/reports/tests/test/index.html`.
Coverage reports (HTML) are at `norintegrate-common/build/reports/jacoco/test/html/index.html`.

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

## Frontend Development

The frontend is a standalone Next.js 15 project in `norintegrate-web/`.

```bash
cd norintegrate-web
npm install
npm run dev
```

The dev server runs at `http://localhost:3000` and expects the API at `http://localhost:8080`.

**Google OAuth:** To enable sign-in, create a Google OAuth 2.0 client and set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and `NEXTAUTH_SECRET` in your `.env` file. See `.env.example` for details.

---

## Monitoring

Prometheus + Grafana are available via a Docker Compose profile:

```bash
docker compose --profile monitoring up
```

| Service | URL | Description |
|---------|-----|-------------|
| Prometheus | `http://localhost:9090` | Metrics collection and querying |
| Grafana | `http://localhost:3001` | Dashboards (login: admin/admin) |
| API metrics | `http://localhost:8080/actuator/prometheus` | Raw Prometheus metrics |
| MCP metrics | `http://localhost:8081/actuator/prometheus` | Raw Prometheus metrics |
| Health check | `http://localhost:8080/actuator/health` | Application health status |

Grafana is pre-configured with Prometheus as the default data source.

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
├── norintegrate-web/            Next.js 15 frontend (Node.js project)
│   ├── app/                     App Router pages and layouts
│   ├── components/              React components
│   └── lib/                     API client and Auth.js config
│
├── docs/
│   ├── adr/                 Architecture Decision Records
│   ├── schema.sql           PostgreSQL DDL
│   ├── api-spec/            OpenAPI specification
│   └── seed.sql             Reference data (3 visa types, 17 procedures)
│
├── docker/
│   ├── api.Dockerfile       Multi-stage build for norintegrate-api
│   ├── mcp.Dockerfile       Multi-stage build for norintegrate-mcp
│   ├── web.Dockerfile       Multi-stage build for norintegrate-web
│   ├── prometheus.yml       Prometheus scrape configuration
│   └── grafana/             Grafana provisioning (datasources)
│
├── .github/workflows/
│   ├── api.yml              Path-filtered CI for norintegrate-api
│   ├── common.yml           Path-filtered CI for norintegrate-common (+ coverage)
│   ├── docker.yml           Docker build verification
│   ├── mcp.yml              Path-filtered CI for norintegrate-mcp
│   └── web.yml              Path-filtered CI for norintegrate-web
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
| `GOOGLE_CLIENT_ID` | — | For frontend | Google OAuth 2.0 client ID |
| `GOOGLE_CLIENT_SECRET` | — | For frontend | Google OAuth 2.0 client secret |
| `NEXTAUTH_SECRET` | — | For frontend | Auth.js session secret (`openssl rand -base64 32`) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | No | Comma-separated list of allowed CORS origins |

Copy `.env.example` to `.env` and fill in `JWT_ISSUER_URI` before running. For frontend OAuth, also set the `GOOGLE_*` and `NEXTAUTH_SECRET` variables.

---

## CI/CD

Each module has a dedicated GitHub Actions workflow triggered only when its relevant files change. See ADR-006 (GitHub Actions) and ADR-009 (monorepo + path filtering) for the rationale.

| Workflow | Triggers on changes to |
|----------|----------------------|
| `api.yml` | `norintegrate-api/`, `norintegrate-common/`, root `build.gradle.kts` |
| `common.yml` | `norintegrate-common/`, root `build.gradle.kts`, `settings.gradle.kts` |
| `docker.yml` | `docker/`, `docker-compose.yml`, `**/build.gradle.kts`, `**/src/**` |
| `mcp.yml` | `norintegrate-mcp/`, `norintegrate-common/`, root `build.gradle.kts` |
| `web.yml` | `norintegrate-web/` |

Each module workflow runs `spotlessCheck` and `build` (which includes all tests) on `ubuntu-latest` with Java 25 (Temurin). The `common.yml` workflow also generates and uploads a JaCoCo coverage report. The `docker.yml` workflow verifies Docker images build successfully.

---

## Architecture Decision Records

| ADR | Decision |
|-----|---------|
| [ADR-001](docs/adr/ADR-001-java-8-to-java-25-lts-migration.md) | Java 8 → Java 25 LTS |
| [ADR-002](docs/adr/ADR-002-spring-boot-3-to-spring-boot-4.md) | Spring Boot 3 → Spring Boot 4 |
| [ADR-003](docs/adr/ADR-003-rest-api-and-mcp-server-separation.md) | REST API and MCP server as separate modules |
| [ADR-004](docs/adr/ADR-004-oracle-to-postgresql-migration.md) | Oracle → PostgreSQL 18 |
| [ADR-005](docs/adr/ADR-005-decision-not-to-use-spring-batch.md) | No Spring Batch |
| [ADR-006](docs/adr/ADR-006-jenkins-to-github-actions-cicd-migration.md) | Jenkins → GitHub Actions |
| [ADR-007](docs/adr/ADR-007-terraform-infrastructure-strategy.md) | Terraform for infrastructure (plan-only) |
| [ADR-008](docs/adr/ADR-008-gradle-kotlin-dsl-adoption.md) | Gradle Kotlin DSL |
| [ADR-009](docs/adr/ADR-009-monorepo-with-path-filtered-cicd.md) | Monorepo with path-filtered CI |
| [ADR-010](docs/adr/ADR-010-repository-pattern-for-data-source-abstraction.md) | Repository pattern for SSB Klass API |
| [ADR-011](docs/adr/ADR-011-integration-testing-strategy.md) | Integration testing with Testcontainers |
| [ADR-012](docs/adr/ADR-012-frontend-technology-choice.md) | Next.js with Auth.js for frontend |
| [ADR-013](docs/adr/ADR-013-observability-with-actuator-prometheus-grafana.md) | Observability with Actuator + Prometheus + Grafana |
| [ADR-014](docs/adr/ADR-014-decision-not-to-integrate-idporten.md) | No ID-porten/BankID (requires Digdir registration) |
