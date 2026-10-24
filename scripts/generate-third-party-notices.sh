#!/usr/bin/env bash
# Collect license and NOTICE data for THIRD-PARTY-NOTICES.md.
#
# Scope: the runtimeClasspath of the three JVM Gradle modules (norintegrate-api,
# norintegrate-common, norintegrate-mcp) only. norintegrate-web's npm
# dependencies are out of scope (handled separately).
#
# What this script does NOT do: it does not decide WHICH artifacts ship.
# The ARTIFACTS list below is a fixed, reviewed set of 151 `group:artifact`
# coordinates, with this project's own norintegrate-common artifact and Maven
# BOM/platform pseudo-artifacts (jackson-bom, hibernate-platform,
# spring-*-bom, etc. — which declare no code or license of their own)
# excluded. If the dependency graph gains or loses an artifact, this block
# must be updated by hand — that step is intentionally manual and reviewed,
# since it defines what ships.
#
# What this script DOES compute, on every run: each artifact's VERSION. It
# resolves runtimeClasspath through scripts/lib/resolve-runtime-artifacts.sh
# and fetches POMs and jars for whatever versions are live right now, then
# asserts the resolved `group:artifact` set equals ARTIFACTS below.
#
# Versions are deliberately absent from ARTIFACTS (ADR-022). Recording them
# made every routine dependency bump invalidate a list that can only be
# repaired with the network and a populated Gradle cache — which Dependabot
# has neither of, so no Dependabot PR could pass
# scripts/check-third-party-drift.sh, which .github/workflows/docs.yml runs
# on every push. Resolving versions live instead means the version data used
# for generation is never stale, while the reviewed part (the artifact set)
# stays a human decision.
#
# One `group:artifact` can resolve to more than one version at once — the
# three modules' runtimeClasspaths are unioned, and e.g.
# io.swagger.core.v3:swagger-annotations-jakarta currently resolves at two
# versions. Every resolved version is fetched and reported;
# render-third-party-notices.py groups them back to one entry and fails if
# they disagree on a license.
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
#   build/third-party-notices/report.tsv   — group:artifact, version, raw
#                                             license name(s), resolution
#                                             source, pom url
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
ch.qos.logback:logback-classic
ch.qos.logback:logback-core
com.ethlo.time:itu
com.fasterxml.jackson.core:jackson-annotations
com.fasterxml.jackson.core:jackson-core
com.fasterxml.jackson.core:jackson-databind
com.fasterxml.jackson.dataformat:jackson-dataformat-yaml
com.fasterxml.jackson.datatype:jackson-datatype-jsr310
com.fasterxml:classmate
com.github.victools:jsonschema-generator
com.github.victools:jsonschema-module-jackson
com.github.victools:jsonschema-module-swagger-2
com.knuddels:jtokkit
com.networknt:json-schema-validator
com.nimbusds:nimbus-jose-jwt
com.sun.istack:istack-commons-runtime
com.zaxxer:HikariCP
commons-logging:commons-logging
io.micrometer:context-propagation
io.micrometer:micrometer-commons
io.micrometer:micrometer-core
io.micrometer:micrometer-jakarta9
io.micrometer:micrometer-observation
io.micrometer:micrometer-registry-prometheus
io.modelcontextprotocol.sdk:mcp
io.modelcontextprotocol.sdk:mcp-core
io.modelcontextprotocol.sdk:mcp-json-jackson3
io.projectreactor:reactor-core
io.prometheus:prometheus-metrics-config
io.prometheus:prometheus-metrics-core
io.prometheus:prometheus-metrics-exposition-formats
io.prometheus:prometheus-metrics-exposition-textformats
io.prometheus:prometheus-metrics-model
io.prometheus:prometheus-metrics-tracer-common
io.swagger.core.v3:swagger-annotations-jakarta
io.swagger.core.v3:swagger-core-jakarta
io.swagger.core.v3:swagger-models-jakarta
jakarta.activation:jakarta.activation-api
jakarta.annotation:jakarta.annotation-api
jakarta.inject:jakarta.inject-api
jakarta.persistence:jakarta.persistence-api
jakarta.transaction:jakarta.transaction-api
jakarta.validation:jakarta.validation-api
jakarta.xml.bind:jakarta.xml.bind-api
net.bytebuddy:byte-buddy
net.logstash.logback:logstash-logback-encoder
org.antlr:ST4
org.antlr:antlr-runtime
org.antlr:antlr4-runtime
org.apache.commons:commons-lang3
org.apache.logging.log4j:log4j-api
org.apache.logging.log4j:log4j-to-slf4j
org.apache.tomcat.embed:tomcat-embed-core
org.apache.tomcat.embed:tomcat-embed-el
org.apache.tomcat.embed:tomcat-embed-websocket
org.aspectj:aspectjweaver
org.checkerframework:checker-qual
org.eclipse.angus:angus-activation
org.glassfish.jaxb:jaxb-core
org.glassfish.jaxb:jaxb-runtime
org.glassfish.jaxb:txw2
org.hdrhistogram:HdrHistogram
org.hibernate.models:hibernate-models
org.hibernate.orm:hibernate-core
org.hibernate.validator:hibernate-validator
org.jboss.logging:jboss-logging
org.jetbrains.kotlin:kotlin-reflect
org.jetbrains.kotlin:kotlin-stdlib
org.jetbrains:annotations
org.jspecify:jspecify
org.postgresql:postgresql
org.reactivestreams:reactive-streams
org.slf4j:jul-to-slf4j
org.slf4j:slf4j-api
org.snakeyaml:snakeyaml-engine
org.springdoc:springdoc-openapi-starter-common
org.springdoc:springdoc-openapi-starter-webmvc-api
org.springdoc:springdoc-openapi-starter-webmvc-ui
org.springframework.ai:mcp-spring-webmvc
org.springframework.ai:spring-ai-autoconfigure-mcp-server-common
org.springframework.ai:spring-ai-autoconfigure-mcp-server-webmvc
org.springframework.ai:spring-ai-commons
org.springframework.ai:spring-ai-mcp
org.springframework.ai:spring-ai-mcp-annotations
org.springframework.ai:spring-ai-model
org.springframework.ai:spring-ai-starter-mcp-server-webmvc
org.springframework.ai:spring-ai-template-st
org.springframework.boot:spring-boot
org.springframework.boot:spring-boot-actuator
org.springframework.boot:spring-boot-actuator-autoconfigure
org.springframework.boot:spring-boot-autoconfigure
org.springframework.boot:spring-boot-data-commons
org.springframework.boot:spring-boot-data-jpa
org.springframework.boot:spring-boot-health
org.springframework.boot:spring-boot-hibernate
org.springframework.boot:spring-boot-http-converter
org.springframework.boot:spring-boot-jackson
org.springframework.boot:spring-boot-jdbc
org.springframework.boot:spring-boot-jpa
org.springframework.boot:spring-boot-micrometer-metrics
org.springframework.boot:spring-boot-micrometer-observation
org.springframework.boot:spring-boot-persistence
org.springframework.boot:spring-boot-security
org.springframework.boot:spring-boot-security-oauth2-resource-server
org.springframework.boot:spring-boot-servlet
org.springframework.boot:spring-boot-sql
org.springframework.boot:spring-boot-starter
org.springframework.boot:spring-boot-starter-actuator
org.springframework.boot:spring-boot-starter-data-jpa
org.springframework.boot:spring-boot-starter-jackson
org.springframework.boot:spring-boot-starter-jdbc
org.springframework.boot:spring-boot-starter-logging
org.springframework.boot:spring-boot-starter-micrometer-metrics
org.springframework.boot:spring-boot-starter-oauth2-resource-server
org.springframework.boot:spring-boot-starter-security
org.springframework.boot:spring-boot-starter-tomcat
org.springframework.boot:spring-boot-starter-tomcat-runtime
org.springframework.boot:spring-boot-starter-validation
org.springframework.boot:spring-boot-starter-web
org.springframework.boot:spring-boot-tomcat
org.springframework.boot:spring-boot-transaction
org.springframework.boot:spring-boot-validation
org.springframework.boot:spring-boot-web-server
org.springframework.boot:spring-boot-webmvc
org.springframework.data:spring-data-commons
org.springframework.data:spring-data-jpa
org.springframework.security:spring-security-config
org.springframework.security:spring-security-core
org.springframework.security:spring-security-crypto
org.springframework.security:spring-security-oauth2-core
org.springframework.security:spring-security-oauth2-jose
org.springframework.security:spring-security-oauth2-resource-server
org.springframework.security:spring-security-web
org.springframework:spring-aop
org.springframework:spring-aspects
org.springframework:spring-beans
org.springframework:spring-context
org.springframework:spring-core
org.springframework:spring-expression
org.springframework:spring-jdbc
org.springframework:spring-messaging
org.springframework:spring-orm
org.springframework:spring-tx
org.springframework:spring-web
org.springframework:spring-webmvc
org.webjars:swagger-ui
org.webjars:webjars-locator-lite
org.yaml:snakeyaml
tools.jackson.core:jackson-core
tools.jackson.core:jackson-databind
tools.jackson.dataformat:jackson-dataformat-yaml
"

