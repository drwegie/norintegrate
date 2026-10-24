# Third-Party Notices

This file lists the third-party software distributed with norintegrate, together with the notices those licenses require.

## Scope

**In scope:** the runtime dependencies (Gradle `runtimeClasspath`) of the three JVM modules — `norintegrate-api`, `norintegrate-common` and `norintegrate-mcp`. These modules are shipped as fat JARs inside the `api` and `mcp` Docker images, so every artifact listed here is actually redistributed.

**Out of scope:** the npm dependencies of `norintegrate-web`. The web image ships a Next.js `.next/standalone` bundle, so it does carry third-party npm code. Both its copyleft (LGPL) components and, since NOR-31, its permissive- and CC-BY-4.0-licensed npm dependencies are covered by `THIRD-PARTY-NOTICES-WEB.md` instead.

**Not included:** build-only dependencies (ktlint, JaCoCo, the Kotlin Gradle plugin, and the rest of the build classpath). They are not part of any distributed artifact, so they carry no redistribution notice obligation.

## How this file is generated

```bash
scripts/generate-third-party-notices.sh   # collect POM licenses + NOTICE files
scripts/render-third-party-notices.py     # render this file
```

The first script resolves each artifact's license from its Maven Central POM (walking the `<parent>` chain when a POM declares none) and extracts any `META-INF/NOTICE` bundled in its JAR. The second applies the reviewed SPDX normalization and dual-license elections, both of which are explicit in the script rather than inferred.

Which artifacts ship is a reviewed list of `group:artifact` coordinates in `scripts/generate-third-party-notices.sh`; their versions are not recorded anywhere, but resolved from Gradle's `resolvedArtifacts` API on every run — **not** by parsing `./gradlew dependencies` tree output, which both invents entries (BOM/platform pseudo-artifacts declare no code) and drops real ones (`(*)` conflict-resolved starters). The generator refuses to run if the resolved artifact set differs from the reviewed one.

Artifacts are therefore listed below **without versions** (ADR-022). A version-pinned list is invalidated by every routine dependency bump, and repairing it needs the network and a populated Gradle cache — which is why Dependabot could never make its own pull requests pass. What the licenses here oblige us to reproduce — the artifact, its license, its NOTICE text — does not depend on the version. The exact versions that shipped are recorded in `build/third-party-notices/report.tsv` at generation time, and are resolvable at any point from the committed Gradle build files.

To check that this file still describes what actually ships — the failure mode a fixed list invites — run:

```bash
scripts/check-third-party-drift.sh
```

It re-resolves `runtimeClasspath` and diffs it against the recorded list at `group:artifact` granularity, exiting non-zero if an artifact has been added or dropped.

- Generated: 2026-10-24
- Source commit: `76cb5a0`
- Artifacts covered: **151**

## Summary

| License | Artifacts |
|---------|-----------|
| Apache-2.0 | 124 |
| EDL-1.0 | 8 |
| MIT | 8 |
| EPL-2.0 | 5 |
| BSD-3-Clause | 3 |
| BSD-2-Clause | 2 |
| MIT-0 | 1 |
| **Total** | **151** |

No dependency is under a strong copyleft license (the GPL/AGPL/SSPL family); none is present. 5 dependencies are under EPL-2.0, which is *weak* copyleft: its reciprocal obligations attach to modifications of the EPL-licensed files themselves, not to code that merely depends on them. norintegrate consumes all of them as unmodified binaries from Maven Central, so no source-disclosure obligation attaches to norintegrate's own code. Where an artifact is offered under a choice of licenses, the elected license is stated explicitly with the artifact.

## Dual-licensed dependencies and elected licenses

- **`ch.qos.logback:logback-classic`** → elected **EPL-2.0**. Dual-licensed EPL-2.0 or LGPL-2.1-only. **EPL-2.0 is elected.** The LGPL-2.1-only option is expressly *not* taken, so LGPL's relinking/source-substitution obligations do not apply here.
- **`ch.qos.logback:logback-core`** → elected **EPL-2.0**. Dual-licensed EPL-2.0 or LGPL-2.1-only. **EPL-2.0 is elected.** The LGPL-2.1-only option is expressly *not* taken, so LGPL's relinking/source-substitution obligations do not apply here.
- **`jakarta.annotation:jakarta.annotation-api`** → elected **EPL-2.0**. Dual-licensed EPL-2.0 or GPL-2.0-only WITH Classpath-exception-2.0. **EPL-2.0 is elected**; the GPL option is not taken.
- **`jakarta.persistence:jakarta.persistence-api`** → elected **EDL-1.0**. Dual-licensed EPL-2.0 or EDL-1.0. **EDL-1.0 is elected** (the BSD-3-Clause-equivalent option).
- **`jakarta.transaction:jakarta.transaction-api`** → elected **EPL-2.0**. Dual-licensed EPL-2.0 or GPL-2.0-only WITH Classpath-exception-2.0. **EPL-2.0 is elected**; the GPL option is not taken.
- **`net.logstash.logback:logstash-logback-encoder`** → elected **Apache-2.0**. Dual-licensed Apache-2.0 or MIT. **Apache-2.0 is elected.**
- **`org.hdrhistogram:HdrHistogram`** → elected **BSD-2-Clause**. Offered as CC0-1.0 (public domain dedication) or BSD-2-Clause. CC0-1.0 grants no patent license (CC0 §4(a) disclaims any patent rights the affirmer might hold), so **BSD-2-Clause is elected** instead.

## Dependencies with an imprecise upstream license declaration

- **`org.antlr:ST4`** — POM declares only "The BSD License" without naming the clause count; recorded as BSD-3-Clause (stricter reading).
- **`org.antlr:antlr-runtime`** — POM declares only "BSD licence" without naming the clause count; recorded as BSD-3-Clause (stricter reading).

## Dependencies by license

### Apache-2.0 (https://www.apache.org/licenses/LICENSE-2.0)

