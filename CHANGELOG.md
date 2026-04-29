# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- RBAC for admin endpoints — `ROLE_ADMIN` required via configurable email allowlist (P0.1)
- Request body validation on all admin endpoints with structured error responses (P0.2)
- ADR-017: MCP server authentication posture documentation (P0.3)
- Production Spring profile with DB SSL (`sslmode=require`) and HikariCP tuning (P0.5)
- Structured JSON logging via Logstash Logback Encoder for production profile (ADR-018)
- OWASP security headers: HSTS, X-Frame-Options, X-Content-Type-Options, Referrer-Policy (P2.11)
- `PiiMasker` utility in `norintegrate-common` for safe email/subject logging (P2.14)
- Dependabot configuration for Gradle, npm, GitHub Actions, and Docker (P2.12)
- Trivy SCA security scan in CI with SARIF upload to GitHub Security (P2.13)
- System architecture diagrams (container, sequence, operational topology) (P1.8)
- Norwegian (`nb`) i18n with `next-intl` — full landing page + navbar translation (P1.10)
- Accessibility tests with `@axe-core/playwright` for WCAG 2.0 AA compliance (P2.16)
- Explicit Auth.js cookie configuration with `__Secure-`/`__Host-` prefixes in production (P2.15)
- MCP server documentation in README with tool table and usage instructions (P1.6)
- MIT license (P1.9)
- Frontend README with quick-start, scripts, i18n, a11y, and auth sections (P2.18)

### Changed

- Docker Compose: all production secrets now require explicit `.env` values (`${VAR:?error}`) (P0.4)
- Graceful shutdown enabled for both backend services (P0.5)

### Removed

- Hardcoded database credentials and insecure defaults from `docker-compose.yml` (P0.4)
- `GF_SECURITY_ADMIN_PASSWORD: admin` default from Grafana config (P0.4)

## [0.1.0] — Initial scaffolding

### Added

- Java 25 LTS backend with Spring Boot 4 / Spring Framework 7 (ADR-001, ADR-002)
- REST API (`norintegrate-api`) and MCP Server (`norintegrate-mcp`) separation (ADR-003)
- PostgreSQL 18 database with 7-table schema (ADR-004)
- Gradle Kotlin DSL multi-project build (ADR-008)
- Monorepo with path-filtered GitHub Actions CI/CD (ADR-006, ADR-009)
- Repository pattern for data source abstraction (ADR-010)
- Integration testing with Testcontainers (ADR-011)
- Next.js 15 frontend with Auth.js Google OAuth (ADR-012)
- Observability with Actuator, Prometheus, and Grafana (ADR-013)
- Three-layer frontend testing strategy with Playwright (ADR-015)
- AWS ECS Fargate deployment configuration (ADR-016)
- Decision records for Spring Batch exclusion (ADR-005) and ID-porten/BankID exclusion (ADR-014)