# Resolve the live versions and assert the shipped set still matches the
# reviewed one above. Checking it here as well as in
# scripts/check-third-party-drift.sh is on purpose: this script writes a
# legal document, and it must not quietly describe a different set of
# artifacts than the one that was reviewed.
RESOLVED_GAVS="$(bash "$SCRIPT_DIR/lib/resolve-runtime-artifacts.sh")" || exit 1

DECLARED_SET="$(printf '%s\n' "$ARTIFACTS" | grep -v '^$' | sort -u)"
RESOLVED_SET="$(printf '%s\n' "$RESOLVED_GAVS" | sed -E 's/:[^:]+$//' | sort -u)"

if [ "$DECLARED_SET" != "$RESOLVED_SET" ]; then
  echo "" >&2
  echo "ERROR: the reviewed ARTIFACTS set no longer matches runtimeClasspath." >&2
  echo "  '-' = reviewed but no longer resolved   '+' = resolved but not reviewed" >&2
  diff -u <(printf '%s\n' "$DECLARED_SET") <(printf '%s\n' "$RESOLVED_SET") \
    | grep -E '^[+-][^+-]' >&2
  echo "" >&2
  echo "Update the ARTIFACTS block above (a reviewed decision), then re-run." >&2
  exit 1
fi

# The POM cache is keyed by GAV and worth keeping across runs, but extracted
# NOTICE files are not: they are named per GAV and nothing prunes the ones
# whose version has since moved on. Left in place, a version bump renders a
# notices file that credits both the old and the new artifact — and the drift
# check would not catch it, since it compares the artifact set only.
# Re-extraction is cheap: it reads jars already in the Gradle cache.
rm -rf "$NOTICE_DIR"
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
      # 5 columns to match every other row written to $REPORT:
      # group:artifact, version, raw license, resolution source, pom url. An
      # earlier format string had only 2 "%s" for 3 args, so printf silently
      # recycled it — writing a bogus extra line instead of one well-formed
      # row.
      printf '%s\t%s\tFETCH_FAILED\tFETCH_FAILED(depth-%s)\t%s\n' "$group:$artifact" "$version" "$depth" "$url" >> "$REPORT"
      return
    fi
    names="$(extract_license_names "$pomfile")"
    if [ -n "$names" ]; then
      local joined src
      joined="$(printf '%s' "$names" | paste -sd';' -)"
      src="direct"
      [ "$depth" -gt 0 ] && src="parent-depth-$depth"
      printf '%s\t%s\t%s\t%s\t%s\n' "$group:$artifact" "$version" "$joined" "$src" "$url" >> "$REPORT"
      return
    fi
    local parent
    parent="$(extract_parent "$pomfile")"
    if [ -z "$parent" ]; then
      printf '%s\t%s\tUNRESOLVED\tUNRESOLVED\t%s\n' "$group:$artifact" "$version" "$url" >> "$REPORT"
      return
    fi
    cg="$(printf '%s' "$parent" | cut -f1)"
    ca="$(printf '%s' "$parent" | cut -f2)"
    cv="$(printf '%s' "$parent" | cut -f3)"
    depth=$((depth + 1))
  done
  printf '%s\t%s\tUNRESOLVED_DEPTH_EXCEEDED\tUNRESOLVED\t%s\n' "$group:$artifact" "$version" "$url" >> "$REPORT"
}

