# ADR-005: Decision Not to Use Spring Batch

**Status:** Accepted
**Date:** 2026-03-14

## Context

The team has Spring Batch experience. Spring Batch is the standard Spring solution for chunk-oriented processing, ETL pipelines, and scheduled bulk operations. It provides job repository persistence, retry/skip policies, partitioning, and restart-after-failure semantics — capabilities that are genuinely useful when large volumes of data must be processed reliably.

During design, we evaluated every data operation in NorIntegrate against Spring Batch's use case criteria:

- **Procedure and visa type master data**: This is a small, hand-curated dataset (tens of rows). It is loaded via Flyway seed migrations, not a processing pipeline.
- **SSB Klass API (municipality data)**: Municipality information is fetched on-demand via a direct `RestClient` call to the SSB Klass API. There is no ETL step — data is never stored locally (see ADR-010). A batch job to sync this data would contradict the architectural decision to treat the external API as the authoritative source.
- **User progress**: Written one record at a time in response to user actions. No bulk ingestion exists or is planned.
- **Checklist generation**: Computed in-process by `DependencyResolver` (Kahn's algorithm on the procedure DAG) at request time. No pre-computation pipeline is needed.

No use case in NorIntegrate involves: processing files, transforming large record sets, scheduled bulk updates, or multi-step ETL. Adding Spring Batch would introduce a job repository schema (typically 9 tables), a `JobLauncher` bean, and `Step`/`Tasklet` infrastructure for zero functional benefit.

## Decision

Spring Batch is not added as a dependency. No `norintegrate-batch` module will be created. This decision is explicitly documented to prevent future contributors from adding it speculatively.

## Consequences

- The project dependency tree is smaller and the Spring context starts faster.
- If a genuine batch use case emerges (e.g. bulk import of procedure data from an external source), this ADR should be revisited and a dedicated module created.
- All scheduled or background work, if ever needed, uses Spring's `@Scheduled` with virtual threads rather than Spring Batch infrastructure.
