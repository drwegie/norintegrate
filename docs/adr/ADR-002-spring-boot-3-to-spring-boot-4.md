# ADR-002: Spring Boot 3 to Spring Boot 4 / Spring Framework 7

**Status:** Accepted
**Date:** 2026-03-14

## Context

The team's Spring experience spans 10 years, starting with Spring 3 XML configuration and evolving through early Spring Boot. Spring Boot 3.x (Spring Framework 6) was already a large step forward: it dropped Java 8/11 support, moved to Jakarta EE 9 namespaces (`jakarta.*` replacing `javax.*`), and embraced GraalVM native compilation. Many teams running Spring Boot 2.x have not yet migrated.

Spring Boot 4 / Spring Framework 7 adds:

- **First-class virtual thread support** — when combined with Java 21+ (we use Java 25), the embedded Tomcat container can run each request on a virtual thread, delivering near-reactive throughput without reactive programming models. The team has no Project Reactor or WebFlux experience, making this especially valuable.
- **Revised security model** — Spring Security 7 ships with Spring Boot 4 and provides a more composable, lambda-based security DSL that replaces the `WebSecurityConfigurerAdapter` subclass pattern the team knew from Spring 3/4.
- **Spring AI integration** — Spring AI targets Spring Boot 4 as its primary platform, which is a hard requirement for the MCP server module (ADR-003).
- **Continued Jakarta namespace** — staying on Spring Boot 3 would have required a second namespace migration later; Spring Boot 4 completes this transition.

The alternative of staying on Spring Boot 3 was considered but rejected because Spring AI's MCP server support requires Spring Boot 4, and virtual thread support in Boot 3 is opt-in with less integration depth.

## Decision

Use Spring Boot 4.0.x with Spring Framework 7 as the application framework. Virtual threads are enabled by default via `spring.threads.virtual.enabled=true`. Spring Security is configured using the lambda DSL with no `WebSecurityConfigurerAdapter`.

## Consequences

- All Spring dependency coordinates use `jakarta.*` namespaces — no `javax.*` imports anywhere in the codebase.
- The security configuration style is materially different from Spring 3/4 experience; the team must learn the lambda-based DSL.
- Virtual threads eliminate the need for reactive programming to handle concurrent SSB Klass API calls.
- Spring Boot 4 requires Java 17+ (we use Java 25, satisfying this requirement per ADR-001).
