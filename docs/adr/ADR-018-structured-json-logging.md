# ADR-018: Structured JSON Logging

## Status

Accepted

## Context

NorIntegrate's backend modules use Spring Boot's default colourised console logging. This works well during local development but is unsuitable for production deployments where logs are shipped to aggregators (CloudWatch, ELK, Datadog). Text-based logs require custom parsers that are fragile and lose structured context (request IDs, user IDs, error metadata).

The alternative — parsing text logs at the aggregator — shifts complexity to infrastructure and produces lower-quality search results. Structured JSON at the source captures the full context once, at the point of emission.

Coming from traditional Java logging (Log4j 1.x, plain Logback), the migration to structured output requires a deliberate choice about encoding format and when to activate it.

## Decision

Use `logstash-logback-encoder` to emit structured JSON logs in the `prod` profile. Retain the Spring Boot console appender for `default`, `dev`, and `test` profiles.

Each module's `logback-spring.xml` uses Spring profile activation:

- **default / dev / test**: Standard Spring Boot console appender (human-readable, colourised)
- **prod**: `LogstashEncoder` with MDC key propagation (`requestId`, `userId` on the API) and a `customFields` block tagging the application name

This gives developers readable local logs while production gets machine-parseable JSON without application code changes.

## Consequences

### Positive

- Log aggregators receive structured JSON — no custom parsing required
- MDC keys (`requestId`, `userId`) propagate automatically into every log line
- Zero impact on local development — console appender unchanged
- Application name tagged in every log line aids multi-service filtering

### Negative

- Additional runtime dependency (`logstash-logback-encoder`)
- JSON logs are harder to read when tailing raw container output in production
- MDC keys must be set by application code (filters or interceptors) — not included in this ADR
