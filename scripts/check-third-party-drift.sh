#!/usr/bin/env bash
# Verify that the artifact list baked into generate-third-party-notices.sh
# still matches what Gradle actually resolves onto the JVM modules'
# runtimeClasspath.
#
# Why this exists: THIRD-PARTY-NOTICES.md is generated from a fixed, reviewed
# artifact list rather than recomputed on every run, because mapping licenses
# and electing one license for dual-licensed artifacts are human judgements.
# The cost of that choice is drift — add or drop a dependency and the notices
# file silently describes the old graph. This script is the guard against a
# notices file that quietly stops being true.
#
# Granularity: group:artifact, NOT group:artifact:version (ADR-022). The
# reviewed list records WHICH artifacts ship, which is the human judgement;
# their versions are resolved live from Gradle by the generator, so they are
# never recorded anywhere a bump could invalidate. Comparing versions here
# meant every routine dependency bump failed this check — and since
# .github/workflows/docs.yml runs it on every push, and regenerating needs
# the network and a populated Gradle cache, no Dependabot PR could ever go
# green on its own. Now the check fires when the artifact SET changes, which
# is when the licence mapping and elections actually need re-reviewing.
#
# Usage:
#   scripts/check-third-party-drift.sh
#
# Exit 0 = list matches. Exit 1 = drift; the diff names what moved. On drift:
#   1. update the ARTIFACTS block in scripts/generate-third-party-notices.sh
#   2. scripts/generate-third-party-notices.sh — needs the jars in the Gradle
#      cache, which this check does not download; if it reports missing jars,
#      run ./gradlew :norintegrate-api:bootJar :norintegrate-mcp:bootJar
#      :norintegrate-common:jar -x test first
#   3. scripts/render-third-party-notices.py
# Step 1 is deliberately manual: it defines what ships.

set -u

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
GEN_SCRIPT="$SCRIPT_DIR/generate-third-party-notices.sh"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

# What actually ships, at group:artifact granularity. Shared with
# scripts/generate-third-party-notices.sh — see that resolver's header for
# why resolvedArtifacts is used rather than the `dependencies` tree output.
if ! bash "$SCRIPT_DIR/lib/resolve-runtime-artifacts.sh" --no-version \
  > "$WORK/actual.txt"; then
  exit 1
fi

# The ARTIFACTS heredoc block from the generator (group:artifact, no version).
awk '/^ARTIFACTS="/{f=1;next} f&&/^"$/{f=0} f&&NF' "$GEN_SCRIPT" \
  | sort -u > "$WORK/declared.txt"

if diff -u "$WORK/declared.txt" "$WORK/actual.txt" > "$WORK/diff.txt"; then
  echo "OK: third-party artifact list matches runtimeClasspath ($(wc -l < "$WORK/actual.txt" | tr -d ' ') artifacts, compared at group:artifact)."
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
