# ADR-010: Repository Pattern for Data Source Abstraction

**Status:** Accepted
**Date:** 2026-03-14

## Context

NorIntegrate's domain data comes from two sources: a local PostgreSQL database (procedures, visa types, checklists, users, progress) and an external HTTP API — the SSB Klass API — which provides official Norwegian municipality information.

In the team's Spring 3 / Java 8 experience, external API calls were often wrapped in a `@Service` and mixed with database access in the same service layer, or municipality data was fetched once and replicated into a local table to avoid network calls at runtime.

Both patterns were considered and rejected:

- **Storing municipality data locally**: The SSB Klass API is maintained by Statistics Norway (SSB), is publicly accessible, and is authoritative. Duplicating it locally creates a synchronisation problem — the local copy can become stale — and introduces an ETL pipeline (which ADR-005 establishes has no place in this project). The municipality table would be read-only data that exists only because of a distrust of the external API, not because of a genuine domain reason.
- **Mixing HTTP client calls with JPA repositories in a service**: This obscures where data comes from, makes testing harder (different mocking strategies for HTTP vs JPA), and couples the service to both persistence and network concerns.

The solution is to apply the Repository pattern uniformly: all data access, regardless of source, is expressed through a domain-facing interface. `SsbKlassClient` is not a "service" — it is a repository-style component that satisfies a data access contract. It is called directly via Spring's `RestClient` and returns domain records. Its interface is defined in `norintegrate-common`; the HTTP implementation is a detail.

## Decision

All data access in `norintegrate-common` is expressed through repository interfaces or dedicated client classes (`SsbKlassClient`). SSB Klass API responses are never stored in PostgreSQL. `SsbKlassClient` calls the external API on every request with no local caching.

## Consequences

- Municipality data is always current; there is no cache invalidation or sync job to maintain.
- Services in `norintegrate-common` are testable with straightforward mock implementations of the repository/client interfaces.
- Network latency for SSB Klass API calls is accepted as a trade-off for data freshness and architectural simplicity. The API is low-latency and publicly hosted by a Norwegian government agency.
- If the SSB Klass API is unavailable, municipality-dependent endpoints will fail. This is an acceptable operational risk given the API's reliability track record.
