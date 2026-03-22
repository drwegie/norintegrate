# ADR-013: Observability with Actuator, Prometheus, and Grafana

## Status

Accepted

## Context

Production services need monitoring and observability to detect issues, understand performance characteristics, and support operational decision-making. Without metrics and health checks, the team is blind to what is happening inside the running applications.

In previous projects built on older Spring versions, monitoring was often an afterthought -- relying on log files, manual health checks, or proprietary APM tools that introduced vendor lock-in. Spring Boot Actuator existed but was underutilized, and metrics collection required significant custom instrumentation.

Spring Boot 4 with Micrometer provides a mature, vendor-neutral metrics facade that integrates deeply with the framework. JVM metrics, HTTP request metrics, database connection pool metrics, and more are available out of the box with zero custom code. The Prometheus registry is the most widely adopted open-source option for time-series metrics storage, and Grafana is the industry standard for visualization.

Both norintegrate-api and norintegrate-mcp are independently deployed services that need their own health and metrics endpoints. The monitoring infrastructure (Prometheus and Grafana) should be available for local development and production use, but should not interfere with the default development workflow.

## Decision

We will add Spring Boot Actuator with the Micrometer Prometheus registry to both the API and MCP modules. The following Actuator endpoints are exposed: `health`, `info`, `prometheus`, and `metrics`.

In the API module, the Actuator health, info, and prometheus endpoints are permitted without authentication in the Spring Security configuration. The MCP module already permits all requests, so no security change is needed there.

For local development and production-like environments, Prometheus and Grafana run as Docker Compose services under the `monitoring` profile. This means `docker compose up` continues to start only the core services (PostgreSQL, API, MCP), while `docker compose --profile monitoring up` adds Prometheus and Grafana. Prometheus is configured to scrape both services at their `/actuator/prometheus` endpoints. Grafana is pre-provisioned with Prometheus as its default datasource.

## Consequences

**Positive:**

- Standard JVM metrics (memory, GC, threads) and HTTP server metrics (request count, latency, error rates) are available immediately with no custom code.
- Health endpoints enable container orchestrators and load balancers to check service liveness.
- The Micrometer API allows adding custom business metrics in the future without changing the monitoring infrastructure.
- Prometheus and Grafana are open-source with no vendor lock-in -- the same stack works in local development, CI, and production.
- The Docker Compose `monitoring` profile keeps the default development experience unchanged; developers who do not need monitoring are unaffected.

**Negative:**

- The `/actuator/prometheus` endpoint is publicly accessible. In production, this should be restricted at the network level (e.g., only accessible from the Prometheus server's IP) or via a reverse proxy.
- Prometheus uses a pull-based model with static target configuration. In a dynamic scaling environment, service discovery (e.g., via Consul or Kubernetes) would be needed to replace the static targets.
- Grafana dashboards are not provisioned automatically -- they need to be created manually or imported from the Grafana dashboard marketplace. This is intentional to avoid over-engineering at this stage.
