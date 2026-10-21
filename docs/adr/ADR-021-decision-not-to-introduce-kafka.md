# ADR-021: Decision Not to Introduce Kafka

**Status:** Accepted
**Date:** 2026-10-21

## Context

Event streaming with Apache Kafka was evaluated as a direction for extending NorIntegrate. Kafka is the de facto backbone for event-driven systems in many Norwegian organisations, so the question was not whether Kafka is a good technology, but whether this system has a problem that Kafka is the answer to.

Kafka earns its operational cost when at least one of these drivers is present:

1. **Integration across independently owned systems** — several producers and consumers that deploy, fail and evolve independently.
2. **A continuous external event source** — data that arrives as a push stream rather than being fetched on request.
3. **Throughput or availability decoupling** — producers must not block on, or fail with, their consumers.
4. **Replay as a requirement** — rebuilding state or adding a new consumer by re-reading history.

We evaluated NorIntegrate against each driver:

- **Independently owned systems**: One team, one product. `norintegrate-api` and `norintegrate-mcp` are separate deployables, but they share a database through `norintegrate-common` and never call each other (ADR-003).
- **External event source**: The only external data source is the SSB Klass API, which is fetched on request and never stored (ADR-010). Nothing pushes data to NorIntegrate.
- **Throughput**: The only user-driven write is `ProgressService.markComplete` / `markIncomplete` — one `user_progress` row per click. No module uses `@Async`, `@Scheduled`, `ApplicationEventPublisher` or `@TransactionalEventListener`; the system has no asynchronous work at all.
- **Replay**: No requirement exists for a historical, replayable record of user progress.

We also evaluated concrete designs, from smallest to largest:

- **Publish a `progress.completed` event and consume it in the same API process.** Rejected: a consumer in the producer's own process does nothing a Spring in-process event listener cannot, without a broker.
- **Use `norintegrate-mcp` as the consumer.** This would have avoided a new deployable, but the MCP server is read-only and unauthenticated (ADR-017). It has no notion of a user, so per-user progress events mean nothing to it.
- **Transactional outbox plus a new worker module that unlocks the next procedures in the dependency graph when a step is completed.** This is the only design with a genuine second process. Rejected: a new deployable brings a Dockerfile, a CI path filter, Kubernetes manifests, an ECS task definition and a Prometheus scrape target. All of that would serve a feature whose result `ChecklistService` already computes at request time with `DependencyResolver`.
- **Running Kafka behind an opt-in Docker Compose profile**, like the existing `monitoring` profile. This lowers the local setup cost, but it does not create a need. An optional broker for an unneeded feature is still an unneeded broker.
- **Event-sourced progress on a compacted topic, with tombstones for erasure.** Rejected as a heavier form of the outbox design, with the same missing driver.

The pattern behind these rejections is that the drivers are properties of system boundaries and data flow, not of features. Adding features to a single-team, request-driven application does not create them.

## Decision

Kafka and other message brokers are not added to NorIntegrate.

If the code ever needs to react to a domain event inside one process, it uses Spring's `ApplicationEventPublisher` with `@TransactionalEventListener`. Background work follows ADR-005: Spring's `@Scheduled` on virtual threads.

## Consequences

- The local stack and the deployment surface stay at three application services plus PostgreSQL. No broker to run, secure, monitor, or upgrade.
- Progress updates remain synchronous and immediately consistent, which matches how users interact with the checklist.
- There is no event history of user progress. This matches the project's data-minimisation stance: no personal information is kept beyond email.
- Revisit this ADR if any of the following appears:
  - a second, independently deployed consumer that holds its own user context;
  - an external source that pushes data to NorIntegrate;
  - a requirement for an auditable or replayable history of user actions;
  - a write volume that no longer fits in single synchronous transactions.
- Together with ADR-005 (Spring Batch) and ADR-014 (ID-porten), this ADR records a technology that was deliberately evaluated and not adopted.
