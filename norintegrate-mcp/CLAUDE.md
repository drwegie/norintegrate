# CLAUDE.md — norintegrate-mcp

MCP (Model Context Protocol) server for AI agents. Deployed independently on port 8081.

---

## Package Design (Domain-Based)

Package-by-feature, NOT package-by-layer.

```
com.norintegrate.mcp
├── tool/            ← IntegrationGuideTool, ProcedureDetailTool, MunicipalitySearchTool
├── config/          ← McpServerConfig, McpSecurityConfig
└── NorintegrateMcpApplication.java
```

---

## MCP Server Configuration

- Transport: SSE (Server-Sent Events) via WebMVC
- SSE endpoint: `/mcp/messages`
- All requests permitted (no OAuth on MCP server)
- Spring AI MCP server integration (`spring-ai-starter-mcp-server-webmvc`)

---

## Testing

- E2E MCP protocol tests using `spring-ai-starter-mcp-client-webflux`
- Testcontainers with shared PostgreSQL container
- Test packages mirror main source packages
