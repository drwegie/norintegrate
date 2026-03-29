# ADR-004: Oracle to PostgreSQL Migration

**Status:** Accepted
**Date:** 2026-03-14

## Context

The team's prior database experience is with Oracle Database — a feature-rich, commercially licensed RDBMS common in enterprise Java environments. Oracle's JDBC driver, dialect, sequence syntax, and proprietary types (e.g. `VARCHAR2`, `NUMBER`) are familiar.

For NorIntegrate, Oracle was evaluated but rejected on the following grounds:

- **Cost**: Oracle Database licensing is expensive and inappropriate for an open-source or personal project. PostgreSQL 18+ is fully open source under the PostgreSQL License.
- **UUID support**: PostgreSQL provides `gen_random_uuid()` natively (no extension needed in PG 13+), generating RFC 4122 v4 UUIDs. Oracle requires a manual workaround (`SYS_GUID()` returns RAW(16), not a formatted UUID string). NorIntegrate uses UUIDs as the primary key for `app_user`.
- **JSONB**: PostgreSQL's `JSONB` type stores structured JSON with binary indexing, supporting containment and key-exists operators. While not heavily used in the initial schema, it provides a clean extension path without a schema migration.
- **Identity columns**: PostgreSQL 10+ supports `GENERATED ALWAYS AS IDENTITY`, which is the SQL standard equivalent of Oracle's sequences-plus-triggers pattern and is cleaner than `SERIAL`. All bigint PKs in NorIntegrate use this.
- **Ecosystem**: Spring Boot's autoconfiguration, Spring Data JPA, Flyway, and Testcontainers all have first-class PostgreSQL support. The `postgresql` Docker image is the de facto standard for local and CI database containers.
- **Timestamps with timezone**: PostgreSQL's `TIMESTAMPTZ` stores UTC internally and converts on output — exactly the behaviour required for `created_at`/`updated_at` columns.

## Decision

Use PostgreSQL 18+ as the sole database. The JDBC URL, Flyway configuration, and Testcontainers setup all target PostgreSQL. No Oracle-specific SQL syntax is used anywhere.

## Consequences

- Database triggers manage `updated_at` — a pattern portable across PostgreSQL environments.
- `gen_random_uuid()` is called in DDL for `app_user.id`; no application-side UUID generation is needed for inserts.
- The Spring Data JPA dialect is set to `PostgreSQLDialect` (Hibernate 6).
- Developers must have Docker available locally to run the Testcontainers-based integration tests.