- `com.ethlo.time:itu`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.ethlo.time/itu)</sub>
- `com.fasterxml.jackson.core:jackson-annotations`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.fasterxml.jackson.core/jackson-annotations)</sub>
- `com.fasterxml.jackson.core:jackson-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.fasterxml.jackson.core/jackson-core)</sub>
- `com.fasterxml.jackson.core:jackson-databind`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.fasterxml.jackson.core/jackson-databind)</sub>
- `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml)</sub>
- `com.fasterxml.jackson.datatype:jackson-datatype-jsr310`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jsr310)</sub>
- `com.fasterxml:classmate`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.fasterxml/classmate)</sub>
- `com.github.victools:jsonschema-generator`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.github.victools/jsonschema-generator)</sub>
- `com.github.victools:jsonschema-module-jackson`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.github.victools/jsonschema-module-jackson)</sub>
- `com.github.victools:jsonschema-module-swagger-2`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.github.victools/jsonschema-module-swagger-2)</sub>
- `com.networknt:json-schema-validator`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.networknt/json-schema-validator)</sub>
- `com.nimbusds:nimbus-jose-jwt`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.nimbusds/nimbus-jose-jwt)</sub>
- `com.zaxxer:HikariCP`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.zaxxer/HikariCP)</sub>
- `commons-logging:commons-logging`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/commons-logging/commons-logging)</sub>
- `io.micrometer:context-propagation`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.micrometer/context-propagation)</sub>
- `io.micrometer:micrometer-commons`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.micrometer/micrometer-commons)</sub>
- `io.micrometer:micrometer-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.micrometer/micrometer-core)</sub>
- `io.micrometer:micrometer-jakarta9`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.micrometer/micrometer-jakarta9)</sub>
- `io.micrometer:micrometer-observation`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.micrometer/micrometer-observation)</sub>
- `io.micrometer:micrometer-registry-prometheus`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.micrometer/micrometer-registry-prometheus)</sub>
- `io.projectreactor:reactor-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.projectreactor/reactor-core)</sub>
- `io.prometheus:prometheus-metrics-config`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.prometheus/prometheus-metrics-config)</sub>
- `io.prometheus:prometheus-metrics-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.prometheus/prometheus-metrics-core)</sub>
- `io.prometheus:prometheus-metrics-exposition-formats`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.prometheus/prometheus-metrics-exposition-formats)</sub>
- `io.prometheus:prometheus-metrics-exposition-textformats`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.prometheus/prometheus-metrics-exposition-textformats)</sub>
- `io.prometheus:prometheus-metrics-model`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.prometheus/prometheus-metrics-model)</sub>
- `io.prometheus:prometheus-metrics-tracer-common`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.prometheus/prometheus-metrics-tracer-common)</sub>
- `io.swagger.core.v3:swagger-annotations-jakarta`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.swagger.core.v3/swagger-annotations-jakarta)</sub>
- `io.swagger.core.v3:swagger-core-jakarta`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.swagger.core.v3/swagger-core-jakarta)</sub>
- `io.swagger.core.v3:swagger-models-jakarta`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.swagger.core.v3/swagger-models-jakarta)</sub>
- `jakarta.inject:jakarta.inject-api`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/jakarta.inject/jakarta.inject-api)</sub>
- `jakarta.validation:jakarta.validation-api`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/jakarta.validation/jakarta.validation-api)</sub>
- `net.bytebuddy:byte-buddy`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/net.bytebuddy/byte-buddy)</sub>
- `net.logstash.logback:logstash-logback-encoder` — elected, see above  
  <sub>[Maven Central](https://central.sonatype.com/artifact/net.logstash.logback/logstash-logback-encoder)</sub>
- `org.apache.commons:commons-lang3`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.apache.commons/commons-lang3)</sub>
- `org.apache.logging.log4j:log4j-api`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.apache.logging.log4j/log4j-api)</sub>
- `org.apache.logging.log4j:log4j-to-slf4j`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.apache.logging.log4j/log4j-to-slf4j)</sub>
- `org.apache.tomcat.embed:tomcat-embed-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.apache.tomcat.embed/tomcat-embed-core)</sub>
- `org.apache.tomcat.embed:tomcat-embed-el`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.apache.tomcat.embed/tomcat-embed-el)</sub>
- `org.apache.tomcat.embed:tomcat-embed-websocket`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.apache.tomcat.embed/tomcat-embed-websocket)</sub>
- `org.hibernate.models:hibernate-models`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.hibernate.models/hibernate-models)</sub>
- `org.hibernate.orm:hibernate-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.hibernate.orm/hibernate-core)</sub>
- `org.hibernate.validator:hibernate-validator`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.hibernate.validator/hibernate-validator)</sub>
- `org.jboss.logging:jboss-logging`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.jboss.logging/jboss-logging)</sub>
- `org.jetbrains.kotlin:kotlin-reflect`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.jetbrains.kotlin/kotlin-reflect)</sub>
- `org.jetbrains.kotlin:kotlin-stdlib`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.jetbrains.kotlin/kotlin-stdlib)</sub>
- `org.jetbrains:annotations`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.jetbrains/annotations)</sub>
- `org.jspecify:jspecify`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.jspecify/jspecify)</sub>
- `org.snakeyaml:snakeyaml-engine`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.snakeyaml/snakeyaml-engine)</sub>
- `org.springdoc:springdoc-openapi-starter-common`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springdoc/springdoc-openapi-starter-common)</sub>
- `org.springdoc:springdoc-openapi-starter-webmvc-api`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-api)</sub>
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui)</sub>
- `org.springframework.ai:mcp-spring-webmvc`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.ai/mcp-spring-webmvc)</sub>
- `org.springframework.ai:spring-ai-autoconfigure-mcp-server-common`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.ai/spring-ai-autoconfigure-mcp-server-common)</sub>
- `org.springframework.ai:spring-ai-autoconfigure-mcp-server-webmvc`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.ai/spring-ai-autoconfigure-mcp-server-webmvc)</sub>
- `org.springframework.ai:spring-ai-commons`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.ai/spring-ai-commons)</sub>
- `org.springframework.ai:spring-ai-mcp`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.ai/spring-ai-mcp)</sub>
- `org.springframework.ai:spring-ai-mcp-annotations`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.ai/spring-ai-mcp-annotations)</sub>
- `org.springframework.ai:spring-ai-model`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.ai/spring-ai-model)</sub>
- `org.springframework.ai:spring-ai-starter-mcp-server-webmvc`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.ai/spring-ai-starter-mcp-server-webmvc)</sub>
- `org.springframework.ai:spring-ai-template-st`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.ai/spring-ai-template-st)</sub>
- `org.springframework.boot:spring-boot`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot)</sub>
- `org.springframework.boot:spring-boot-actuator`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-actuator)</sub>
- `org.springframework.boot:spring-boot-actuator-autoconfigure`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-actuator-autoconfigure)</sub>
- `org.springframework.boot:spring-boot-autoconfigure`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-autoconfigure)</sub>
- `org.springframework.boot:spring-boot-data-commons`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-data-commons)</sub>
- `org.springframework.boot:spring-boot-data-jpa`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-data-jpa)</sub>
- `org.springframework.boot:spring-boot-health`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-health)</sub>
- `org.springframework.boot:spring-boot-hibernate`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-hibernate)</sub>
- `org.springframework.boot:spring-boot-http-converter`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-http-converter)</sub>
- `org.springframework.boot:spring-boot-jackson`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-jackson)</sub>
- `org.springframework.boot:spring-boot-jdbc`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-jdbc)</sub>
- `org.springframework.boot:spring-boot-jpa`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-jpa)</sub>
- `org.springframework.boot:spring-boot-micrometer-metrics`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-micrometer-metrics)</sub>
- `org.springframework.boot:spring-boot-micrometer-observation`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-micrometer-observation)</sub>
- `org.springframework.boot:spring-boot-persistence`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-persistence)</sub>
- `org.springframework.boot:spring-boot-security`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-security)</sub>
- `org.springframework.boot:spring-boot-security-oauth2-resource-server`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-security-oauth2-resource-server)</sub>
- `org.springframework.boot:spring-boot-servlet`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-servlet)</sub>
- `org.springframework.boot:spring-boot-sql`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-sql)</sub>
- `org.springframework.boot:spring-boot-starter`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter)</sub>
- `org.springframework.boot:spring-boot-starter-actuator`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-actuator)</sub>
- `org.springframework.boot:spring-boot-starter-data-jpa`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa)</sub>
- `org.springframework.boot:spring-boot-starter-jackson`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-jackson)</sub>
- `org.springframework.boot:spring-boot-starter-jdbc`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-jdbc)</sub>
- `org.springframework.boot:spring-boot-starter-logging`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-logging)</sub>
- `org.springframework.boot:spring-boot-starter-micrometer-metrics`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-micrometer-metrics)</sub>
- `org.springframework.boot:spring-boot-starter-oauth2-resource-server`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-oauth2-resource-server)</sub>
- `org.springframework.boot:spring-boot-starter-security`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-security)</sub>
- `org.springframework.boot:spring-boot-starter-tomcat`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-tomcat)</sub>
- `org.springframework.boot:spring-boot-starter-tomcat-runtime`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-tomcat-runtime)</sub>
- `org.springframework.boot:spring-boot-starter-validation`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-validation)</sub>
- `org.springframework.boot:spring-boot-starter-web`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-starter-web)</sub>
- `org.springframework.boot:spring-boot-tomcat`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-tomcat)</sub>
- `org.springframework.boot:spring-boot-transaction`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-transaction)</sub>
- `org.springframework.boot:spring-boot-validation`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-validation)</sub>
- `org.springframework.boot:spring-boot-web-server`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-web-server)</sub>
- `org.springframework.boot:spring-boot-webmvc`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot-webmvc)</sub>
- `org.springframework.data:spring-data-commons`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.data/spring-data-commons)</sub>
- `org.springframework.data:spring-data-jpa`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.data/spring-data-jpa)</sub>
- `org.springframework.security:spring-security-config`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.security/spring-security-config)</sub>
- `org.springframework.security:spring-security-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.security/spring-security-core)</sub>
- `org.springframework.security:spring-security-crypto`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.security/spring-security-crypto)</sub>
- `org.springframework.security:spring-security-oauth2-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.security/spring-security-oauth2-core)</sub>
- `org.springframework.security:spring-security-oauth2-jose`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.security/spring-security-oauth2-jose)</sub>
- `org.springframework.security:spring-security-oauth2-resource-server`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.security/spring-security-oauth2-resource-server)</sub>
- `org.springframework.security:spring-security-web`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework.security/spring-security-web)</sub>
- `org.springframework:spring-aop`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-aop)</sub>
- `org.springframework:spring-aspects`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-aspects)</sub>
- `org.springframework:spring-beans`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-beans)</sub>
- `org.springframework:spring-context`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-context)</sub>
- `org.springframework:spring-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-core)</sub>
- `org.springframework:spring-expression`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-expression)</sub>
- `org.springframework:spring-jdbc`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-jdbc)</sub>
- `org.springframework:spring-messaging`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-messaging)</sub>
- `org.springframework:spring-orm`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-orm)</sub>
- `org.springframework:spring-tx`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-tx)</sub>
- `org.springframework:spring-web`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-web)</sub>
- `org.springframework:spring-webmvc`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.springframework/spring-webmvc)</sub>
- `org.webjars:swagger-ui`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.webjars/swagger-ui)</sub>
- `org.yaml:snakeyaml`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.yaml/snakeyaml)</sub>
- `tools.jackson.core:jackson-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/tools.jackson.core/jackson-core)</sub>
- `tools.jackson.core:jackson-databind`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/tools.jackson.core/jackson-databind)</sub>
- `tools.jackson.dataformat:jackson-dataformat-yaml`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/tools.jackson.dataformat/jackson-dataformat-yaml)</sub>

### EDL-1.0 (https://www.eclipse.org/org/documents/edl-v10.php)

- `com.sun.istack:istack-commons-runtime`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.sun.istack/istack-commons-runtime)</sub>
- `jakarta.activation:jakarta.activation-api`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/jakarta.activation/jakarta.activation-api)</sub>
- `jakarta.persistence:jakarta.persistence-api` — elected, see above  
  <sub>[Maven Central](https://central.sonatype.com/artifact/jakarta.persistence/jakarta.persistence-api)</sub>
- `jakarta.xml.bind:jakarta.xml.bind-api`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/jakarta.xml.bind/jakarta.xml.bind-api)</sub>
- `org.eclipse.angus:angus-activation`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.eclipse.angus/angus-activation)</sub>
- `org.glassfish.jaxb:jaxb-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.glassfish.jaxb/jaxb-core)</sub>
- `org.glassfish.jaxb:jaxb-runtime`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.glassfish.jaxb/jaxb-runtime)</sub>
- `org.glassfish.jaxb:txw2`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.glassfish.jaxb/txw2)</sub>

