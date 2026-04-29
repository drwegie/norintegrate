# ADR-017: MCP Server Authentication Posture

## Status

Accepted

## Context

The NorIntegrate MCP server exposes three tools to AI agents via the Model Context Protocol:

- `getIntegrationGuide` — returns a checklist of settlement procedures for a visa type
- `getProcedureDetail` — returns details for a single procedure
- `searchMunicipality` — queries the SSB Klass API for Norwegian municipalities

All three tools are **read-only** against **public reference data** (procedures, visa types, SSB Klass municipalities). No tool path accesses the `app_user` or `user_progress` tables — there is no way for a caller to read, modify, or enumerate user-scoped data. Every call is idempotent.

The question is whether to require application-level authentication (API key, OAuth, or mTLS) on the MCP server or to delegate access control to the infrastructure layer.

Coming from enterprise Java backgrounds (Spring Security on every service), the instinct is to add authentication at the application level. However, the data surface here is genuinely public — the same information is available unauthenticated via the REST API's `GET /api/v1/procedures/**` and `GET /api/v1/checklist/**` endpoints.

## Decision

**Permit all requests at the application level. Enforce access control at the infrastructure layer (private subnet and/or API gateway).**

The MCP server runs in a private subnet within the ECS Fargate deployment (ADR-016). Only the API gateway or internal services can reach it. This is the standard defense-in-depth posture for internal microservices that serve public data.

### Revisit trigger

Add application-level authentication when **any** of these conditions become true:

1. A tool is added that accesses user-scoped data (`app_user`, `user_progress`)
2. A tool performs write operations (create, update, delete)
3. The MCP server is exposed directly to the internet without a gateway
4. Rate limiting or per-client quotas are required at the application layer

## Consequences

### Positive

- Zero authentication overhead on MCP tool calls — lower latency for AI agent interactions
- Simpler deployment — no token provisioning or key rotation for MCP clients
- Consistent with the REST API posture where the same data is already public
- Infrastructure-level controls (security groups, ALB rules) are easier to audit than scattered API keys

### Negative

- If infrastructure misconfiguration exposes the MCP server publicly, all tools are accessible — but the data is public anyway
- No per-client attribution at the application layer (logging shows source IP only, not client identity)
- Adding authentication later requires coordinating with all MCP clients
