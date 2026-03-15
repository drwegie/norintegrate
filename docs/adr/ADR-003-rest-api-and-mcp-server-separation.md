# ADR-003: REST API and MCP Server Separation

**Status:** Accepted
**Date:** 2026-03-14

## Context

NorIntegrate serves two distinct client types: human users interacting through a web or mobile frontend (REST API) and AI agents querying via the Model Context Protocol (MCP). Both clients require access to the same domain logic — procedures, visa types, checklists, municipality information, and user progress.

The simplest initial design would be a single deployable application that exposes both REST endpoints and an MCP server on different ports or paths. This monolith approach reduces operational complexity for a single developer and eliminates network latency between components.

However, the two protocols have different characteristics:

- The REST API is request/response HTTP, benefits from standard caching headers, and is secured with OAuth 2.0 JWT tokens issued to end users.
- The MCP server uses a structured tool-call protocol, is consumed by AI agents (not browsers), and has a different authentication surface.
- Spring AI's MCP server support brings in additional dependencies (Spring AI autoconfiguration, tool registration, SSE transport) that are irrelevant to the REST API.

Building both into a single JAR would couple their deployment cycles, mix unrelated dependencies into a single classpath, and make it impossible to scale them independently.

## Decision

Split into three Gradle modules: `norintegrate-common` (shared domain library, never deployed), `norintegrate-api` (REST API, independently deployable), and `norintegrate-mcp` (MCP server, independently deployable). Both deployable modules depend on `norintegrate-common` and never depend on each other.

## Consequences

- Domain logic, entities, services, and repository interfaces live in `norintegrate-common` and are tested once, not duplicated.
- Each deployable module has its own `application.yml`, `Dockerfile`, and GitHub Actions workflow (path-filtered per ADR-009).
- Adding a new domain feature requires a change to `norintegrate-common` plus one or both consumer modules, which is an explicit and visible cost.
- The `norintegrate-api` classpath carries no Spring AI dependencies; the `norintegrate-mcp` classpath carries no OpenAPI/Swagger dependencies.