# Finds the artifact's jar in the Gradle module cache and, if it bundles a
# META-INF/NOTICE(.txt|.md), extracts it to $NOTICE_DIR. Returns 2 when the
# jar itself is missing, which is not the same as "has no NOTICE": resolving
# the dependency graph (as check-third-party-drift.sh does) caches POMs but
# not jars, and treating that as "no NOTICE" once silently dropped ~30
# Apache-2.0 NOTICE sections from a regenerated file.
extract_notice() {
  local gav="$1" group artifact version
  group="${gav%%:*}"
  local rest="${gav#*:}"
  artifact="${rest%%:*}"
  version="${rest#*:}"
  local jar
  jar="$(find "$GRADLE_CACHE/$group/$artifact/$version" -type f -name "${artifact}-${version}.jar" 2>/dev/null | head -1)"
  [ -z "$jar" ] && return 2
  local entry
  # Case-insensitive on purpose: the JAR spec does not fix the casing, and
  # Spring Framework ships META-INF/notice.txt in lower case. Matching only
  # the upper-case form silently dropped all 12 spring-* artifacts — every
  # one of them Apache-2.0, i.e. exactly the artifacts whose NOTICE text
  # section 4(d) requires us to carry.
  entry="$(unzip -l "$jar" 2>/dev/null | tr -s ' ' | cut -d' ' -f5 | grep -iE '^META-INF/NOTICE(\.txt|\.md)?$' | head -1)"
  [ -z "$entry" ] && return 1
  # Encode the gav into a filesystem-safe name. Using "_" here would be
  # ambiguous: an artifact whose own name or version contains "_" (e.g.
  # some Kotlin/Scala-published artifacts) would decode back to the wrong
  # gav. "+" and "~" are not valid inside a Maven groupId/artifactId/version,
  # so this mapping is unambiguous and losslessly reversible — see the
  # matching decode in render-third-party-notices.py's collect_notices().
  local safe_name notice_file
  safe_name="$(printf '%s' "$gav" | tr ':/' '+~')"
  notice_file="$NOTICE_DIR/${safe_name}.NOTICE.txt"
  unzip -p "$jar" "$entry" > "$notice_file" 2>/dev/null
  [ -s "$notice_file" ] || rm -f "$notice_file"
}

