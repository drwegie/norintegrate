# ADR-001: Java 8 to Java 25 LTS Migration

**Status:** Accepted
**Date:** 2026-03-14

## Context

The team has 15 years of Java experience centred on Java 8. Java 8 was a landmark release — lambdas, streams, and Optional transformed idiomatic Java — and many mature enterprise codebases have remained on it. However, Java 8 reached end of free commercial support years ago, and the language has advanced substantially since then.

Java 25 is the next LTS after Java 21. Jumping directly from Java 8 to Java 25 means skipping several interim releases, but all the language improvements from Java 9–25 are available at once, and the long-term support horizon justifies a single migration rather than two (8→17, then 17→25).

Key language features that motivated this decision:

- **Records** eliminate the boilerplate DTO pattern (constructor, getters, equals/hashCode, toString) that was unavoidable in Java 8. Every DTO in NorIntegrate is a record.
- **Pattern matching** (instanceof and switch) replaces verbose `if (x instanceof Foo) { Foo f = (Foo) x; ... }` chains.
- **Text blocks** make inline SQL, JSON, and multi-line strings readable without string concatenation.
- **`var`** reduces noise when the type is obvious from the right-hand side.
- **Sealed classes** provide compiler-enforced closed hierarchies, replacing unchecked `default` branches in switch.
- **Virtual threads** (Project Loom, finalized in Java 21) allow Spring Boot to run high-concurrency workloads without reactive programming — a critical enabler for ADR-002.

## Decision

Adopt Java 25 LTS as the sole target runtime. The compiler source and target are pinned to Java 25 in `build.gradle.kts`. Java 8 compatibility is not maintained.

## Consequences

- All DTOs are records; no Lombok is needed or permitted.
- Virtual threads are available for Spring Boot's embedded Tomcat (enabled in Spring Boot 4 by default).
- The team must learn features introduced between Java 9 and Java 25, but the migration is a one-time cost with a long LTS horizon ahead.
- Spring Boot 4 (ADR-002) requires Java 17+ at minimum; Java 25 satisfies this constraint and future-proofs the runtime.
