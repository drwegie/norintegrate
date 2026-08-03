# CLAUDE.md — norintegrate-mcp

MCP (Model Context Protocol) server for AI agents. Deployed independently on port 8081.

---

## Package Design (Domain-Based)

Package-by-feature, NOT package-by-layer. All main sources are Kotlin (ADR-019).

```
src/main/kotlin/com/norintegrate/mcp
├── tool/            ← IntegrationGuideTool, ProcedureDetailTool, MunicipalitySearchTool
│                       + their record result/DTO types (ProcedureStep, DocumentItem, ...)
├── resource/        ← ProcedureResource, VisaTypeResource
├── config/          ← McpServerConfig, McpSecurityConfig
└── NorIntegrateMcpApplication.kt
```

---

## MCP Server Configuration

- Transport: SSE (Server-Sent Events) via WebMVC
- SSE connection endpoint: `/sse` (Spring AI default, not overridden)
- SSE message (POST) endpoint: `/mcp/messages` (set via `sse-message-endpoint`)
- All requests permitted (no OAuth on MCP server)
- Spring AI MCP server integration (`spring-ai-starter-mcp-server-webmvc`)

---

## Testing

- E2E MCP protocol tests using `spring-ai-starter-mcp-client-webflux`
- Testcontainers with shared PostgreSQL container
- Test packages mirror main source packages
- Unit tests (`src/test/kotlin`) are Kotlin, alongside their Kotlin subjects
- Integration tests (`*IT.java`, `src/test/java`) stay in Java by design — a standing regression
  check that Java call sites still interoperate correctly with the Kotlin-compiled tools and
  record types (ADR-019)
