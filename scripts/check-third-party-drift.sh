#!/usr/bin/env bash
# Verify that the artifact list baked into generate-third-party-notices.sh
# still matches what Gradle actually resolves onto the JVM modules'
# runtimeClasspath.
#
# Why this exists: THIRD-PARTY-NOTICES.md is generated from a fixed, reviewed
# artifact list rather than recomputed on every run, because mapping licenses
# and electing one license for dual-licensed artifacts are human judgements.
# The cost of that choice is drift — bump a dependency and the notices file
# silently describes the old graph. This script is the guard against a
# notices file that quietly stops being true.
#
# Usage:
#   scripts/check-third-party-drift.sh
#
# Exit 0 = list matches. Exit 1 = drift; the diff names what moved. On drift:
#   1. update the ARTIFACTS block in scripts/generate-third-party-notices.sh
#   2. scripts/generate-third-party-notices.sh
#   3. scripts/render-third-party-notices.py
# Step 1 is deliberately manual: it defines what ships.

set -u

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
GEN_SCRIPT="$SCRIPT_DIR/generate-third-party-notices.sh"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

# Ask Gradle for the resolved artifacts directly. The `dependencies` task's
# tree output is NOT usable here: it prints BOM/platform pseudo-artifacts that
# ship no code, and elides `(*)` conflict-resolved entries it has already
# shown elsewhere. Reading it produced three different counts on three
# attempts; resolvedArtifacts is the authoritative view of what lands in the
# fat JAR.
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
  echo "ERROR: Gradle resolution failed. Cannot verify the artifact list." >&2
  tail -20 "$WORK/gradle.err" >&2
  exit 1
fi

# Drop this project's own modules — they need no third-party attribution.
grep '^DEP' "$WORK/gradle.out" \
  | cut -f2 \
  | grep -v '^com\.norintegrate:' \
  | sort -u > "$WORK/actual.txt"

# The ARTIFACTS heredoc block from the generator.
awk '/^ARTIFACTS="/{f=1;next} f&&/^"$/{f=0} f&&NF' "$GEN_SCRIPT" \
  | sort -u > "$WORK/declared.txt"

if [ ! -s "$WORK/actual.txt" ]; then
  echo "ERROR: resolved no artifacts — the check itself is broken." >&2
  exit 1
fi

if diff -u "$WORK/declared.txt" "$WORK/actual.txt" > "$WORK/diff.txt"; then
  echo "OK: third-party artifact list matches runtimeClasspath ($(wc -l < "$WORK/actual.txt" | tr -d ' ') artifacts)."
  exit 0
fi

echo "" >&2
echo "DRIFT: THIRD-PARTY-NOTICES.md no longer describes the shipped dependencies." >&2
echo "  '-' = listed in the notices file but no longer resolved" >&2
echo "  '+' = actually shipped but missing from the notices file" >&2
echo "" >&2
grep -E '^[+-][^+-]' "$WORK/diff.txt" >&2
echo "" >&2
echo "Regenerate: see the header of scripts/check-third-party-drift.sh." >&2
exit 1
