#!/usr/bin/env bash
# Collect license and NOTICE data for THIRD-PARTY-NOTICES.md.
#
# Scope: the runtimeClasspath of the three JVM Gradle modules (norintegrate-api,
# norintegrate-common, norintegrate-mcp) only. norintegrate-web's npm
# dependencies are out of scope (handled separately).
#
# What this script does NOT do: it does not compute the dependency list.
# The ARTIFACTS list below is a fixed, previously-verified snapshot (152
# entries) captured via Gradle's resolvedArtifacts API against
# runtimeClasspath for all three JVM modules, with this project's own
# norintegrate-common artifact and Maven BOM/platform pseudo-artifacts
# (jackson-bom, hibernate-platform, spring-*-bom, etc. — which declare no
# code or license of their own) excluded. Deliberately NOT derived by
# grepping `./gradlew dependencies` tree output: that approach both pulls in
# phantom BOM/platform entries and silently drops `(*)` conflict-resolved
# starters, so it undercounts and overcounts at the same time. If the
# dependency graph changes (a module's build.gradle.kts dependencies
# change), this list must be re-captured via resolvedArtifacts and the
# ARTIFACTS block below updated by hand — that step is intentionally manual
# and reviewed, since it defines what ships.
#
# For each artifact in the list, this script:
#   1. Downloads its POM from Maven Central and reads
#      <licenses><license><name>. If a POM declares no license, it walks up
#      the <parent> chain (up to 4 levels) until one is found.
#   2. For every artifact, checks whether its JAR (found in the Gradle
#      module cache) bundles a META-INF/NOTICE(.txt|.md) and, if so, saves
#      its content. This is done for all artifacts (cheap to check); only
#      the Apache-2.0-licensed ones are legally required to have their
#      NOTICE content propagated (Apache-2.0 §4(d)), which is why
#      THIRD-PARTY-NOTICES.md only reproduces NOTICE content for those.
#
# This script does NOT write THIRD-PARTY-NOTICES.md itself. License
# normalization (mapping raw POM strings like "The Apache Software License,
# Version 2.0" to an SPDX id), grouping, and selecting one license for
# dual-licensed artifacts (e.g. logback: EPL-2.0 vs LGPL-2.1-only) requires
# human review and is not automated — see build/third-party-notices/report.tsv
# for the raw data that review was based on.
#
# Requirements: curl, xmllint (libxml2), unzip.
#
# Usage:
#   scripts/generate-third-party-notices.sh
#
# Idempotent: downloaded POMs are cached under
# build/third-party-notices/poms/ and re-used on subsequent runs (delete
# that directory, or the whole build/third-party-notices/ tree, to force a
# clean re-fetch). Output:
#   build/third-party-notices/report.tsv   — gav, raw license name(s),
#                                             resolution source, pom url
#   build/third-party-notices/notices/     — extracted META-INF/NOTICE files,
#                                             one per artifact that has one

set -u

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
OUT_DIR="$REPO_ROOT/build/third-party-notices"
POM_CACHE="$OUT_DIR/poms"
NOTICE_DIR="$OUT_DIR/notices"
REPORT="$OUT_DIR/report.tsv"
GRADLE_CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/caches/modules-2/files-2.1"

