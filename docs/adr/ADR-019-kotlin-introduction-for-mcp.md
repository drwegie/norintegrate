# ADR-019: Kotlin Introduction for norintegrate-mcp

## Status

Proposed

## Context

NorIntegrate's three JVM modules (`norintegrate-common`, `norintegrate-api`, `norintegrate-mcp`) are all Java 25. The team wanted to evaluate Kotlin as a language option for new backend work, but the codebase's prior experience is Java-only, and a language change is not something to adopt on faith — it needed to be verified against the project's actual toolchain (Java 25 bytecode target, Spring Boot 4.1, Gradle 9, Spotless, JaCoCo) before any production code moved.

An AI-assisted proof of concept was run to validate the following claims against primary sources, with a real Gradle build (not just documentation reading) confirming each one:

- **Kotlin can target Java 25 bytecode.** Kotlin 2.3.0 introduced the ability to generate Java 25 class files (previously the ceiling was lower). Verified in the PoC by decompiling the compiled `.class` output and confirming class file major version 69 (Java 25). Source: https://kotlinlang.org/docs/whatsnew23.html
- **Spring Boot's Kotlin support has concrete, non-optional requirements.** Spring Boot supports Kotlin 2.2+. `kotlin-reflect` must be on the classpath for Spring's reflection-based bean instantiation to work correctly with Kotlin classes, and the `kotlin("plugin.spring")` (all-open) compiler plugin is effectively required — without it, Spring AOP proxying and `@Configuration` class enhancement fail because Kotlin classes and members are `final` by default. Sources: https://docs.spring.io/spring-boot/reference/features/kotlin.html and https://docs.spring.io/spring-framework/reference/languages/kotlin/requirements.html
- **Spotless can format Kotlin and Java in the same build without conflict.** `kotlin { ktlint() }` and `java { googleJavaFormat() }` are independent format targets within the same Spotless extension and can coexist in one module's build script. Source: https://github.com/diffplug/spotless/blob/main/plugin-gradle/README.md
- **JaCoCo's Kotlin coverage instrumentation is version-sensitive.** Inline function coverage (a Kotlin-specific bytecode pattern) requires JaCoCo 0.8.13+, and official Java 25 class file support requires 0.8.14+. The project's transitive JaCoCo version already resolves to 0.8.14, but it was not pinned — a future Gradle wrapper bump (the project receives frequent Dependabot bumps) could silently downgrade it and break Kotlin coverage. Source: https://www.jacoco.org/jacoco/trunk/doc/changes.html

**Why Kotlin 2.3.21 specifically, not the newest stable (2.4.10):** `spring-boot-dependencies:4.1.0`, the BOM this project already imports for all modules, manages `kotlin.version=2.3.21`. Pinning the Kotlin Gradle plugin to the same version as Spring Boot's managed `kotlin-stdlib` / `kotlin-reflect` eliminates version skew — the compiler and the runtime libraries on the classpath are guaranteed to match, which is exactly the combination Spring Boot's own test matrix validates.

All of the above was confirmed against a real build of the target module, not assumed from documentation alone. The AI's proposed alternatives (see below) were reviewed and rejected by a human before this decision was finalized.

## Decision

Adopt Kotlin, scoped to `norintegrate-mcp` only. This ADR (NOR-14) wires the Gradle build for Kotlin/Java interop and migrates one pilot file (`MunicipalitySearchTool`). A follow-up item (NOR-15) will convert the remainder of `norintegrate-mcp` to Kotlin and will be the point at which this ADR's status moves from Proposed to Accepted.

`norintegrate-mcp` was chosen as the pilot module for three concrete reasons:

1. **Smallest surface area.** `norintegrate-mcp`'s `src/main` is ~335 lines across 13 files — the smallest of the three JVM modules — which bounds the blast radius of a language migration.
2. **Leaf node in the dependency graph.** Per this project's dependency rules, `norintegrate-mcp` depends on `norintegrate-common`, but nothing in the project depends on `norintegrate-mcp`. A Kotlin/Java interop mistake inside `norintegrate-mcp` cannot propagate to `norintegrate-api` or `norintegrate-common`.
3. **No coverage gate, but a strong regression net.** Unlike `norintegrate-common`, `norintegrate-mcp` does not wire `jacocoTestCoverageVerification` into `check`, so there is no risk of a Kotlin file's coverage profile tripping a hard gate during the pilot. Its existing MCP-protocol end-to-end tests (`spring-ai-starter-mcp-client-webflux`) already exercise the tool through the real protocol layer, giving strong regression coverage for a language change to the tool implementation.

Build wiring:

