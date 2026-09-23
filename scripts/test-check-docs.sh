#!/usr/bin/env bash
# Regression harness for scripts/check-docs.sh.
#
# For every scenario: clone the repo (current working-tree content, not
# just the last commit — see copy_tree below) into a fresh mktemp -d
# directory, optionally inject one known-bad edit, run check-docs.sh there,
# and assert both the exit code AND that the output actually contains the
# FAIL message we expect — a FAIL for the wrong reason must not count as a
# pass for this scenario. The real working tree is never touched.
#
# Portability: same target as check-docs.sh itself — macOS bash 3.2 and the
# bash shipped with ubuntu-latest. No GNU-only flags, no mapfile, no jq /
# python / node.
#
# Usage: bash scripts/test-check-docs.sh   (run from anywhere; cd's to repo root)

set -u

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

total=0
failed=0

# Portable in-place sed: BSD sed (macOS) requires an -i extension argument;
# GNU sed (ubuntu) treats a bare -i argument as the script itself. Writing
# to a temp file and moving it over behaves identically on both.
sed_inplace() {
  local expr="$1" file="$2"
  sed -E "$expr" "$file" > "$file.tmp" && mv "$file.tmp" "$file"
}

# Build a throwaway copy of the repo in $1, reflecting the CURRENT working
# tree (including any uncommitted edits — e.g. to check-docs.sh itself
# while it's being developed), not just the last commit.
#
# `git clone --local` (rather than a plain file copy) is deliberate:
# check-docs.sh itself shells out to `git ls-files` to enumerate tracked
# ADR files and *.md files, so the copy needs a real, independent .git of
# its own for those checks to see the same file list they see in the real
# repo. `--no-hardlinks` guarantees the clone's objects are fully separate
# files, so nothing done to the clone (including its eventual `rm -rf`) can
# ever reach the real .git.
#
# Uncommitted changes don't exist in a fresh clone (it checks out HEAD), so
# after cloning, every tracked file that currently differs from HEAD in the
# real working tree is overlaid on top with its live content.
copy_tree() {
  local dest="$1" f
  git clone --local --no-hardlinks --quiet "$REPO_ROOT" "$dest"
  while IFS= read -r -d '' f; do
    [ -f "$REPO_ROOT/$f" ] && cp "$REPO_ROOT/$f" "$dest/$f"
  done < <(cd "$REPO_ROOT" && git diff --name-only -z HEAD -- .)
}

# run_scenario NAME EXPECT_EXIT MUTATOR NEEDLE [NEEDLE...]
# MUTATOR is a function name (or "" for the baseline) invoked as
# "$MUTATOR tmpdir" to inject the breakage. Every NEEDLE substring must
# appear (grep -F) somewhere in check-docs.sh's combined stdout+stderr for
# the scenario to pass — this is what rules out a FAIL for the wrong
# reason.
run_scenario() {
  local name="$1" expect_exit="$2" mutator="$3"
  shift 3
  local needles=("$@")

  local tmp out rc ok=1 n
  tmp="$(mktemp -d)"
  copy_tree "$tmp"

  if [ -n "$mutator" ]; then
    "$mutator" "$tmp"
  fi

  out="$(cd "$tmp" && bash "$tmp/scripts/check-docs.sh" 2>&1)"
  rc=$?

  total=$((total + 1))

  if [ "$rc" -ne "$expect_exit" ]; then
    ok=0
  fi
  # Guard against expanding an empty array (the baseline scenario passes no
  # needles): bash < 4.4 (macOS's /bin/bash is 3.2) treats "${arr[@]}" on an
  # empty array as an unbound-variable error under `set -u`, even though
  # "${#arr[@]}" on the same array correctly reports 0.
  if [ "${#needles[@]}" -gt 0 ]; then
    for n in "${needles[@]}"; do
      if ! printf '%s' "$out" | grep -qF "$n"; then
        ok=0
      fi
    done
  fi

  if [ "$ok" -eq 1 ]; then
    echo "PASS: $name"
  else
    failed=$((failed + 1))
    echo "FAIL: $name (exit=$rc, expected=$expect_exit)"
    echo "$out" | sed 's/^/    /'
  fi

  rm -rf "$tmp"
}

# --- Scenarios -------------------------------------------------------