### MIT (https://opensource.org/license/mit)

- `com.knuddels:jtokkit`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/com.knuddels/jtokkit)</sub>
- `io.modelcontextprotocol.sdk:mcp`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.modelcontextprotocol.sdk/mcp)</sub>
- `io.modelcontextprotocol.sdk:mcp-core`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.modelcontextprotocol.sdk/mcp-core)</sub>
- `io.modelcontextprotocol.sdk:mcp-json-jackson3`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/io.modelcontextprotocol.sdk/mcp-json-jackson3)</sub>
- `org.checkerframework:checker-qual`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.checkerframework/checker-qual)</sub>
- `org.slf4j:jul-to-slf4j`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.slf4j/jul-to-slf4j)</sub>
- `org.slf4j:slf4j-api`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.slf4j/slf4j-api)</sub>
- `org.webjars:webjars-locator-lite`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.webjars/webjars-locator-lite)</sub>

### EPL-2.0 (https://www.eclipse.org/legal/epl-2.0/)

- `ch.qos.logback:logback-classic` — elected, see above  
  <sub>[Maven Central](https://central.sonatype.com/artifact/ch.qos.logback/logback-classic)</sub>
- `ch.qos.logback:logback-core` — elected, see above  
  <sub>[Maven Central](https://central.sonatype.com/artifact/ch.qos.logback/logback-core)</sub>
- `jakarta.annotation:jakarta.annotation-api` — elected, see above  
  <sub>[Maven Central](https://central.sonatype.com/artifact/jakarta.annotation/jakarta.annotation-api)</sub>
- `jakarta.transaction:jakarta.transaction-api` — elected, see above  
  <sub>[Maven Central](https://central.sonatype.com/artifact/jakarta.transaction/jakarta.transaction-api)</sub>
- `org.aspectj:aspectjweaver`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.aspectj/aspectjweaver)</sub>

### BSD-3-Clause (https://opensource.org/license/bsd-3-clause)

- `org.antlr:ST4` — see note above  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.antlr/ST4)</sub>
- `org.antlr:antlr-runtime` — see note above  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.antlr/antlr-runtime)</sub>
- `org.antlr:antlr4-runtime`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.antlr/antlr4-runtime)</sub>

### BSD-2-Clause (https://opensource.org/license/bsd-2-clause)

- `org.hdrhistogram:HdrHistogram` — elected, see above  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.hdrhistogram/HdrHistogram)</sub>
- `org.postgresql:postgresql`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.postgresql/postgresql)</sub>

### MIT-0 (https://opensource.org/license/mit-0)