# Fixed, reviewed list of runtimeClasspath artifacts. See header comment.
ARTIFACTS="
ch.qos.logback:logback-classic:1.5.34
ch.qos.logback:logback-core:1.5.34
com.ethlo.time:itu:1.14.0
com.fasterxml.jackson.core:jackson-annotations:2.21
com.fasterxml.jackson.core:jackson-core:2.21.4
com.fasterxml.jackson.core:jackson-databind:2.22.1
com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.21.4
com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.21.4
com.fasterxml:classmate:1.7.3
com.github.victools:jsonschema-generator:5.0.0
com.github.victools:jsonschema-module-jackson:5.0.0
com.github.victools:jsonschema-module-swagger-2:5.0.0
com.knuddels:jtokkit:1.1.0
com.networknt:json-schema-validator:3.0.0
com.nimbusds:nimbus-jose-jwt:10.9
com.sun.istack:istack-commons-runtime:4.1.2
com.zaxxer:HikariCP:7.0.2
commons-logging:commons-logging:1.3.6
io.micrometer:context-propagation:1.2.1
io.micrometer:micrometer-commons:1.17.0
io.micrometer:micrometer-core:1.17.0
io.micrometer:micrometer-jakarta9:1.17.0
io.micrometer:micrometer-observation:1.17.0
io.micrometer:micrometer-registry-prometheus:1.17.0
io.modelcontextprotocol.sdk:mcp-core:2.0.0
io.modelcontextprotocol.sdk:mcp-json-jackson3:2.0.0
io.modelcontextprotocol.sdk:mcp:2.0.0
io.projectreactor:reactor-core:3.8.6
io.prometheus:prometheus-metrics-config:1.5.1
io.prometheus:prometheus-metrics-core:1.5.1
io.prometheus:prometheus-metrics-exposition-formats:1.5.1
io.prometheus:prometheus-metrics-exposition-textformats:1.5.1
io.prometheus:prometheus-metrics-model:1.5.1
io.prometheus:prometheus-metrics-tracer-common:1.5.1
io.swagger.core.v3:swagger-annotations-jakarta:2.2.38
io.swagger.core.v3:swagger-annotations-jakarta:2.2.52
io.swagger.core.v3:swagger-core-jakarta:2.2.52
io.swagger.core.v3:swagger-models-jakarta:2.2.52
jakarta.activation:jakarta.activation-api:2.1.4
jakarta.annotation:jakarta.annotation-api:3.0.0
jakarta.inject:jakarta.inject-api:2.0.1
jakarta.persistence:jakarta.persistence-api:3.2.0
jakarta.transaction:jakarta.transaction-api:2.0.1
jakarta.validation:jakarta.validation-api:3.1.1
jakarta.xml.bind:jakarta.xml.bind-api:4.0.5
net.bytebuddy:byte-buddy:1.18.10
net.logstash.logback:logstash-logback-encoder:9.0
org.antlr:ST4:4.3.4
org.antlr:antlr-runtime:3.5.3
org.antlr:antlr4-runtime:4.13.2
org.apache.commons:commons-lang3:3.20.0
org.apache.logging.log4j:log4j-api:2.25.4
org.apache.logging.log4j:log4j-to-slf4j:2.25.4
org.apache.tomcat.embed:tomcat-embed-core:11.0.24
org.apache.tomcat.embed:tomcat-embed-el:11.0.24
org.apache.tomcat.embed:tomcat-embed-websocket:11.0.24
org.aspectj:aspectjweaver:1.9.25.1
org.checkerframework:checker-qual:3.55.1
org.eclipse.angus:angus-activation:2.0.3
org.glassfish.jaxb:jaxb-core:4.0.9
org.glassfish.jaxb:jaxb-runtime:4.0.9
org.glassfish.jaxb:txw2:4.0.9
org.hdrhistogram:HdrHistogram:2.2.2
org.hibernate.models:hibernate-models:1.1.1
org.hibernate.orm:hibernate-core:7.4.1.Final
org.hibernate.validator:hibernate-validator:9.1.0.Final
org.jboss.logging:jboss-logging:3.6.3.Final
org.jetbrains.kotlin:kotlin-reflect:2.4.10
org.jetbrains.kotlin:kotlin-stdlib:2.4.10
org.jetbrains:annotations:13.0
org.jspecify:jspecify:1.0.0
org.postgresql:postgresql:42.7.13
org.reactivestreams:reactive-streams:1.0.4
org.slf4j:jul-to-slf4j:2.0.18
org.slf4j:slf4j-api:2.0.18
org.snakeyaml:snakeyaml-engine:3.0.1
org.springdoc:springdoc-openapi-starter-common:3.1.0
org.springdoc:springdoc-openapi-starter-webmvc-api:3.1.0
org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0
org.springframework.ai:mcp-spring-webmvc:2.0.0
org.springframework.ai:spring-ai-autoconfigure-mcp-server-common:2.0.0
org.springframework.ai:spring-ai-autoconfigure-mcp-server-webmvc:2.0.0
org.springframework.ai:spring-ai-commons:2.0.0
org.springframework.ai:spring-ai-mcp-annotations:2.0.0
org.springframework.ai:spring-ai-mcp:2.0.0
org.springframework.ai:spring-ai-model:2.0.0
org.springframework.ai:spring-ai-starter-mcp-server-webmvc:2.0.0
org.springframework.ai:spring-ai-template-st:2.0.0
org.springframework.boot:spring-boot-actuator-autoconfigure:4.1.0
org.springframework.boot:spring-boot-actuator:4.1.0
org.springframework.boot:spring-boot-autoconfigure:4.1.0
org.springframework.boot:spring-boot-data-commons:4.1.0
org.springframework.boot:spring-boot-data-jpa:4.1.0
org.springframework.boot:spring-boot-health:4.1.0
org.springframework.boot:spring-boot-hibernate:4.1.0
org.springframework.boot:spring-boot-http-converter:4.1.0
org.springframework.boot:spring-boot-jackson:4.1.0
org.springframework.boot:spring-boot-jdbc:4.1.0
org.springframework.boot:spring-boot-jpa:4.1.0
org.springframework.boot:spring-boot-micrometer-metrics:4.1.0
org.springframework.boot:spring-boot-micrometer-observation:4.1.0
org.springframework.boot:spring-boot-persistence:4.1.0
org.springframework.boot:spring-boot-security-oauth2-resource-server:4.1.0
org.springframework.boot:spring-boot-security:4.1.0
org.springframework.boot:spring-boot-servlet:4.1.0
org.springframework.boot:spring-boot-sql:4.1.0
org.springframework.boot:spring-boot-starter-actuator:4.1.0
org.springframework.boot:spring-boot-starter-data-jpa:4.1.0
org.springframework.boot:spring-boot-starter-jackson:4.1.0
org.springframework.boot:spring-boot-starter-jdbc:4.1.0
org.springframework.boot:spring-boot-starter-logging:4.1.0
org.springframework.boot:spring-boot-starter-micrometer-metrics:4.1.0
org.springframework.boot:spring-boot-starter-oauth2-resource-server:4.1.0
org.springframework.boot:spring-boot-starter-security:4.1.0
org.springframework.boot:spring-boot-starter-tomcat-runtime:4.1.0
org.springframework.boot:spring-boot-starter-tomcat:4.1.0
org.springframework.boot:spring-boot-starter-validation:4.1.0
org.springframework.boot:spring-boot-starter-web:4.1.0
org.springframework.boot:spring-boot-starter:4.1.0
org.springframework.boot:spring-boot-tomcat:4.1.0
org.springframework.boot:spring-boot-transaction:4.1.0
org.springframework.boot:spring-boot-validation:4.1.0
org.springframework.boot:spring-boot-web-server:4.1.0
org.springframework.boot:spring-boot-webmvc:4.1.0
org.springframework.boot:spring-boot:4.1.0
org.springframework.data:spring-data-commons:4.1.0
org.springframework.data:spring-data-jpa:4.1.0
org.springframework.security:spring-security-config:7.1.0
org.springframework.security:spring-security-core:7.1.0
org.springframework.security:spring-security-crypto:7.1.0
org.springframework.security:spring-security-oauth2-core:7.1.0
org.springframework.security:spring-security-oauth2-jose:7.1.0
org.springframework.security:spring-security-oauth2-resource-server:7.1.0
org.springframework.security:spring-security-web:7.1.0
org.springframework:spring-aop:7.0.8
org.springframework:spring-aspects:7.0.8
org.springframework:spring-beans:7.0.8
org.springframework:spring-context:7.0.8
org.springframework:spring-core:7.0.8
org.springframework:spring-expression:7.0.8
org.springframework:spring-jdbc:7.0.8
org.springframework:spring-messaging:7.0.8
org.springframework:spring-orm:7.0.8
org.springframework:spring-tx:7.0.8
org.springframework:spring-web:7.0.8
org.springframework:spring-webmvc:7.0.8
org.webjars:swagger-ui:5.32.11
org.webjars:webjars-locator-lite:1.1.3
org.yaml:snakeyaml:2.6
tools.jackson.core:jackson-core:3.1.4
tools.jackson.core:jackson-databind:3.1.5
tools.jackson.dataformat:jackson-dataformat-yaml:3.1.4
"

