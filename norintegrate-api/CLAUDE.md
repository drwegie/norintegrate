# CLAUDE.md — norintegrate-api

REST API module for human users. Deployed independently on port 8080.

---

## Package Design (Domain-Based)

Package-by-feature, NOT package-by-layer.

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

---

## API Design

- All endpoints under `/api/v1/`
- Admin endpoints under `/api/v1/admin/`
- Public endpoints: no authentication required
- Progress endpoints: OAuth 2.0 JWT required
- Consistent error format via GlobalExceptionHandler
- Cursor-based pagination where applicable

---

## Security

- OAuth 2.0 JWT resource server (Google provider)
- Stateless sessions (no cookies)
- Actuator endpoints (`/actuator/health`, `/actuator/info`, `/actuator/prometheus`) are public
- CORS allowed origins configured via `norintegrate.cors.allowed-origins` (default: `http://localhost:3000`)

---

## Testing

- `@SpringBootTest(webEnvironment = MOCK)` + Testcontainers for controller integration tests (see ADR-011)
- Singleton PostgreSQL container shared across all test classes via `@DynamicPropertySource`
- `@Transactional` on `AbstractIntegrationTest` — every test rolls back, no manual cleanup needed
- `SecurityMockMvcRequestPostProcessors.jwt()` for authenticated endpoint tests
- Test packages mirror main source packages