# Previously found bugs / edge-case guards (NOR-19), each exercising a
# distinct empty-value guard in check-docs.sh.

# (a) A blank version cell must not silently pass version_prefix_match's
# "empty claim" guard.
mutate_a() {
  sed_inplace 's/^\| Next\.js \| 15 \|/| Next.js |  |/' "$1/CLAUDE.md"
}

# (b) A shields.io badge message that's entirely a literal space ("_")
# must truncate to "" in extract_badge_value and be treated as "badge not
# found", not compared against anything.
mutate_b() {
  sed_inplace 's/Spring_Boot-4\.1-green/Spring_Boot-_-green/' "$1/README.md"
}

# (c) Same guard as (b), for the Java badge, message starting with "_".
mutate_c() {
  sed_inplace 's/Java-25_LTS-orange/Java-_LTS-orange/' "$1/README.md"
}

# (1) CLAUDE.md's ADR table missing two known ADR files.
mutate_1() {
  sed_inplace '/^\| ADR-017 \|/d' "$1/CLAUDE.md"
  sed_inplace '/^\| ADR-018 \|/d' "$1/CLAUDE.md"
}

# (2) README's ADR index missing a known ADR file.
mutate_2() {
  sed_inplace '/^\| \[ADR-019\]/d' "$1/README.md"
}

# (3) CLAUDE.md table claims a Spring Boot version that doesn't match
# build.gradle.kts.
mutate_3() {
  sed_inplace 's/\| Spring Boot \| 4\.1\.1 \|/| Spring Boot | 4.0.x |/' "$1/CLAUDE.md"
}

# (4) A README link (outside the ADR index table, so this isolates Check 2)
# pointing at a docs/adr/ file that doesn't exist.
mutate_4() {
  sed_inplace '42s/ADR-017-mcp-server-authentication-posture\.md/ADR-016-nope.md/' "$1/README.md"
}

# (5) README Spring Boot badge claims a version prefix that doesn't match
# build.gradle.kts.
mutate_5() {
  sed_inplace 's/Spring_Boot-4\.1-green/Spring_Boot-4.0-green/' "$1/README.md"
}

# (K) Kotlin badge stale relative to build.gradle.kts's kotlin("jvm") version.
mutate_k() {
  sed_inplace 's/Kotlin_2\.4-7F52FF/Kotlin_2.3-7F52FF/' "$1/README.md"
}

# (M) Unresolved merge conflict marker, in both a tracked .md file and a
# tracked non-md file.
mutate_m_md() {
  printf '<<<<<<< HEAD\n' >> "$1/CLAUDE.md"
}
mutate_m_nonmd() {
  printf '<<<<<<< HEAD\n' >> "$1/build.gradle.kts"
}

run_scenario "baseline: unmodified copy passes" \
  0 ""

run_scenario "(a) blank Next.js version cell in CLAUDE.md" \
  1 mutate_a "CLAUDE.md claims Next.js"

run_scenario "(b) blank Spring Boot badge message in README" \
  1 mutate_b "Spring Boot badge"

run_scenario "(c) Java badge message starting with _" \
  1 mutate_c "Java badge" "not found"

run_scenario "(1) CLAUDE.md ADR index missing ADR-017 and ADR-018" \
  1 mutate_1 "CLAUDE.md: ADR index is missing"

run_scenario "(2) README ADR index missing ADR-019" \
  1 mutate_2 "README.md: ADR index is missing"

run_scenario "(3) CLAUDE.md claims Spring Boot 4.0.x" \
  1 mutate_3 "CLAUDE.md claims Spring Boot"

run_scenario "(4) README link to nonexistent ADR file" \
  1 mutate_4 "does not resolve"

run_scenario "(5) README Spring Boot badge says 4.0" \
  1 mutate_5 "Spring Boot badge claims 4.0"

run_scenario "(K) Kotlin badge stale vs build.gradle.kts" \
  1 mutate_k "Kotlin badge claims 2.3"

run_scenario "(M) conflict marker in a tracked .md file" \
  1 mutate_m_md "unresolved merge conflict marker"

run_scenario "(M) conflict marker in a tracked non-md file" \
  1 mutate_m_nonmd "unresolved merge conflict marker"

echo "=================================="
echo "test-check-docs.sh: $total scenario(s), $failed failure(s)"
[ "$failed" -eq 0 ] || exit 1
exit 0
