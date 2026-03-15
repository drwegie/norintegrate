# ADR-011: Integration Testing Strategy

**Status:** Accepted
**Date:** 2026-03-15

## Context

`norintegrate-api` is a Spring Boot 4 application with a security layer, JPA repositories, and service logic that spans multiple classes. The question was how to test it.

Two testing strategies were considered for the controller layer:

**Unit tests with `@WebMvcTest`** load only the web layer (controllers, filters, serialisation) and mock every service and repository. They are fast and isolated, but they do not verify that the application context wires correctly with real JPA, real security configuration, or a real database. A `@WebMvcTest` can pass while a production deployment fails on a misconfigured datasource, a missing bean, or a Hibernate query that references a column that does not exist in the schema.

**Integration tests with `@SpringBootTest`** load the full application context. Combined with a real PostgreSQL instance (via Testcontainers), they verify the entire stack — HTTP request handling, security filter chain, Spring Security JWT processing, service transactions, JPA entity mapping, and schema correctness — in a single test run.

Given that this project demonstrates production-grade engineering practices and the domain logic involves non-trivial flows (the checklist dependency resolver, OAuth user provisioning, transactional progress tracking), integration tests were chosen for the controller layer.

## Decision

Use `@SpringBootTest(webEnvironment = MOCK)` with `@AutoConfigureMockMvc` and a real PostgreSQL container (Testcontainers) for all controller tests in `norintegrate-api`. The following specific decisions were made:

### Singleton container pattern

Each test class that extends `AbstractIntegrationTest` shares a single PostgreSQL container, started once via a `static` initializer block. The container's JDBC URL is registered with `@DynamicPropertySource` so all Spring contexts receive the correct connection properties.

This pattern was chosen over `@Testcontainers` + `@Container` + `@ServiceConnection` on a base class. The annotation-driven approach starts a new container per test class, but Spring Test caches application contexts across classes. The result is a cached context still configured to connect to a container that has since been stopped — every request times out after the Hikari connection pool timeout (30 seconds), producing 500 errors in every test.

### Single shared application context

`@MockitoBean SsbKlassClient` is declared in `AbstractIntegrationTest` (not per test class). This ensures all test classes produce the same Spring context configuration and the context is created only once per test run. Tests that do not need municipality data leave the mock unconfigured (Mockito returns empty/null), which has no effect since no other test class calls municipality endpoints.

Had `@MockitoBean` remained on `MunicipalityControllerIT` alone, Spring Test would create a second context variant. That second context would re-run `spring.sql.init`, which fails because the tables already exist from the first context's initialisation.

### Transactional test isolation

`@Transactional` is declared on `AbstractIntegrationTest`. Each test method runs in a transaction that is rolled back after the test completes. This gives full isolation between tests without resetting the database: every test sees the seed data in its original state, and mutations from one test (e.g. the admin controller creating or updating a procedure) do not affect subsequent tests.

This works because `WebEnvironment.MOCK` dispatches requests synchronously in the test thread, so the MockMvc call joins the test's open transaction. The service's own `@Transactional` methods participate in the existing transaction rather than creating a new one.

### JWT mocking

Spring Security Test's `SecurityMockMvcRequestPostProcessors.jwt()` injects a pre-built `Authentication` object directly into the security context, bypassing the `JwtDecoder` entirely. This is used for all protected endpoint tests.

A `@Primary JwtDecoder` bean is provided in `TestSecurityConfig` to prevent Spring Boot's auto-configuration from attempting to fetch the OIDC discovery document at `spring.security.oauth2.resourceserver.jwt.issuer-uri` during context startup. Without it, the context would fail to start if the issuer URI is unreachable from the test environment.

### Schema and seed data

`spring.sql.init` applies `db/schema.sql` and `db/seed.sql` (in `src/test/resources/`) when the test application context starts. The statement separator is `;;` to accommodate trigger function bodies that contain internal semicolons.

The seed data uses `INSERT ... OVERRIDING SYSTEM VALUE` to insert procedures with explicit IDs (1–11). Because `GENERATED ALWAYS AS IDENTITY` sequences are not advanced by `OVERRIDING SYSTEM VALUE` inserts, the sequence would generate ID=1 on the first application-driven insert — colliding with the seed data. A `SELECT setval(...)` call at the end of `seed.sql` advances the sequence past the highest seeded ID.

## Consequences

- The full stack is verified on every test run: HTTP routing, security, transactions, entity mapping, and schema correctness are all exercised.
- Integration tests run in roughly the same time as the first container startup (~10–15 seconds), then as fast as the database can handle the queries (~5 seconds for 29 tests), because the container and context are shared.
- Tests require Docker. The CI runner (GitHub Actions `ubuntu-latest`) has Docker pre-installed. Local runs require Docker Desktop or OrbStack.
- The `@Transactional` approach relies on `WebEnvironment.MOCK`. If the web environment were changed to `RANDOM_PORT`, requests would cross a thread boundary and the transaction would not propagate — each test would commit its changes and leave the database dirty for subsequent tests.
