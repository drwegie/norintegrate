# CLAUDE.md — NorIntegrate Project Rules

This file defines the rules and design decisions for the NorIntegrate project.
Claude Code must follow these rules when generating or modifying code.

---

## Project Overview

NorIntegrate is a platform that helps immigrants navigate the settlement process in Norway.
It provides a REST API for human users and an MCP server for AI agents.
Both share domain logic through a common module.

---

## Technology Stack (Non-Negotiable)

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 25 (LTS) | Use records, pattern matching, text blocks, var |
| Spring Boot | 4.0.x | Spring Framework 7 base |
| Spring AI | Latest stable | MCP server integration |
| Spring Security | OAuth 2.0 | Google + GitHub providers |
| PostgreSQL | 16+ | Only database |
| Gradle | Kotlin DSL | Multi-project build |
| Docker | Multi-stage | One Dockerfile per deployable module |
| GitHub Actions | Path-filtered | Separate workflows per module |
| Terraform | 1.x | Plan-only (not deployed) |

---

## Module Structure

```
norintegrate/
├── norintegrate-common/    ← Shared domain (library, not deployed)
├── norintegrate-api/       ← REST API (deployed independently)
├── norintegrate-mcp/       ← MCP Server (deployed independently)
├── infra/                  ← Terraform
├── k8s/                    ← Kubernetes manifests
├── docker/                 ← Dockerfiles
├── docs/
│   ├── adr/                ← Architecture Decision Records (English)
│   └── api-spec/           ← OpenAPI YAML
├── .github/workflows/
├── build.gradle.kts
├── settings.gradle.kts
├── CLAUDE.md               ← This file
└── README.md               ← English
```

### Dependency Rules

- `norintegrate-common` depends on NOTHING in this project
- `norintegrate-api` depends on `norintegrate-common`
- `norintegrate-mcp` depends on `norintegrate-common`
- `norintegrate-api` and `norintegrate-mcp` NEVER depend on each other

---

## Package Design (Domain-Based)

All modules use package-by-feature, NOT package-by-layer.

### norintegrate-common

```
com.norintegrate.common
├── checklist/       ← ChecklistTemplate, ChecklistService, DependencyResolver
├── procedure/       ← Procedure, ProcedureDependency, DocumentRequirement, ProcedureService
├── visa/            ← VisaType, VisaTypeService
├── progress/        ← AppUser, UserProgress, ProgressService
└── municipality/    ← MunicipalityInfo (record), SsbKlassClient, MunicipalityService
```

### norintegrate-api

```
com.norintegrate.api
├── checklist/       ← ChecklistController, ChecklistResponse
├── procedure/       ← ProcedureController, ProcedureAdminController, DTOs
├── visa/            ← VisaTypeController, VisaTypeResponse
├── progress/        ← ProgressController, AccountController, DTOs
├── municipality/    ← MunicipalityController, MunicipalityResponse
├── config/          ← SecurityConfig, OpenApiConfig, WebConfig
├── exception/       ← GlobalExceptionHandler, custom exceptions
└── NorintegateApiApplication.java
```

### norintegrate-mcp

```
com.norintegrate.mcp
├── tool/            ← IntegrationGuideTool, ProcedureDetailTool, MunicipalitySearchTool
├── config/          ← McpServerConfig, McpSecurityConfig
└── NorintegrateMcpApplication.java
```

---

## Database (PostgreSQL)

7 tables total. Schema file: `docs/schema.sql`

| Table | Purpose |
|-------|---------|
| visa_type | Master data: visa categories (PK is VARCHAR, e.g. 'SKILLED_WORKER') |
| procedure | Settlement procedures (e.g. 'Get D-nummer') |
| procedure_dependency | DAG edges: prerequisite → dependent |
| document_requirement | Required documents per procedure |
| checklist_template | Visa type × procedure mapping with display order |
| app_user | Minimal OAuth user (UUID, provider, subject, email only) |
| user_progress | Per-user procedure completion tracking |

### Database Rules

- All timestamps are TIMESTAMPTZ
- updated_at uses database trigger (never set in application code)
- procedure_dependency has CHECK constraint preventing self-dependency
- user_progress has UNIQUE(user_id, procedure_id)
- app_user has UNIQUE(oauth_provider, oauth_subject)
- Use GENERATED ALWAYS AS IDENTITY for bigint PKs
- Use gen_random_uuid() for app_user PK

---

## Code Style Rules

### Java

- DTOs are ALWAYS Java records (never classes with getters/setters)
- Entities are JPA @Entity classes (records cannot be JPA entities)
- Use `var` when the type is obvious from the right side
- Use pattern matching in switch where applicable
- Use text blocks for multi-line strings
- No Lombok — Java records and IDE generation are sufficient
- No Spring Batch — documented in ADR-005 as unnecessary for this scope

### API Design

