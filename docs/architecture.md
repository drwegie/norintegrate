# System Architecture

This document provides a visual overview of NorIntegrate's architecture. For the rationale behind each decision, see the linked ADRs.

## Container Diagram

How the system's containers interact at runtime.

```mermaid
graph LR
    Browser["Browser"]
    AI["AI Agent<br/>(MCP Client)"]

    subgraph NorIntegrate
        Web["norintegrate-web<br/>Next.js 15<br/>:3000"]
        API["norintegrate-api<br/>Spring Boot 4<br/>:8080"]
        MCP["norintegrate-mcp<br/>Spring Boot 4<br/>:8081"]
        DB[("PostgreSQL 18<br/>7 tables")]
    end

    Google["Google OAuth 2.0"]
    SSB["SSB Klass API<br/>(Statistics Norway)"]

    Browser -->|HTTPS| Web
    Web -->|REST + JWT| API
    Browser -->|OAuth 2.0 OIDC| Google
    API -->|JDBC| DB
    MCP -->|JDBC| DB
    MCP -->|HTTPS| SSB
    API -->|HTTPS| SSB
    AI -->|MCP/SSE| MCP
```

See [ADR-003](adr/ADR-003-rest-api-and-mcp-server-separation.md) for the REST/MCP separation rationale and [ADR-017](adr/ADR-017-mcp-server-authentication-posture.md) for MCP security posture.

## Authenticated Request Sequence

How a browser request flows through authentication.

```mermaid
sequenceDiagram
    participant B as Browser
    participant W as norintegrate-web
    participant G as Google OAuth
    participant A as norintegrate-api
    participant DB as PostgreSQL

    B->>W: GET /checklist (unauthenticated)
    W->>A: GET /api/v1/checklist/{visaType}
    A->>DB: SELECT procedures
    DB-->>A: ResultSet
    A-->>W: JSON response
    W-->>B: Rendered page

    Note over B,G: User clicks "Sign in with Google"
    B->>G: OAuth 2.0 Authorization Code flow
    G-->>B: id_token + refresh_token
    B->>W: Callback with tokens
    W-->>B: Session cookie set

    B->>W: POST /api/v1/progress (authenticated)
    W->>A: POST with Authorization: Bearer {id_token}
    A->>A: Validate JWT (issuer, expiry)
    A->>DB: INSERT user_progress
    DB-->>A: OK
    A-->>W: 201 Created
    W-->>B: Progress updated
```

## Operational Topology

Container layout for local development and production.

```mermaid
graph TB
    subgraph "Docker Compose (local)"
        PG["postgres:18-alpine<br/>:5432"]
        API_L["norintegrate-api<br/>:8080"]
        MCP_L["norintegrate-mcp<br/>:8081"]
        WEB_L["norintegrate-web<br/>:3000"]

        subgraph "monitoring profile"
            PROM["prometheus<br/>:9090"]
            GRAF["grafana<br/>:3001"]
        end

        API_L --> PG
        MCP_L --> PG
        WEB_L --> API_L
        PROM --> API_L
        PROM --> MCP_L
        GRAF --> PROM
    end

    subgraph "AWS ECS Fargate (production)"
        ALB["Application Load Balancer"]
        API_P["API Service<br/>(Fargate task)"]
        MCP_P["MCP Service<br/>(Fargate task)"]
        WEB_P["Web Service<br/>(Fargate task)"]
        RDS["RDS PostgreSQL<br/>(sslmode=require)"]

        ALB --> API_P
        ALB --> MCP_P
        ALB --> WEB_P
        API_P --> RDS
        MCP_P --> RDS
    end
```

See [ADR-013](adr/ADR-013-observability-with-actuator-prometheus-grafana.md) for the monitoring stack and [ADR-018](adr/ADR-018-structured-json-logging.md) for production logging.