- `org.reactivestreams:reactive-streams`  
  <sub>[Maven Central](https://central.sonatype.com/artifact/org.reactivestreams/reactive-streams)</sub>

## Bundled NOTICE files (Apache-2.0 section 4(d))

87 of the distributed artifacts bundle a `META-INF/NOTICE` file. Their contents are reproduced below, deduplicated to 31 distinct notices — several artifacts from the same project ship byte-identical text.

<details>
<summary><code>com.fasterxml:classmate</code></summary>

```text
Java ClassMate library was originally written by Tatu Saloranta (tatu.saloranta@iki.fi)

Other developers who have contributed code are:

* Brian Langel

## Copyright

Copyright 2007-, Tatu Saloranta (tatu.saloranta@iki.fi)
```

</details>

<details>
<summary><code>com.fasterxml.jackson.core:jackson-annotations (+1 more)</code></summary>

Applies to:

- `com.fasterxml.jackson.core:jackson-annotations`
- `com.fasterxml.jackson.core:jackson-databind`

```text
# Jackson JSON processor

Jackson is a high-performance, Free/Open Source JSON processing library.
It was originally written by Tatu Saloranta (tatu.saloranta@iki.fi), and has
been in development since 2007.
It is currently developed by a community of developers.

## Copyright

Copyright 2007-, Tatu Saloranta (tatu.saloranta@iki.fi)

## Licensing

Jackson 2.x core and extension components are licensed under Apache License 2.0
To find the details that apply to this artifact see the accompanying LICENSE file.

## Credits

A list of contributors may be found from CREDITS(-2.x) file, which is included
in some artifacts (usually source distributions); but is always available
from the source code management (SCM) system project uses.
```

</details>

<details>
<summary><code>com.fasterxml.jackson.core:jackson-core</code></summary>

```text
# Jackson JSON processor

Jackson is a high-performance, Free/Open Source JSON processing library.
It was originally written by Tatu Saloranta (tatu.saloranta@iki.fi), and has
been in development since 2007.
It is currently developed by a community of developers.

## Copyright

Copyright 2007-, Tatu Saloranta (tatu.saloranta@iki.fi)

## Licensing

Jackson 2.x core and extension components are licensed under Apache License 2.0
To find the details that apply to this artifact see the accompanying LICENSE file.

## Credits

A list of contributors may be found from CREDITS(-2.x) file, which is included
in some artifacts (usually source distributions); but is always available
from the source code management (SCM) system project uses.

## FastDoubleParser

jackson-core bundles a shaded copy of FastDoubleParser <https://github.com/wrandelshofer/FastDoubleParser>.
That code is available under an MIT license <https://github.com/wrandelshofer/FastDoubleParser/blob/main/LICENSE>
under the following copyright.

Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.

See FastDoubleParser-LICENSE and also FastDoubleParser-ThirdParty-LICENSE for details of other source code
included in FastDoubleParser and the licenses and copyrights that apply to that code.

## Schubfach

jackson-core bundles a copy of the Schubfach number writing code <https://github.com/c4f7fcce9cb06515/Schubfach>.
That code is available under an MIT license <https://github.com/c4f7fcce9cb06515/Schubfach/blob/master/todec/LICENSE>
under the following copyright.

Copyright 2018-2020 Raffaello Giulietti

See Schubfach-LICENSE.
```

</details>

<details>
<summary><code>com.fasterxml.jackson.dataformat:jackson-dataformat-yaml (+1 more)</code></summary>

Applies to:

- `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml`
- `tools.jackson.dataformat:jackson-dataformat-yaml`

```text
# Jackson JSON processor

Jackson is a high-performance, Free/Open Source JSON processing library.
It was originally written by Tatu Saloranta (tatu.saloranta@iki.fi), and has
been in development since 2007.
It is currently developed by a community of developers.

## Copyright

Copyright 2007-, Tatu Saloranta (tatu.saloranta@iki.fi)

## Licensing

Jackson components are licensed under Apache (Software) License, version 2.0,
as per accompanying LICENSE file.

## Credits

A list of contributors may be found from CREDITS file, which is included
in some artifacts (usually source distributions); but is always available
from the source code management (SCM) system project uses.
```

</details>

<details>
<summary><code>com.fasterxml.jackson.datatype:jackson-datatype-jsr310</code></summary>

```text
# Jackson JSON processor

Jackson is a high-performance, Free/Open Source JSON processing library.
It was originally written by Tatu Saloranta (tatu.saloranta@iki.fi), and has
been in development since 2007.
It is currently developed by a community of developers.

## Licensing

Jackson components are licensed under Apache (Software) License, version 2.0,
as per accompanying LICENSE file.

## Credits

A list of contributors may be found from CREDITS file, which is included
in some artifacts (usually source distributions); but is always available
from the source code management (SCM) system project uses.
```

</details>

<details>
<summary><code>commons-logging:commons-logging</code></summary>

```text
Apache Commons Logging
Copyright 2001-2026 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (https://www.apache.org/).
```

</details>

<details>
<summary><code>io.micrometer:context-propagation</code></summary>

```text
Micrometer Context Propagation

Copyright (c) 2017-Present VMware, Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

-------------------------------------------------------------------------------

This product contains a modified portion of 'io.netty.util.internal.logging',
in the Netty/Common library distributed by The Netty Project:

  * Copyright 2013 The Netty Project
  * License: Apache License v2.0
  * Homepage: https://netty.io

This product contains a modified portion of 'StringUtils.isBlank()',
in the Commons Lang library distributed by The Apache Software Foundation:

  * Copyright 2001-2019 The Apache Software Foundation
  * License: Apache License v2.0
  * Homepage: https://commons.apache.org/proper/commons-lang/

This product contains a modified portion of 'JsonUtf8Writer',
in the Moshi library distributed by Square, Inc:

  * Copyright 2010 Google Inc.
  * License: Apache License v2.0
  * Homepage: https://github.com/square/moshi

This product contains a modified portion of the 'org.springframework.lang'
package in the Spring Framework library, distributed by VMware, Inc:

  * Copyright 2002-2019 the original author or authors.
  * License: Apache License v2.0
  * Homepage: https://spring.io/projects/spring-framework
```

</details>

<details>
<summary><code>io.micrometer:micrometer-commons (+4 more)</code></summary>

Applies to:

- `io.micrometer:micrometer-commons`
- `io.micrometer:micrometer-core`
- `io.micrometer:micrometer-jakarta9`
- `io.micrometer:micrometer-observation`
- `io.micrometer:micrometer-registry-prometheus`

```text
Micrometer

Copyright (c) 2017-Present VMware, Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

-------------------------------------------------------------------------------

This product contains a modified portion of 'io.netty.util.internal.logging',
in the Netty/Common library distributed by The Netty Project:

  * Copyright 2013 The Netty Project
  * License: Apache License v2.0
  * Homepage: https://netty.io

This product contains a modified portion of 'StringUtils.isBlank()',
in the Commons Lang library distributed by The Apache Software Foundation:

  * Copyright 2001-2019 The Apache Software Foundation
  * License: Apache License v2.0
  * Homepage: https://commons.apache.org/proper/commons-lang/

This product contains a modified portion of 'JsonUtf8Writer',
in the Moshi library distributed by Square, Inc:

  * Copyright 2010 Google Inc.
  * License: Apache License v2.0
  * Homepage: https://github.com/square/moshi

This product contains a modified portion of the 'org.springframework.lang'
package in the Spring Framework library, distributed by VMware, Inc:

  * Copyright 2002-2019 the original author or authors.
  * License: Apache License v2.0
  * Homepage: https://spring.io/projects/spring-framework
```

</details>

<details>
<summary><code>io.swagger.core.v3:swagger-annotations-jakarta</code></summary>

```text
Swagger Core - swagger-annotations
Copyright (c) 2015. SmartBear Software Inc.
Swagger Core - swagger-annotations  is licensed under Apache 2.0 license.
Copy of the Apache 2.0 license can be found in `LICENSE` file.
```

</details>

<details>
<summary><code>io.swagger.core.v3:swagger-core-jakarta</code></summary>

```text
Swagger Core - swagger-core
Copyright (c) 2015. SmartBear Software Inc.
Swagger Core - swagger-core  is licensed under Apache 2.0 license.
Copy of the Apache 2.0 license can be found in `LICENSE` file.
```

</details>

<details>
<summary><code>io.swagger.core.v3:swagger-models-jakarta</code></summary>

```text
Swagger Core - swagger-models
Copyright (c) 2015. SmartBear Software Inc.
Swagger Core - swagger-models  is licensed under Apache 2.0 license.
Copy of the Apache 2.0 license can be found in `LICENSE` file.
```

</details>

<details>
<summary><code>jakarta.activation:jakarta.activation-api</code></summary>

```text
# Notices for Jakarta Activation

This content is produced and maintained by the Jakarta Activation project.

* Project home: https://projects.eclipse.org/projects/ee4j.jaf

## Trademarks

Jakarta Activation is a trademark of the Eclipse Foundation.

## Copyright

All content is the property of the respective authors or their employers. For
more information regarding authorship of content, please consult the listed
source code repository logs.

## Declared Project Licenses

This program and the accompanying materials are made available under the terms
of the Eclipse Public License v. 2.0 which is available at
https://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License v1.0
which is available at https://www.eclipse.org/org/documents/edl-v10.php. This
Source Code may also be made available under the following Secondary Licenses
when the conditions for such availability set forth in the Eclipse Public
License v. 2.0 are satisfied: (secondary) GPL-2.0 with Classpath-exception-2.0
which is available at https://openjdk.java.net/legal/gplv2+ce.html.

SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0-only with
Classpath-exception-2.0

## Source Code

The project maintains the following source code repositories:

* https://github.com/jakartaee/jaf-api
* https://github.com/jakartaee/jaf-tck

## Third-party Content

This project leverages the following third party content.

Apache Ant (1.9.6)

* License: Apache License, 2.0, W3C License, Public Domain

Apache Ant (1.9.6)

* License: Apache License, 2.0, W3C License, Public Domain

Apache commons-lang (3.5)

* License: Apache-2.0

font-awesome (4.7.0)

* License: OFL-1.1 AND MIT

jsoup (1.10.2)

* License: MIT

JTHarness (5.0)

* License: (GPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0)
* Project: https://wiki.openjdk.java.net/display/CodeTools/JT+Harness
* Source: http://hg.openjdk.java.net/code-tools/jtharness/

JUnit (4.12)

* License: Eclipse Public License

normalize.css (3.0.2)

* License: MIT
* Project: http://necolas.github.io/normalize.css/
* Source: http://necolas.github.io/normalize.css/

SigTest (4.0)

* License: GPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
* Project: https://wiki.openjdk.java.net/display/CodeTools/sigtest
* Source: http://hg.openjdk.java.net/code-tools/sigtest/file/c57f97e2ac2f

## Cryptography

Content may contain encryption software. The country in which you are currently
may have restrictions on the import, possession, and use, and/or re-export to
another country, of encryption software. BEFORE using any encryption software,
please check the country's laws, regulations and policies concerning the import,
possession, or use, and re-export of encryption software, to see if this is
permitted.
```

</details>

<details>
<summary><code>jakarta.annotation:jakarta.annotation-api</code></summary>

```text
# Notices for Jakarta Annotations

This content is produced and maintained by the Jakarta Annotations project.

* Project home: https://projects.eclipse.org/projects/ee4j.ca

## Trademarks

Jakarta Annotations™ is a trademark of the Eclipse Foundation.

## Copyright

All content is the property of the respective authors or their employers. For
more information regarding authorship of content, please consult the listed
source code repository logs.

## Declared Project Licenses

This program and the accompanying materials are made available under the terms
of the Eclipse Public License v. 2.0 which is available at
https://www.eclipse.org/legal/epl-2.0. This Source Code may also be made
available under the following Secondary Licenses when the conditions for such
availability set forth in the Eclipse Public License v. 2.0 are satisfied:
GPL-2.0 with Classpath-exception-2.0 which is available at
https://openjdk.java.net/legal/gplv2+ce.html.

SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-only with Classpath-exception-2.0

## Source Code

The project maintains the following source code repositories:

* https://github.com/jakartaee/common-annotations-api

## Cryptography

Content may contain encryption software. The country in which you are currently
may have restrictions on the import, possession, and use, and/or re-export to
another country, of encryption software. BEFORE using any encryption software,
please check the country's laws, regulations and policies concerning the import,
possession, or use, and re-export of encryption software, to see if this is
permitted.
```

</details>

<details>
<summary><code>jakarta.inject:jakarta.inject-api</code></summary>

```text
# Notices for Eclipse Jakarta Dependency Injection

This content is produced and maintained by the Eclipse Jakarta Dependency Injection project.

* Project home: https://projects.eclipse.org/projects/cdi.batch

## Trademarks

Jakarta Dependency Injection is a trademark of the Eclipse Foundation.

## Copyright

All content is the property of the respective authors or their employers. For
more information regarding authorship of content, please consult the listed
source code repository logs.

## Declared Project Licenses

This program and the accompanying materials are made available under the terms
of the Apache License, Version 2.0 which is available at
https://www.apache.org/licenses/LICENSE-2.0.

SPDX-License-Identifier: Apache-2.0

## Source Code

The project maintains the following source code repositories:

https://github.com/eclipse-ee4j/injection-api
https://github.com/eclipse-ee4j/injection-spec
https://github.com/eclipse-ee4j/injection-tck

## Third-party Content

This project leverages the following third party content.

None

## Cryptography

None
```

</details>

<details>
<summary><code>jakarta.persistence:jakarta.persistence-api</code></summary>

```text
[//]: # " Copyright (c) 2019, 2023 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # "  "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

# Notices for Jakarta Persistence

This content is produced and maintained by the Jakarta Persistence project.

* Project home: https://projects.eclipse.org/projects/ee4j.jpa

## Trademarks

Jakarta Persistence is a trademark of the Eclipse Foundation.

## Copyright

All content is the property of the respective authors or their employers. For
more information regarding authorship of content, please consult the listed
source code repository logs.

## Declared Project Licenses

This program and the accompanying materials are made available under the terms
of the Eclipse Public License v. 2.0 which is available at
https://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License v1.0
which is available at https://www.eclipse.org/org/documents/edl-v10.php.

SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause

## Source Code

The project maintains the following source code repositories:

* https://github.com/jakartaee/persistence

## Third-party Content

This project leverages the following third party content.

None

## Cryptography

Content may contain encryption software. The country in which you are currently
may have restrictions on the import, possession, and use, and/or re-export to
another country, of encryption software. BEFORE using any encryption software,
please check the country's laws, regulations and policies concerning the import,
possession, or use, and re-export of encryption software, to see if this is
permitted.
```

</details>

<details>
<summary><code>jakarta.transaction:jakarta.transaction-api</code></summary>

```text
# Notices for Jakarta Transactions

This content is produced and maintained by the Jakarta Transactions project.

* Project home: https://projects.eclipse.org/projects/ee4j.jta

## Trademarks

Jakarta Transactions is a trademark of the Eclipse Foundation.

## Copyright

All content is the property of the respective authors or their employers. For
more information regarding authorship of content, please consult the listed
source code repository logs.

## Declared Project Licenses

This program and the accompanying materials are made available under the terms
of the Eclipse Public License v. 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0. This Source Code may also be made
available under the following Secondary Licenses when the conditions for such
availability set forth in the Eclipse Public License v. 2.0 are satisfied: GNU
General Public License, version 2 with the GNU Classpath Exception which is
available at https://www.gnu.org/software/classpath/license.html.

SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

## Source Code

The project maintains the following source code repositories:

* https://github.com/eclipse-ee4j/jta-api

## Third-party Content

This project leverages the following third party content.

None

## Cryptography

Content may contain encryption software. The country in which you are currently
may have restrictions on the import, possession, and use, and/or re-export to
another country, of encryption software. BEFORE using any encryption software,
please check the country's laws, regulations and policies concerning the import,
possession, or use, and re-export of encryption software, to see if this is
permitted.
```

</details>

<details>
<summary><code>jakarta.xml.bind:jakarta.xml.bind-api</code></summary>

```text
[//]: # " Copyright (c) 2018, 2024 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # "  "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

# Notices for Jakarta XML Binding

This content is produced and maintained by the Jakarta XML Binding project.

* Project home: https://projects.eclipse.org/projects/ee4j.jaxb

## Trademarks

Jakarta XML Binding™ is a trademark of the Eclipse Foundation.

## Copyright

All content is the property of the respective authors or their employers. For
more information regarding authorship of content, please consult the listed
source code repository logs.

## Declared Project Licenses

This program and the accompanying materials are made available under the terms
of the Eclipse Distribution License v1.0 which is available at
https://www.eclipse.org/org/documents/edl-v10.php.

SPDX-License-Identifier: BSD-3-Clause

## Source Code

The project maintains the following source code repositories:

* https://github.com/jakartaee/jaxb-api
* https://github.com/jakartaee/jaxb-tck

## Cryptography

Content may contain encryption software. The country in which you are currently
may have restrictions on the import, possession, and use, and/or re-export to
another country, of encryption software. BEFORE using any encryption software,
please check the country's laws, regulations and policies concerning the import,
possession, or use, and re-export of encryption software, to see if this is
permitted.
```

</details>

<details>
<summary><code>net.bytebuddy:byte-buddy</code></summary>

```text
Copyright 2014 - Present Rafael Winterhalter

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

</details>

<details>
<summary><code>org.apache.commons:commons-lang3</code></summary>

```text
Apache Commons Lang
Copyright 2001-2025 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (https://www.apache.org/).
```

</details>

<details>
<summary><code>org.apache.logging.log4j:log4j-api</code></summary>

```text
Apache Log4j API
Copyright 1999-2026 The Apache Software Foundation


This product includes software developed at
The Apache Software Foundation (http://www.apache.org/).
```

</details>

<details>
<summary><code>org.apache.logging.log4j:log4j-to-slf4j</code></summary>

```text
Log4j API to SLF4J Adapter
Copyright 1999-2026 The Apache Software Foundation


This product includes software developed at
The Apache Software Foundation (http://www.apache.org/).
```

</details>

<details>
<summary><code>org.apache.tomcat.embed:tomcat-embed-core</code></summary>

```text
Apache Tomcat
Copyright 1999-2026 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (http://www.apache.org/).

The original XML Schemas for Java EE Deployment Descriptors:
 - javaee_5.xsd
 - javaee_web_services_1_2.xsd
 - javaee_web_services_client_1_2.xsd
 - javaee_6.xsd
 - javaee_web_services_1_3.xsd
 - javaee_web_services_client_1_3.xsd
 - jsp_2_2.xsd
 - web-app_3_0.xsd
 - web-common_3_0.xsd
 - web-fragment_3_0.xsd
 - javaee_7.xsd
 - javaee_web_services_1_4.xsd
 - javaee_web_services_client_1_4.xsd
 - jsp_2_3.xsd
 - web-app_3_1.xsd
 - web-common_3_1.xsd
 - web-fragment_3_1.xsd
 - javaee_8.xsd
 - web-app_4_0.xsd
 - web-common_4_0.xsd
 - web-fragment_4_0.xsd

may be obtained from:
http://www.oracle.com/webfolder/technetwork/jsc/xml/ns/javaee/index.html
```

</details>

<details>
<summary><code>org.apache.tomcat.embed:tomcat-embed-el (+1 more)</code></summary>

Applies to:

- `org.apache.tomcat.embed:tomcat-embed-el`
- `org.apache.tomcat.embed:tomcat-embed-websocket`

```text
Apache Tomcat
Copyright 1999-2026 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (http://www.apache.org/).
```

</details>

<details>
<summary><code>org.eclipse.angus:angus-activation</code></summary>

```text
# Notices for Eclipse Angus

This content is produced and maintained by the Eclipse Angus project.

* Project home: https://projects.eclipse.org/projects/ee4j.angus

## Trademarks

Eclipse Angus is a trademark of the Eclipse Foundation.

## Copyright

All content is the property of the respective authors or their employers. For
more information regarding authorship of content, please consult the listed
source code repository logs.

## Declared Project Licenses

This program and the accompanying materials are made available under the terms
of the Eclipse Distribution License v1.0 which is available at
https://www.eclipse.org/org/documents/edl-v10.php.

SPDX-License-Identifier: BSD-3-Clause

## Source Code

The project maintains the following source code repositories:

* https://github.com/eclipse-ee4j/angus-activation
* https://github.com/eclipse-ee4j/angus-mail

## Third-party Content

This project leverages the following third party content.

None

## Cryptography

Content may contain encryption software. The country in which you are currently
may have restrictions on the import, possession, and use, and/or re-export to
another country, of encryption software. BEFORE using any encryption software,
please check the country's laws, regulations and policies concerning the import,
possession, or use, and re-export of encryption software, to see if this is
permitted.
```

</details>

<details>
<summary><code>org.glassfish.jaxb:jaxb-core (+2 more)</code></summary>

Applies to:

- `org.glassfish.jaxb:jaxb-core`
- `org.glassfish.jaxb:jaxb-runtime`
- `org.glassfish.jaxb:txw2`

```text
# Notices for Eclipse Implementation of JAXB

This content is produced and maintained by the Eclipse Implementation of JAXB
project.

* Project home: https://projects.eclipse.org/projects/ee4j.jaxb-impl

## Trademarks

Eclipse Implementation of JAXB is a trademark of the Eclipse Foundation.

## Copyright

All content is the property of the respective authors or their employers. For
more information regarding authorship of content, please consult the listed
source code repository logs.

## Declared Project Licenses

This program and the accompanying materials are made available under the terms
of the Eclipse Distribution License v. 1.0 which is available at
http://www.eclipse.org/org/documents/edl-v10.php.

SPDX-License-Identifier: BSD-3-Clause

## Source Code

The project maintains the following source code repositories:

* https://github.com/eclipse-ee4j/jaxb-ri
* https://github.com/eclipse-ee4j/jaxb-istack-commons
* https://github.com/eclipse-ee4j/jaxb-dtd-parser
* https://github.com/eclipse-ee4j/jaxb-fi
* https://github.com/eclipse-ee4j/jaxb-stax-ex
* https://github.com/eclipse-ee4j/jax-rpc-ri

## Third-party Content

This project leverages the following third party content.

Apache Ant (1.10.2)

* License: Apache-2.0 AND W3C AND LicenseRef-Public-Domain

Apache Ant (1.10.2)

* License: Apache-2.0 AND W3C AND LicenseRef-Public-Domain

Apache Felix (1.2.0)

* License: Apache License, 2.0

args4j (2.33)

* License: MIT License

dom4j (1.6.1)

* License: Custom license based on Apache 1.1

file-management (3.0.0)

* License: Apache-2.0
* Project: https://maven.apache.org/shared/file-management/
* Source:
   https://svn.apache.org/viewvc/maven/shared/tags/file-management-3.0.0/

JUnit (4.12)

* License: Eclipse Public License

JUnit (4.12)

* License: Eclipse Public License

maven-compat (3.5.2)

* License: Apache-2.0
* Project: https://maven.apache.org/ref/3.5.2/maven-compat/
* Source:
   https://mvnrepository.com/artifact/org.apache.maven/maven-compat/3.5.2

maven-core (3.5.2)

* License: Apache-2.0
* Project: https://maven.apache.org/ref/3.5.2/maven-core/index.html
* Source: https://mvnrepository.com/artifact/org.apache.maven/maven-core/3.5.2

maven-plugin-annotations (3.5)

* License: Apache-2.0
* Project: https://maven.apache.org/plugin-tools/maven-plugin-annotations/
* Source:
   https://github.com/apache/maven-plugin-tools/tree/master/maven-plugin-annotations

maven-plugin-api (3.5.2)

* License: Apache-2.0

maven-resolver-api (1.1.1)

* License: Apache-2.0

maven-resolver-api (1.1.1)

* License: Apache-2.0

maven-resolver-connector-basic (1.1.1)

* License: Apache-2.0

maven-resolver-impl (1.1.1)

* License: Apache-2.0

maven-resolver-spi (1.1.1)

* License: Apache-2.0

maven-resolver-transport-file (1.1.1)

* License: Apache-2.0
* Project: https://maven.apache.org/resolver/maven-resolver-transport-file/
* Source:
   https://github.com/apache/maven-resolver/tree/master/maven-resolver-transport-file

maven-resolver-util (1.1.1)

* License: Apache-2.0

maven-settings (3.5.2)

* License: Apache-2.0
* Source:
   https://mvnrepository.com/artifact/org.apache.maven/maven-settings/3.5.2

OSGi Service Platform Core Companion Code (6.0)

* License: Apache License, 2.0

plexus-archiver (3.5)

* License: Apache-2.0
* Project: https://codehaus-plexus.github.io/plexus-archiver/
* Source: https://github.com/codehaus-plexus/plexus-archiver

plexus-io (3.0.0)

* License: Apache-2.0

plexus-utils (3.1.0)

* License: Apache- 2.0 or Apache- 1.1 or BSD or Public Domain or Indiana
   University Extreme! Lab Software License V1.1.1 (Apache 1.1 style)

relaxng-datatype (1.0)

* License: New BSD license

Sax (0.2)

* License: SAX-PD
* Project: http://www.megginson.com/downloads/SAX/
* Source: http://sourceforge.net/project/showfiles.php?group_id=29449

testng (6.14.2)

* License: Apache-2.0 AND (MIT OR GPL-1.0+)
* Project: https://testng.org/doc/index.html
* Source: https://github.com/cbeust/testng

wagon-http-lightweight (3.0.0)

* License: Pending
* Project: https://maven.apache.org/wagon/
* Source:
   https://mvnrepository.com/artifact/org.apache.maven.wagon/wagon-http-lightweight/3.0.0

xz for java (1.8)

* License: LicenseRef-Public-Domain

## Cryptography

Content may contain encryption software. The country in which you are currently
may have restrictions on the import, possession, and use, and/or re-export to
another country, of encryption software. BEFORE using any encryption software,
please check the country's laws, regulations and policies concerning the import,
possession, or use, and re-export of encryption software, to see if this is
permitted.
```

</details>

<details>
<summary><code>org.springframework:spring-aop (+11 more)</code></summary>

Applies to:

- `org.springframework:spring-aop`
- `org.springframework:spring-aspects`
- `org.springframework:spring-beans`
- `org.springframework:spring-context`
- `org.springframework:spring-core`
- `org.springframework:spring-expression`
- `org.springframework:spring-jdbc`
- `org.springframework:spring-messaging`
- `org.springframework:spring-orm`
- `org.springframework:spring-tx`
- `org.springframework:spring-web`
- `org.springframework:spring-webmvc`

```text
Spring Framework 7.0.9
Copyright (c) 2002-2026 Pivotal, Inc.

This product is licensed to you under the Apache License, Version 2.0
(the "License"). You may not use this product except in compliance with
the License.

This product may include a number of subcomponents with separate
copyright notices and license terms. Your use of the source code for
these subcomponents is subject to the terms and conditions of the
subcomponent's license, as noted in the license.txt file.
```

</details>

<details>
<summary><code>org.springframework.boot:spring-boot (+36 more)</code></summary>

Applies to:

- `org.springframework.boot:spring-boot`
- `org.springframework.boot:spring-boot-actuator`
- `org.springframework.boot:spring-boot-actuator-autoconfigure`
- `org.springframework.boot:spring-boot-autoconfigure`
- `org.springframework.boot:spring-boot-data-commons`
- `org.springframework.boot:spring-boot-data-jpa`
- `org.springframework.boot:spring-boot-health`
- `org.springframework.boot:spring-boot-hibernate`
- `org.springframework.boot:spring-boot-http-converter`
- `org.springframework.boot:spring-boot-jackson`
- `org.springframework.boot:spring-boot-jdbc`
- `org.springframework.boot:spring-boot-jpa`
- `org.springframework.boot:spring-boot-micrometer-metrics`
- `org.springframework.boot:spring-boot-micrometer-observation`
- `org.springframework.boot:spring-boot-persistence`
- `org.springframework.boot:spring-boot-security`
- `org.springframework.boot:spring-boot-security-oauth2-resource-server`
- `org.springframework.boot:spring-boot-servlet`
- `org.springframework.boot:spring-boot-sql`
- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-starter-actuator`
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.springframework.boot:spring-boot-starter-jackson`
- `org.springframework.boot:spring-boot-starter-jdbc`
- `org.springframework.boot:spring-boot-starter-logging`
- `org.springframework.boot:spring-boot-starter-micrometer-metrics`
- `org.springframework.boot:spring-boot-starter-oauth2-resource-server`
- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.boot:spring-boot-starter-tomcat`
- `org.springframework.boot:spring-boot-starter-tomcat-runtime`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-tomcat`
- `org.springframework.boot:spring-boot-transaction`
- `org.springframework.boot:spring-boot-validation`
- `org.springframework.boot:spring-boot-web-server`
- `org.springframework.boot:spring-boot-webmvc`

```text
Spring Boot 4.1.1
Copyright (c) 2012-2026 VMware, Inc.

This product is licensed to you under the Apache License, Version 2.0
(the "License"). You may not use this product except in compliance with
the License.
```

</details>

<details>
<summary><code>org.springframework.data:spring-data-commons</code></summary>

```text
Spring Data Commons 4.1.1 (2026.0.1)
Copyright (c) [2010-2026] Pivotal Software, Inc.

This product is licensed to you under the Apache License, Version 2.0 (the "License").
You may not use this product except in compliance with the License.

This product may include a number of subcomponents with
separate copyright notices and license terms. Your use of the source
code for the these subcomponents is subject to the terms and
conditions of the subcomponent's license, as noted in the LICENSE file.
```

</details>

<details>
<summary><code>org.springframework.data:spring-data-jpa</code></summary>

```text
Spring Data JPA 4.1.1 (2026.0.1)
Copyright (c) 2015-2026 Pivotal Software, Inc.

This product is licensed to you under the Apache License, Version 2.0
(the "License"). You may not use this product except in compliance with
the License.

This product may include a number of subcomponents with separate
copyright notices and license terms. Your use of the source code for
these subcomponents is subject to the terms and conditions of the
subcomponent's license, as noted in the license.txt file.
```

</details>

<details>
<summary><code>tools.jackson.core:jackson-core</code></summary>

```text
# Jackson JSON processor

Jackson is a high-performance, Free/Open Source JSON processing library.
It was originally written by Tatu Saloranta (tatu.saloranta@iki.fi), and has
been in development since 2007.
It is currently developed by a community of developers.

## Copyright

Copyright 2007-, Tatu Saloranta (tatu.saloranta@iki.fi)

## Licensing

Jackson 3.x core and extension components are licensed under Apache License 2.0
To find the details that apply to this artifact see the accompanying LICENSE file.

## Credits

A list of contributors may be found from CREDITS file, which is included
in some artifacts (usually source distributions); but is always available
from the source code management (SCM) system project uses.

## FastDoubleParser

jackson-core bundles a shaded copy of FastDoubleParser <https://github.com/wrandelshofer/FastDoubleParser>.
That code is available under an MIT license <https://github.com/wrandelshofer/FastDoubleParser/blob/main/LICENSE>
under the following copyright.

Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.

See FastDoubleParser-LICENSE and also FastDoubleParser-ThirdParty-LICENSE for details of other source code
included in FastDoubleParser and the licenses and copyrights that apply to that code.

## Schubfach

jackson-core bundles a copy of the Schubfach number writing code <https://github.com/c4f7fcce9cb06515/Schubfach>.
That code is available under an MIT license <https://github.com/c4f7fcce9cb06515/Schubfach/blob/master/todec/LICENSE>
under the following copyright.

Copyright 2018-2020 Raffaello Giulietti

See Schubfach-LICENSE.
```

</details>

<details>
<summary><code>tools.jackson.core:jackson-databind</code></summary>

```text
# Jackson JSON processor

Jackson is a high-performance, Free/Open Source JSON processing library.
It was originally written by Tatu Saloranta (tatu.saloranta@iki.fi), and has
been in development since 2007.
It is currently developed by a community of developers.

## Copyright

Copyright 2007-, Tatu Saloranta (tatu.saloranta@iki.fi)

## Licensing

Jackson 3.x core and extension components are licensed under Apache License 2.0
To find the details that apply to this artifact see the accompanying LICENSE file.

## Credits

A list of contributors may be found from CREDITS file, which is included
in some artifacts (usually source distributions); but is always available
from the source code management (SCM) system project uses.
```

</details>

## License texts

### Apache-2.0

```text
Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.

      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.

      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.

      "Legal Entity" shall mean the union of the acting entity and all
      other entities that control, are controlled by, or are under common
      control with that entity. For the purposes of this definition,
      "control" means (i) the power, direct or indirect, to cause the
      direction or management of such entity, whether by contract or
      otherwise, or (ii) ownership of fifty percent (50%) or more of the
      outstanding shares, or (iii) beneficial ownership of such entity.

      "You" (or "Your") shall mean an individual or Legal Entity
      exercising permissions granted by this License.

      "Source" form shall mean the preferred form for making modifications,
      including but not limited to software source code, documentation
      source, and configuration files.

      "Object" form shall mean any form resulting from mechanical
      transformation or translation of a Source form, including but
      not limited to compiled object code, generated documentation,
      and conversions to other media types.

      "Work" shall mean the work of authorship, whether in Source or
      Object form, made available under the License, as indicated by a
      copyright notice that is included in or attached to the work
      (an example is provided in the Appendix below).

      "Derivative Works" shall mean any work, whether in Source or Object
      form, that is based on (or derived from) the Work and for which the
      editorial revisions, annotations, elaborations, or other modifications
      represent, as a whole, an original work of authorship. For the purposes
      of this License, Derivative Works shall not include works that remain
      separable from, or merely link (or bind by name) to the interfaces of,
      the Work and Derivative Works thereof.

      "Contribution" shall mean any work of authorship, including
      the original version of the Work and any modifications or additions
      to that Work or Derivative Works thereof, that is intentionally
      submitted to Licensor for inclusion in the Work by the copyright owner
      or by an individual or Legal Entity authorized to submit on behalf of
      the copyright owner. For the purposes of this definition, "submitted"
      means any form of electronic, verbal, or written communication sent
      to the Licensor or its representatives, including but not limited to
      communication on electronic mailing lists, source code control systems,
      and issue tracking systems that are managed by, or on behalf of, the
      Licensor for the purpose of discussing and improving the Work, but
      excluding communication that is conspicuously marked or otherwise
      designated in writing by the copyright owner as "Not a Contribution."

      "Contributor" shall mean Licensor and any individual or Legal Entity
      on behalf of whom a Contribution has been received by Licensor and
      subsequently incorporated within the Work.

   2. Grant of Copyright License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.

   3. Grant of Patent License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      (except as stated in this section) patent license to make, have made,
      use, offer to sell, sell, import, and otherwise transfer the Work,
      where such license applies only to those patent claims licensable
      by such Contributor that are necessarily infringed by their
      Contribution(s) alone or by combination of their Contribution(s)
      with the Work to which such Contribution(s) was submitted. If You
      institute patent litigation against any entity (including a
      cross-claim or counterclaim in a lawsuit) alleging that the Work
      or a Contribution incorporated within the Work constitutes direct
      or contributory patent infringement, then any patent licenses
      granted to You under this License for that Work shall terminate
      as of the date such litigation is filed.

   4. Redistribution. You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the following conditions:

      (a) You must give any other recipients of the Work or
          Derivative Works a copy of this License; and

      (b) You must cause any modified files to carry prominent notices
          stating that You changed the files; and

      (c) You must retain, in the Source form of any Derivative Works
          that You distribute, all copyright, patent, trademark, and
          attribution notices from the Source form of the Work,
          excluding those notices that do not pertain to any part of
          the Derivative Works; and

      (d) If the Work includes a "NOTICE" text file as part of its
          distribution, then any Derivative Works that You distribute must
          include a readable copy of the attribution notices contained
          within such NOTICE file, excluding those notices that do not
          pertain to any part of the Derivative Works, in at least one
          of the following places: within a NOTICE text file distributed
          as part of the Derivative Works; within the Source form or
          documentation, if provided along with the Derivative Works; or,
          within a display generated by the Derivative Works, if and
          wherever such third-party notices normally appear. The contents
          of the NOTICE file are for informational purposes only and
          do not modify the License. You may add Your own attribution
          notices within Derivative Works that You distribute, alongside
          or as an addendum to the NOTICE text from the Work, provided
          that such additional attribution notices cannot be construed
          as modifying the License.

      You may add Your own copyright statement to Your modifications and
      may provide additional or different license terms and conditions
      for use, reproduction, or distribution of Your modifications, or
      for any such Derivative Works as a whole, provided Your use,
      reproduction, and distribution of the Work otherwise complies with
      the conditions stated in this License.

   5. Submission of Contributions. Unless You explicitly state otherwise,
      any Contribution intentionally submitted for inclusion in the Work
      by You to the Licensor shall be under the terms and conditions of
      this License, without any additional terms or conditions.
      Notwithstanding the above, nothing herein shall supersede or modify
      the terms of any separate license agreement you may have executed
      with Licensor regarding such Contributions.

   6. Trademarks. This License does not grant permission to use the trade
      names, trademarks, service marks, or product names of the Licensor,
      except as required for reasonable and customary use in describing the
      origin of the Work and reproducing the content of the NOTICE file.

   7. Disclaimer of Warranty. Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE. You are solely responsible for determining the
      appropriateness of using or redistributing the Work and assume any
      risks associated with Your exercise of permissions under this License.

   8. Limitation of Liability. In no event and under no legal theory,
      whether in tort (including negligence), contract, or otherwise,
      unless required by applicable law (such as deliberate and grossly
      negligent acts) or agreed to in writing, shall any Contributor be
      liable to You for damages, including any direct, indirect, special,
      incidental, or consequential damages of any character arising as a
      result of this License or out of the use or inability to use the
      Work (including but not limited to damages for loss of goodwill,
      work stoppage, computer failure or malfunction, or any and all
      other commercial damages or losses), even if such Contributor
      has been advised of the possibility of such damages.

   9. Accepting Warranty or Additional Liability. While redistributing
      the Work or Derivative Works thereof, You may choose to offer,
      and charge a fee for, acceptance of support, warranty, indemnity,
      or other liability obligations and/or rights consistent with this
      License. However, in accepting such obligations, You may act only
      on Your own behalf and on Your sole responsibility, not on behalf
      of any other Contributor, and only if You agree to indemnify,
      defend, and hold each Contributor harmless for any liability
      incurred by, or claims asserted against, such Contributor by reason
      of your accepting any such warranty or additional liability.

   END OF TERMS AND CONDITIONS

   APPENDIX: How to apply the Apache License to your work.

      To apply the Apache License to your work, attach the following
      boilerplate notice, with the fields enclosed by brackets "[]"
      replaced with your own identifying information. (Don't include
      the brackets!)  The text should be enclosed in the appropriate
      comment syntax for the file format. We also recommend that a
      file or class name and description of purpose be included on the
      same "printed page" as the copyright notice for easier
      identification within third-party archives.

   Copyright [yyyy] [name of copyright owner]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

### MIT

```text
MIT License

Copyright (c) <year> <copyright holders>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
USE OR OTHER DEALINGS IN THE SOFTWARE.
```

### BSD-3-Clause

```text
Copyright (c) <year> <owner>. 

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```

### BSD-2-Clause

```text
Copyright (c) <year> <owner> 

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```

### EDL-1.0

```text
Copyright (c) 2007, Eclipse Foundation, Inc. and its licensors.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
- Neither the name of the Eclipse Foundation, Inc. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```

### EPL-2.0

The EPL-2.0 artifacts above are consumed as unmodified binaries from Maven Central; norintegrate modifies none of them. Their source is available from the upstream projects. The full text is reproduced here because the logback JARs bundle no license text themselves.

```text
Eclipse Public License - v 2.0
THE ACCOMPANYING PROGRAM IS PROVIDED UNDER THE TERMS OF THIS ECLIPSE PUBLIC LICENSE (“AGREEMENT”). ANY USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM CONSTITUTES RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT.

1. DEFINITIONS
“Contribution” means:

a) in the case of the initial Contributor, the initial content Distributed under this Agreement, and
b) in the case of each subsequent Contributor:
i) changes to the Program, and
ii) additions to the Program;
where such changes and/or additions to the Program originate from and are Distributed by that particular Contributor. A Contribution “originates” from a Contributor if it was added to the Program by such Contributor itself or anyone acting on such Contributor's behalf. Contributions do not include changes or additions to the Program that are not Modified Works.
“Contributor” means any person or entity that Distributes the Program.

“Licensed Patents” mean patent claims licensable by a Contributor which are necessarily infringed by the use or sale of its Contribution alone or when combined with the Program.

“Program” means the Contributions Distributed in accordance with this Agreement.

“Recipient” means anyone who receives the Program under this Agreement or any Secondary License (as applicable), including Contributors.

“Derivative Works” shall mean any work, whether in Source Code or other form, that is based on (or derived from) the Program and for which the editorial revisions, annotations, elaborations, or other modifications represent, as a whole, an original work of authorship.

“Modified Works” shall mean any work in Source Code or other form that results from an addition to, deletion from, or modification of the contents of the Program, including, for purposes of clarity any new file in Source Code form that contains any contents of the Program. Modified Works shall not include works that contain only declarations, interfaces, types, classes, structures, or files of the Program solely in each case in order to link to, bind by name, or subclass the Program or Modified Works thereof.

“Distribute” means the acts of a) distributing or b) making available in any manner that enables the transfer of a copy.

“Source Code” means the form of a Program preferred for making modifications, including but not limited to software source code, documentation source, and configuration files.

“Secondary License” means either the GNU General Public License, Version 2.0, or any later versions of that license, including any exceptions or additional permissions as identified by the initial Contributor.

2. GRANT OF RIGHTS
a) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a non-exclusive, worldwide, royalty-free copyright license to reproduce, prepare Derivative Works of, publicly display, publicly perform, Distribute and sublicense the Contribution of such Contributor, if any, and such Derivative Works.
b) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a non-exclusive, worldwide, royalty-free patent license under Licensed Patents to make, use, sell, offer to sell, import and otherwise transfer the Contribution of such Contributor, if any, in Source Code or other form. This patent license shall apply to the combination of the Contribution and the Program if, at the time the Contribution is added by the Contributor, such addition of the Contribution causes such combination to be covered by the Licensed Patents. The patent license shall not apply to any other combinations which include the Contribution. No hardware per se is licensed hereunder.
c) Recipient understands that although each Contributor grants the licenses to its Contributions set forth herein, no assurances are provided by any Contributor that the Program does not infringe the patent or other intellectual property rights of any other entity. Each Contributor disclaims any liability to Recipient for claims brought by any other entity based on infringement of intellectual property rights or otherwise. As a condition to exercising the rights and licenses granted hereunder, each Recipient hereby assumes sole responsibility to secure any other intellectual property rights needed, if any. For example, if a third party patent license is required to allow Recipient to Distribute the Program, it is Recipient's responsibility to acquire that license before distributing the Program.
d) Each Contributor represents that to its knowledge it has sufficient copyright rights in its Contribution, if any, to grant the copyright license set forth in this Agreement.
e) Notwithstanding the terms of any Secondary License, no Contributor makes additional grants to any Recipient (other than those set forth in this Agreement) as a result of such Recipient's receipt of the Program under the terms of a Secondary License (if permitted under the terms of Section 3).
3. REQUIREMENTS
3.1 If a Contributor Distributes the Program in any form, then:

a) the Program must also be made available as Source Code, in accordance with section 3.2, and the Contributor must accompany the Program with a statement that the Source Code for the Program is available under this Agreement, and informs Recipients how to obtain it in a reasonable manner on or through a medium customarily used for software exchange; and
b) the Contributor may Distribute the Program under a license different than this Agreement, provided that such license:
i) effectively disclaims on behalf of all other Contributors all warranties and conditions, express and implied, including warranties or conditions of title and non-infringement, and implied warranties or conditions of merchantability and fitness for a particular purpose;
ii) effectively excludes on behalf of all other Contributors all liability for damages, including direct, indirect, special, incidental and consequential damages, such as lost profits;
iii) does not attempt to limit or alter the recipients' rights in the Source Code under section 3.2; and
iv) requires any subsequent distribution of the Program by any party to be under a license that satisfies the requirements of this section 3.
3.2 When the Program is Distributed as Source Code:

a) it must be made available under this Agreement, or if the Program (i) is combined with other material in a separate file or files made available under a Secondary License, and (ii) the initial Contributor attached to the Source Code the notice described in Exhibit A of this Agreement, then the Program may be made available under the terms of such Secondary Licenses, and
b) a copy of this Agreement must be included with each copy of the Program.
3.3 Contributors may not remove or alter any copyright, patent, trademark, attribution notices, disclaimers of warranty, or limitations of liability (‘notices’) contained within the Program from any copy of the Program which they Distribute, provided that Contributors may add their own appropriate notices.

4. COMMERCIAL DISTRIBUTION
Commercial distributors of software may accept certain responsibilities with respect to end users, business partners and the like. While this license is intended to facilitate the commercial use of the Program, the Contributor who includes the Program in a commercial product offering should do so in a manner which does not create potential liability for other Contributors. Therefore, if a Contributor includes the Program in a commercial product offering, such Contributor (“Commercial Contributor”) hereby agrees to defend and indemnify every other Contributor (“Indemnified Contributor”) against any losses, damages and costs (collectively “Losses”) arising from claims, lawsuits and other legal actions brought by a third party against the Indemnified Contributor to the extent caused by the acts or omissions of such Commercial Contributor in connection with its distribution of the Program in a commercial product offering. The obligations in this section do not apply to any claims or Losses relating to any actual or alleged intellectual property infringement. In order to qualify, an Indemnified Contributor must: a) promptly notify the Commercial Contributor in writing of such claim, and b) allow the Commercial Contributor to control, and cooperate with the Commercial Contributor in, the defense and any related settlement negotiations. The Indemnified Contributor may participate in any such claim at its own expense.

For example, a Contributor might include the Program in a commercial product offering, Product X. That Contributor is then a Commercial Contributor. If that Commercial Contributor then makes performance claims, or offers warranties related to Product X, those performance claims and warranties are such Commercial Contributor's responsibility alone. Under this section, the Commercial Contributor would have to defend claims against the other Contributors related to those performance claims and warranties, and if a court requires any other Contributor to pay any damages as a result, the Commercial Contributor must pay those damages.

5. NO WARRANTY
EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, AND TO THE EXTENT PERMITTED BY APPLICABLE LAW, THE PROGRAM IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Each Recipient is solely responsible for determining the appropriateness of using and distributing the Program and assumes all risks associated with its exercise of rights under this Agreement, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and unavailability or interruption of operations.

6. DISCLAIMER OF LIABILITY
EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, AND TO THE EXTENT PERMITTED BY APPLICABLE LAW, NEITHER RECIPIENT NOR ANY CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

7. GENERAL
If any provision of this Agreement is invalid or unenforceable under applicable law, it shall not affect the validity or enforceability of the remainder of the terms of this Agreement, and without further action by the parties hereto, such provision shall be reformed to the minimum extent necessary to make such provision valid and enforceable.

If Recipient institutes patent litigation against any entity (including a cross-claim or counterclaim in a lawsuit) alleging that the Program itself (excluding combinations of the Program with other software or hardware) infringes such Recipient's patent(s), then such Recipient's rights granted under Section 2(b) shall terminate as of the date such litigation is filed.

All Recipient's rights under this Agreement shall terminate if it fails to comply with any of the material terms or conditions of this Agreement and does not cure such failure in a reasonable period of time after becoming aware of such noncompliance. If all Recipient's rights under this Agreement terminate, Recipient agrees to cease use and distribution of the Program as soon as reasonably practicable. However, Recipient's obligations under this Agreement and any licenses granted by Recipient relating to the Program shall continue and survive.

Everyone is permitted to copy and distribute copies of this Agreement, but in order to avoid inconsistency the Agreement is copyrighted and may only be modified in the following manner. The Agreement Steward reserves the right to publish new versions (including revisions) of this Agreement from time to time. No one other than the Agreement Steward has the right to modify this Agreement. The Eclipse Foundation is the initial Agreement Steward. The Eclipse Foundation may assign the responsibility to serve as the Agreement Steward to a suitable separate entity. Each new version of the Agreement will be given a distinguishing version number. The Program (including Contributions) may always be Distributed subject to the version of the Agreement under which it was received. In addition, after a new version of the Agreement is published, Contributor may elect to Distribute the Program (including its Contributions) under the new version.

Except as expressly stated in Sections 2(a) and 2(b) above, Recipient receives no rights or licenses to the intellectual property of any Contributor under this Agreement, whether expressly, by implication, estoppel or otherwise. All rights in the Program not expressly granted under this Agreement are reserved. Nothing in this Agreement is intended to be enforceable by any entity that is not a Contributor or Recipient. No third-party beneficiary rights are created under this Agreement.

Exhibit A – Form of Secondary Licenses Notice
“This Source Code may also be made available under the following Secondary Licenses when the conditions for such availability set forth in the Eclipse Public License, v. 2.0 are satisfied: {name license(s), version(s), and exceptions or additional permissions here}.”

Simply including a copy of this Agreement, including this Exhibit A is not sufficient to license the Source Code under Secondary Licenses.

If it is not possible or desirable to put the notice in a particular file, then You may include the notice in a location (such as a LICENSE file in a relevant directory) where a recipient would be likely to look for such a notice.

You may add additional accurate notices of copyright ownership.
```

### MIT-0

Full text: https://opensource.org/license/mit-0
