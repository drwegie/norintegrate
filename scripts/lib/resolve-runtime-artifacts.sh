#!/usr/bin/env bash
# Print the third-party artifacts actually resolved onto the JVM modules'
# runtimeClasspath, one "group:artifact:version" per line, sorted and
# de-duplicated, with this project's own modules dropped.
#
# Shared by scripts/check-third-party-drift.sh and
# scripts/generate-third-party-notices.sh so there is exactly one definition
# of "what ships" — the same single-source-of-truth split the web side uses
# (scripts/lib/third-party-notices-web-inventory.js).
#
# The `dependencies` task's tree output is NOT usable here: it prints
# BOM/platform pseudo-artifacts that ship no code, and elides `(*)`
# conflict-resolved entries it has already shown elsewhere. Reading it
# produced three different counts on three attempts; resolvedArtifacts is the
# authoritative view of what lands in the fat JAR.
#
# Usage:
#   bash scripts/lib/resolve-runtime-artifacts.sh              # group:artifact:version
#   bash scripts/lib/resolve-runtime-artifacts.sh --no-version # group:artifact
#
# Exit 1 (with Gradle's stderr tail) if resolution fails or yields nothing.

set -u

LIB_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$LIB_DIR/../.." && pwd)"

STRIP_VERSION=0
if [ "${1:-}" = "--no-version" ]; then
  STRIP_VERSION=1
fi

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

cat > "$WORK/deps.init.gradle" <<'EOF'
allprojects {
    tasks.register("printRuntimeDepsForDriftCheck") {
        doLast {
            def cfg = configurations.findByName("runtimeClasspath")
            if (cfg == null || !cfg.canBeResolved) return
            cfg.resolvedConfiguration.resolvedArtifacts.each { a ->
                def id = a.moduleVersion.id
                println "DEP\t${id.group}:${id.name}:${id.version}"
            }
        }
    }
}
EOF

echo "Resolving runtimeClasspath for the JVM modules..." >&2
if ! "$REPO_ROOT/gradlew" -q -p "$REPO_ROOT" \
  --init-script "$WORK/deps.init.gradle" \
  :norintegrate-api:printRuntimeDepsForDriftCheck \
  :norintegrate-common:printRuntimeDepsForDriftCheck \
  :norintegrate-mcp:printRuntimeDepsForDriftCheck \
  > "$WORK/gradle.out" 2>"$WORK/gradle.err"; then
  echo "ERROR: Gradle resolution failed. Cannot determine what ships." >&2
  tail -20 "$WORK/gradle.err" >&2
  exit 1
fi

# Drop this project's own modules — they need no third-party attribution.
grep '^DEP' "$WORK/gradle.out" \
  | cut -f2 \
  | grep -v '^com\.norintegrate:' \
  > "$WORK/gav.txt"

if [ "$STRIP_VERSION" -eq 1 ]; then
  sed -E 's/:[^:]+$//' "$WORK/gav.txt" | sort -u > "$WORK/out.txt"
else
  sort -u "$WORK/gav.txt" > "$WORK/out.txt"
fi

if [ ! -s "$WORK/out.txt" ]; then
  echo "ERROR: resolved no artifacts — the resolver itself is broken." >&2
  exit 1
fi

cat "$WORK/out.txt"