mkdir -p "$POM_CACHE" "$NOTICE_DIR"
: > "$REPORT"

pom_url_of() {
  local group="$1" artifact="$2" version="$3" gpath
  gpath="$(printf '%s' "$group" | tr '.' '/')"
  echo "https://repo1.maven.org/maven2/${gpath}/${artifact}/${version}/${artifact}-${version}.pom"
}

# Downloads (and caches) a POM. Prints its local path on success.
fetch_pom() {
  local group="$1" artifact="$2" version="$3"
  local file="$POM_CACHE/${group}_${artifact}_${version}.pom"
  if [ ! -s "$file" ]; then
    local url
    url="$(pom_url_of "$group" "$artifact" "$version")"
    if ! curl -s --max-time 20 -f "$url" -o "${file}.tmp" 2>/dev/null; then
      rm -f "${file}.tmp"
      return 1
    fi
    mv "${file}.tmp" "$file"
  fi
  printf '%s' "$file"
}

# Prints one raw <license><name> value per line (namespace-agnostic).
extract_license_names() {
  xmllint --xpath "//*[local-name()='license']/*[local-name()='name']" "$1" 2>/dev/null \
    | sed -e 's/<name>//' -e 's/<\/name>//' \
    | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' \
    | grep -v '^$'
}

# Prints "group<TAB>artifact<TAB>version" for a POM's <parent>, if any.
extract_parent() {
  local file="$1" g a v
  g="$(xmllint --xpath "string(//*[local-name()='parent']/*[local-name()='groupId'])" "$file" 2>/dev/null)"
  a="$(xmllint --xpath "string(//*[local-name()='parent']/*[local-name()='artifactId'])" "$file" 2>/dev/null)"
  v="$(xmllint --xpath "string(//*[local-name()='parent']/*[local-name()='version'])" "$file" 2>/dev/null)"
  [ -z "$g" ] && return 1
  printf '%s\t%s\t%s' "$g" "$a" "$v"
}

# Resolves the license(s) for one gav, walking up the <parent> chain (max
# depth 4) if the POM itself declares none. Appends one row to $REPORT.
resolve_one() {
  local gav="$1" group artifact version
  group="${gav%%:*}"
  local rest="${gav#*:}"
  artifact="${rest%%:*}"
  version="${rest#*:}"

  local depth=0 cg="$group" ca="$artifact" cv="$version" pomfile names url
  while [ "$depth" -le 4 ]; do
    pomfile="$(fetch_pom "$cg" "$ca" "$cv")"
    url="$(pom_url_of "$cg" "$ca" "$cv")"
    if [ -z "$pomfile" ]; then
      printf '%s\tFETCH_FAILED\tFETCH_FAILED\t%s\n' "$gav" "$depth" "$url" >> "$REPORT"
      return
    fi
    names="$(extract_license_names "$pomfile")"
    if [ -n "$names" ]; then
      local joined src
      joined="$(printf '%s' "$names" | paste -sd';' -)"
      src="direct"
      [ "$depth" -gt 0 ] && src="parent-depth-$depth"
      printf '%s\t%s\t%s\t%s\n' "$gav" "$joined" "$src" "$url" >> "$REPORT"
      return
    fi
    local parent
    parent="$(extract_parent "$pomfile")"
    if [ -z "$parent" ]; then
      printf '%s\tUNRESOLVED\tUNRESOLVED\t%s\n' "$gav" "$url" >> "$REPORT"
      return
    fi
    cg="$(printf '%s' "$parent" | cut -f1)"
    ca="$(printf '%s' "$parent" | cut -f2)"
    cv="$(printf '%s' "$parent" | cut -f3)"
    depth=$((depth + 1))
  done
  printf '%s\tUNRESOLVED_DEPTH_EXCEEDED\tUNRESOLVED\t%s\n' "$gav" "$url" >> "$REPORT"
}

# Finds the artifact's jar in the Gradle module cache and, if it bundles a
# META-INF/NOTICE(.txt|.md), extracts it to $NOTICE_DIR.
extract_notice() {
  local gav="$1" group artifact version
  group="${gav%%:*}"
  local rest="${gav#*:}"
  artifact="${rest%%:*}"
  version="${rest#*:}"
  local jar
  jar="$(find "$GRADLE_CACHE/$group/$artifact/$version" -type f -name "${artifact}-${version}.jar" 2>/dev/null | head -1)"
  [ -z "$jar" ] && return 1
  local entry
  # Case-insensitive on purpose: the JAR spec does not fix the casing, and
  # Spring Framework ships META-INF/notice.txt in lower case. Matching only
  # the upper-case form silently dropped all 12 spring-* artifacts — every
  # one of them Apache-2.0, i.e. exactly the artifacts whose NOTICE text
  # section 4(d) requires us to carry.
  entry="$(unzip -l "$jar" 2>/dev/null | tr -s ' ' | cut -d' ' -f5 | grep -iE '^META-INF/NOTICE(\.txt|\.md)?$' | head -1)"
  [ -z "$entry" ] && return 1
  local safe_name notice_file
  safe_name="$(printf '%s' "$gav" | tr ':/' '__')"
  notice_file="$NOTICE_DIR/${safe_name}.NOTICE.txt"
  unzip -p "$jar" "$entry" > "$notice_file" 2>/dev/null
  [ -s "$notice_file" ] || rm -f "$notice_file"
}

count=0
total=0
for gav in $ARTIFACTS; do
  [ -z "$gav" ] && continue
  total=$((total + 1))
done

for gav in $ARTIFACTS; do
  [ -z "$gav" ] && continue
  count=$((count + 1))
  resolve_one "$gav"
  extract_notice "$gav"
  printf '[%d/%d] %s\n' "$count" "$total" "$gav" >&2
done

unresolved="$(grep -cE 'UNRESOLVED|FETCH_FAILED' "$REPORT" || true)"
notice_count="$(find "$NOTICE_DIR" -type f -name '*.NOTICE.txt' | wc -l | tr -d ' ')"

echo "" >&2
echo "Resolved $((total - unresolved))/$total artifacts; $unresolved need manual follow-up." >&2
echo "Extracted $notice_count META-INF/NOTICE file(s) to $NOTICE_DIR" >&2
echo "Report: $REPORT" >&2

# Exit non-zero when any artifact's license could not be established, so a
# caller (or CI) fails loudly instead of rendering a notices file with holes
# in it. A network hiccup and a genuinely unlicensed POM both land here; the
# report tells them apart. Re-running is safe — resolved POMs are cached.
if [ "$unresolved" -gt 0 ]; then
  echo "" >&2
  echo "ERROR: $unresolved artifact(s) unresolved — see $REPORT." >&2
  echo "Do not render THIRD-PARTY-NOTICES.md until every artifact resolves." >&2
  exit 1
fi