- Root `build.gradle.kts` declares `kotlin("jvm")` and `kotlin("plugin.spring")` as `apply false`, and pins the JaCoCo tool version to `0.8.14` for all subprojects. Neither plugin is applied at the `subprojects` level — only `norintegrate-common` and `norintegrate-api` remain pure Java.
- `norintegrate-mcp/build.gradle.kts` applies both Kotlin plugins, sets `jvmTarget = JVM_25` explicitly (required — Kotlin does not infer this from the Java toolchain, and a mismatch fails the build), adds `kotlin-reflect` (version-managed by the Spring Boot BOM), and adds a module-local `spotless { kotlin { ktlint("1.8.0") } }` block alongside the existing `java { googleJavaFormat() }` configuration inherited from the root.
- `MunicipalityResult` is a Kotlin `data class` annotated `@JvmRecord`, so it compiles to an actual JVM record and keeps record-style accessors (`code()`, `name()`) for the Java call sites that still reference it (`MunicipalitySearchToolIT.java`), while also getting Kotlin's `data class` `equals`/`hashCode`/`copy`/destructuring for Kotlin call sites.
- `MunicipalitySearchTool.searchMunicipality`'s `query` parameter is typed nullable (`String?`), not non-null (`String`). A non-null Kotlin parameter causes the compiler to insert an `Intrinsics.checkNotNullParameter` null check that throws `NullPointerException` before the method body runs — which would change the tool's contract (it currently throws `IllegalArgumentException("query must not be blank")` for a `null` query, both as a matter of Java API design and as part of the MCP protocol's behavior when a client omits an argument). The nullable parameter plus an explicit `require(!query.isNullOrBlank())` preserves the exact original exception type and message.

## Alternatives Considered by AI (and Why Rejected)

As part of this decision, the AI proposed several alternative approaches. Each was reviewed and rejected by a human before proceeding, for the reasons below:

- **A. A new Kotlin-only module.** Rejected: there is no honest domain responsibility left to assign it. This project's `CLAUDE.md` already forbids Spring Batch, SSB Klass caching, and job-vacancy features, and `norintegrate-common`'s contract is "depends on nothing else in the project" — there is no genuine slice of domain logic to carve out. A module that exists only to host a different programming language would misrepresent what it does.
- **B. Convert one slice of `norintegrate-common` to Kotlin.** Rejected: `norintegrate-common` is the only module with `jacocoTestCoverageVerification` (`INSTRUCTION >= 0.80`) wired into `check`. The PoC measured this risk directly — even a `data class` with fully equivalent, exhaustive assertions landed at exactly 80% instruction coverage, i.e., right on the gate with no margin. `norintegrate-common` is also a shared dependency of both `norintegrate-api` and `norintegrate-mcp`, so a coverage regression there affects three CI workflows instead of one.
- **C. Convert test code only, keep production code in Java.** Rejected: this does not support a genuine claim of "production Kotlin" — it would demonstrate Kotlin test-writing ability without validating any of the production-path concerns (bytecode target, Spring bean instantiation, `kotlin-reflect`, `plugin.spring`) that this ADR exists to de-risk.
- **D. Kotlin 2.4.10 (latest stable) instead of 2.3.21.** Rejected: `spring-boot-dependencies:4.1.0` manages `kotlin.version=2.3.21`. Using 2.4.10 would introduce a version skew between the compiler and the BOM-managed `kotlin-stdlib`/`kotlin-reflect` on the classpath — a combination Spring Boot's own release has not validated — for zero functional benefit to this project.
- **E. Apply the Kotlin plugin uniformly to all `subprojects`.** Rejected: this would add the Kotlin stdlib and a `compileKotlin` task to two modules (`norintegrate-common`, `norintegrate-api`) that have no Kotlin source, and it would make the module boundary this ADR establishes unreadable from the build files themselves — any reader would have to know the ADR by heart instead of seeing the scope in `build.gradle.kts`.
- **F. Convert all three JVM modules to Kotlin in one pass.** Rejected: PORT-2 explicitly deprioritizes this. The regression risk spans multiple iterations' worth of surface area at once, with no incremental verification point.
- **G. Keep the JVM toolchain at 25 but lower Kotlin's bytecode target below 25 as a workaround.** Rejected: unnecessary. Kotlin 2.3.x officially generates JVM 25 bytecode (confirmed in the PoC — class file major version 69), so there is no compatibility gap to work around in the first place.

## Consequences

### Positive

- `norintegrate-mcp` gains a modern, null-safe, more concise language for future tool implementations, validated end-to-end against this project's actual build (not just Kotlin's general documentation).
- The Kotlin/Gradle plugin version is pinned to exactly what Spring Boot 4.1's BOM manages, eliminating a whole class of stdlib/compiler version-skew bugs.
- `norintegrate-common` and `norintegrate-api` remain untouched — zero risk to the coverage-gated module or the REST API during this pilot.
- `MunicipalitySearchToolIT.java` remains in Java, providing a standing regression check that Java call sites continue to interoperate correctly with the Kotlin-compiled `MunicipalityResult`/`MunicipalitySearchTool`.
- The `@JvmRecord` + nullable-parameter pattern established here is now documented and repeatable for the remaining `norintegrate-mcp` files in NOR-15.

### Negative

- `norintegrate-mcp` now has two source languages (Java and Kotlin) simultaneously until NOR-15 completes, which is a temporary but real cognitive cost for anyone reading the module.
- Two toolchains (`javac` and `kotlinc`) now run in `norintegrate-mcp`'s build, adding a small amount of build time and a second linter (`ktlint`) alongside `google-java-format`.
- The `@JvmRecord` requirement and the nullable-parameter-plus-`require()` pattern are non-obvious Kotlin/Java interop pitfalls that must be documented and remembered for every future Kotlin file that is exposed to Java call sites or Spring AI's `@Tool` reflection-based invocation.