- All endpoints under `/api/v1/`
- Admin endpoints under `/api/v1/admin/`
- Public endpoints: no authentication required
- Progress endpoints: OAuth 2.0 JWT required
- Consistent error format via GlobalExceptionHandler
- Cursor-based pagination where applicable

### External API Calls

- SSB Klass API: call directly via RestClient, NO caching, NO local storage
- Never store data that is available from a stable external API

### Testing

- Unit tests for all services in common module
- `@SpringBootTest(webEnvironment = MOCK)` + Testcontainers for controller integration tests (see ADR-011)
- Singleton PostgreSQL container shared across all test classes via `@DynamicPropertySource`
- `@Transactional` on `AbstractIntegrationTest` — every test rolls back, no manual cleanup needed
- `SecurityMockMvcRequestPostProcessors.jwt()` for authenticated endpoint tests
- Test packages mirror main source packages
- Target: ≥ 80% coverage on common module

---

## What NOT to Do

- Do NOT add Spring Batch (ADR-005)
- Do NOT cache SSB Klass API responses
- Do NOT store municipality data in PostgreSQL
- Do NOT add job vacancy features
- Do NOT use Lombok
- Do NOT use package-by-layer structure
- Do NOT store passwords or personal information beyond email
- Do NOT create a norintegrate-batch module
- Do NOT use WidthType.PERCENTAGE in any docx generation

---

## Key Algorithm: DependencyResolver

Implements Kahn's algorithm for topological sort on the procedure DAG.

Input: visa type, optional set of completed procedure IDs
Output: ordered list of remaining procedures, with next recommended step(s)

Must detect and reject cyclic dependencies with CyclicDependencyException.

---

## Architecture Decision Records

All ADRs live in `docs/adr/`. Use Michael Nygard's template (Status, Context, Decision, Consequences). Write in English. Explain the migration journey — WHY the new choice was made over the familiar one, not just WHAT was chosen.

| ADR | Title | Status |
|-----|-------|--------|
| ADR-001 | Java 8 to Java 25 LTS Migration | Done |
| ADR-002 | Spring 3 to Spring Boot 4 / Framework 7 | Done |
| ADR-003 | REST API and MCP Server Separation | Done |
| ADR-004 | Oracle to PostgreSQL Migration | Done |
| ADR-005 | Decision Not to Use Spring Batch | Done |
| ADR-006 | Jenkins to GitHub Actions CI/CD Migration | Done |
| ADR-007 | Terraform Infrastructure Strategy | Done |
| ADR-008 | Gradle Kotlin DSL Adoption | Done |
| ADR-009 | Monorepo with Path-Filtered CI/CD | Done |
| ADR-010 | Repository Pattern for Data Source Abstraction | Done |
| ADR-011 | Integration Testing Strategy | Done |

When a new architectural decision is made, add a row here and create the file in `docs/adr/` before marking the task complete.

---

## Repository Hygiene (Mandatory Final Step)

Before marking ANY task complete, perform a repository hygiene pass. This is not optional — it is the last step of every task.

### Checklist

**1. Remove temporary and unused files**
- Delete any scratch files, debug scripts, or one-off utilities created during the task
- Remove commented-out dead code that was never meant to stay
- If a file no longer has a referencing import or caller, delete it

**2. Verify directory structure**
- Confirm new source files are in the correct package (package-by-feature, not package-by-layer)
- Confirm test files mirror the main source package they test
- No files should land in the project root unless they belong there (build files, README, CLAUDE.md, .gitignore, docker-compose.yml)

**3. Update README.md if setup or usage changed**
Update `README.md` when any of the following changed:
- Prerequisites or tool versions
- How to start the application locally
- How to apply the database schema or seed data
- New environment variables
- New or changed API endpoints (add/update the endpoint table)
- New modules or significant structural changes

Do NOT rewrite the README for internal refactors that have no user-visible effect.

**4. Update ADRs for architectural decisions**
Write a new ADR (next number in sequence) when the task involved a decision that is:
- Non-obvious and could be revisited later
- A trade-off between two real alternatives
- A deviation from what the team's prior experience would suggest

Add the new ADR to the table in the **Architecture Decision Records** section above and create the file in `docs/adr/`.

Do NOT write an ADR for implementation details, bug fixes, or straightforward feature additions.

**5. Verify the repository can be set up from scratch**
After any change that touches the schema, seed data, environment variables, or startup sequence, mentally walk through the README Quick Start and Local Development steps. If a new step is now required that isn't documented, add it.

### What this step is NOT
- Not a full code review
- Not a refactor pass
- Not a chance to add features
- Not required for trivial changes (typo fixes, comment updates)

---

## Context: Developer Profile

The developer has 15 years of Java experience (primarily Java 8) and 10 years of Spring experience (primarily Spring 3 / early Spring Boot). This project demonstrates migration to modern Java 25 + Spring Boot 4. ADRs should reflect this migration journey — explaining WHY the new technology was chosen over the familiar one, not just WHAT was chosen.