count=0
total=0
for gav in $RESOLVED_GAVS; do
  [ -z "$gav" ] && continue
  total=$((total + 1))
done

missing_jars=""
for gav in $RESOLVED_GAVS; do
  [ -z "$gav" ] && continue
  count=$((count + 1))
  resolve_one "$gav"
  extract_notice "$gav"
  [ $? -eq 2 ] && missing_jars="${missing_jars}${gav}
"
  printf '[%d/%d] %s\n' "$count" "$total" "$gav" >&2
done

unresolved="$(grep -cE 'UNRESOLVED|FETCH_FAILED' "$REPORT" || true)"
notice_count="$(find "$NOTICE_DIR" -type f -name '*.NOTICE.txt' | wc -l | tr -d ' ')"
missing_count="$(printf '%s' "$missing_jars" | grep -c . || true)"

echo "" >&2
echo "Resolved $((total - unresolved))/$total artifacts; $unresolved need manual follow-up." >&2
echo "Extracted $notice_count META-INF/NOTICE file(s) to $NOTICE_DIR" >&2
echo "Report: $REPORT" >&2

# A missing jar means its NOTICE could not be checked, so the rendered file
# would silently lack any NOTICE that jar carries. Fail instead.
if [ "$missing_count" -gt 0 ]; then
  echo "" >&2
  echo "ERROR: $missing_count artifact jar(s) not found in $GRADLE_CACHE:" >&2
  printf '%s' "$missing_jars" | sed 's/^/  /' >&2
  echo "Download them first, e.g.:" >&2
  echo "  ./gradlew :norintegrate-api:bootJar :norintegrate-mcp:bootJar :norintegrate-common:jar -x test" >&2
  echo "Do not render THIRD-PARTY-NOTICES.md until every jar is present." >&2
  exit 1
fi

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
