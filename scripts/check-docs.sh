#!/usr/bin/env bash
# Detect documentation drift before it reaches main.
#
# Three checks:
#   1. ADR index completeness  — every docs/adr/ADR-*.md file is listed in
#      both README.md and CLAUDE.md's ADR tables, and vice versa.
#   2. Broken relative links   — every relative markdown link target in a
#      tracked *.md file resolves to a real path on disk.
#   3. Version claim consistency — the CLAUDE.md technology stack table
#      matches the versions actually declared in the build files.
#
# Portability: this script targets macOS bash 3.2 (the default /bin/bash on
# macOS) and the bash shipped with ubuntu-latest. Concretely that means:
#   - no GNU-only flags (grep -P, sed -i without an extension, readlink -f)
#   - no mapfile / readarray (bash 4+ only)
#   - no jq / yq / python / node dependency
#   - `read -ra` + here-strings (<<<) are used; both exist since bash 2.05b
#
# Usage: bash scripts/check-docs.sh   (run from anywhere; cd's to repo root)

set -u

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT" || exit 1

fail_count=0

fail() {
  fail_count=$((fail_count + 1))
  echo "FAIL: $1"
}

# Portable whitespace trim (no external `trim` tool required).
trim() {
  local s="$1"
  s="${s#"${s%%[![:space:]]*}"}"
  s="${s%"${s##*[![:space:]]}"}"
  printf '%s' "$s"
}

echo "== Check 1: ADR index completeness =="

# Use git ls-files so untracked scratch files under docs/adr/ are ignored.
ADR_FILES="$(git ls-files 'docs/adr/ADR-*.md' | sort)"
README_ROWS="$(grep -nE '^\| \[ADR-[0-9]+\]\(docs/adr/[^)]+\)' README.md || true)"
CLAUDE_ROWS="$(grep -nE '^\| ADR-[0-9]+ \|' CLAUDE.md || true)"

# Forward: every ADR file must be indexed in both README.md and CLAUDE.md.
while IFS= read -r adr_path; do
  [ -z "$adr_path" ] && continue
  base="$(basename "$adr_path")"
  num="$(echo "$base" | sed -E 's/^ADR-([0-9]+)-.*/\1/')"

  if ! echo "$README_ROWS" | grep -qF "[ADR-$num](docs/adr/$base)"; then
    fail "README.md: ADR index is missing $adr_path — add a row '| [ADR-$num](docs/adr/$base) | <decision> |' to the Architecture Decision Records table"
  fi

  if ! echo "$CLAUDE_ROWS" | grep -qF "| ADR-$num |"; then
    fail "CLAUDE.md: ADR index is missing $adr_path — add a row '| ADR-$num | <title> | <status> |' to the Architecture Decision Records table"
  fi
done <<< "$ADR_FILES"

# Reverse: every README index row must point at a file that actually exists.
while IFS= read -r row; do
  [ -z "$row" ] && continue
  lineno="${row%%:*}"
  file="$(echo "$row" | sed -E 's/^[0-9]+:\| \[ADR-[0-9]+\]\((docs\/adr\/[^)]+)\).*/\1/')"
  if [ ! -f "$file" ]; then
    fail "README.md:$lineno references $file, which does not exist"
  fi
done <<< "$README_ROWS"

# Reverse: every CLAUDE.md index row must correspond to an existing ADR file.
while IFS= read -r row; do
  [ -z "$row" ] && continue
  lineno="${row%%:*}"
  num="$(echo "$row" | sed -E 's/^[0-9]+:\| ADR-([0-9]+) \|.*/\1/')"
  match="$(git ls-files "docs/adr/ADR-$num-*.md")"
  if [ -z "$match" ]; then
    fail "CLAUDE.md:$lineno references ADR-$num, but no docs/adr/ADR-$num-*.md file exists"
  fi
done <<< "$CLAUDE_ROWS"

echo "== Check 2: broken relative links =="

MD_FILES="$(git ls-files '*.md')"

while IFS= read -r file; do
  [ -z "$file" ] && continue
  dir="$(dirname "$file")"
  links="$(grep -noE '\]\([^)]*\)' "$file" || true)"
  [ -z "$links" ] && continue
  while IFS= read -r entry; do
    [ -z "$entry" ] && continue
    lineno="${entry%%:*}"
    raw="${entry#*:}"
    target="${raw#\](}"
    target="${target%)}"

    case "$target" in
      http://* | https://* | mailto:* | "#"* | "") continue ;;
    esac

    # Strip a #anchor suffix — only the path part must exist on disk.
    target="${target%%#*}"
    [ -z "$target" ] && continue

    target_path="$dir/$target"
    if [ ! -e "$target_path" ]; then
      fail "$file:$lineno links to '$target', which does not resolve to an existing path ($target_path)"
    fi
  done <<< "$links"
done <<< "$MD_FILES"

echo "== Check 3: version claim consistency =="

# Extract the trimmed value of the 2nd column of a '| Label | value | ... |'
# markdown table row. Returns empty string (and caller must handle it as a
# FAIL) if the row isn't found at all — a missing row is not a silent pass.
extract_col2() {
  local label="$1" file="$2"
  local row
  row="$(grep -E "^\\| ${label} \\|" "$file" | head -1)"
  [ -z "$row" ] && return 1
  local raw
  raw="$(echo "$row" | sed -E "s/^\\| ${label} \\|([^|]*)\\|.*/\\1/")"
  trim "$raw"
  return 0
}

# --- Java: exact major version, compared against JavaLanguageVersion.of(N) ---
if claim="$(extract_col2 'Java' CLAUDE.md)"; then
  claim_major="$(echo "$claim" | grep -oE '^[0-9]+')"
  actual_line="$(grep -E 'JavaLanguageVersion\.of\([0-9]+\)' build.gradle.kts || true)"
  if [ -z "$actual_line" ]; then
    fail "build.gradle.kts: JavaLanguageVersion.of(...) not found — cannot verify the CLAUDE.md Java version claim ('$claim')"
  else
    actual="$(echo "$actual_line" | sed -E 's/.*JavaLanguageVersion\.of\(([0-9]+)\).*/\1/')"
    if [ "$claim_major" != "$actual" ]; then
      fail "CLAUDE.md claims Java $claim, but build.gradle.kts declares JavaLanguageVersion.of($actual)"
    fi
  fi
else
  fail "CLAUDE.md: technology stack table has no 'Java' row — cannot verify version claim"
fi

# --- Kotlin: exact version, compared against kotlin("jvm") version "X" ---
if claim="$(extract_col2 'Kotlin' CLAUDE.md)"; then
  actual_line="$(grep -E 'kotlin\("jvm"\) version "[0-9][^"]*"' build.gradle.kts || true)"
  if [ -z "$actual_line" ]; then
    fail 'build.gradle.kts: kotlin("jvm") version "..." not found — cannot verify the CLAUDE.md Kotlin version claim'
  else
    actual="$(echo "$actual_line" | sed -E 's/.*kotlin\("jvm"\) version "([^"]*)".*/\1/')"
    if [ "$claim" != "$actual" ]; then
      fail "CLAUDE.md claims Kotlin $claim, but build.gradle.kts declares kotlin(\"jvm\") version \"$actual\""
    fi
  fi
else
  fail "CLAUDE.md: technology stack table has no 'Kotlin' row — cannot verify version claim"
fi

# --- Spring Boot: exact version, compared against the boot plugin version ---
if claim="$(extract_col2 'Spring Boot' CLAUDE.md)"; then
  actual_line="$(grep -E 'id\("org\.springframework\.boot"\) version "[0-9][^"]*"' build.gradle.kts || true)"
  if [ -z "$actual_line" ]; then
    fail 'build.gradle.kts: id("org.springframework.boot") version "..." not found — cannot verify the CLAUDE.md Spring Boot version claim'
  else
    actual="$(echo "$actual_line" | sed -E 's/.*id\("org\.springframework\.boot"\) version "([^"]*)".*/\1/')"
    if [ "$claim" != "$actual" ]; then
      fail "CLAUDE.md claims Spring Boot $claim, but build.gradle.kts declares org.springframework.boot version \"$actual\""
    fi
  fi
else
  fail "CLAUDE.md: technology stack table has no 'Spring Boot' row — cannot verify version claim"
fi

# --- Next.js: the doc states a version PREFIX (e.g. "15"), not an exact
# version — package.json pins a full semver range (e.g. "^15.5.21"). Compare
# component-by-component for exactly as many components as the doc claims,
# so a claim of "15" passes against "15.5.21" (only the major component is
# asserted) but a claim of "4.0.x" against an actual "4.1.0" correctly FAILS
# (the 2nd component, 0 vs 1, does not match). A naive "first character"
# or substring comparison would let the latter case slip through silently;
# this must not.
if claim="$(extract_col2 'Next\.js' CLAUDE.md)"; then
  actual_raw="$(grep -E '"next": *"[^"]*"' norintegrate-web/package.json | head -1 | sed -E 's/.*"next": *"([^"]*)".*/\1/')"
  if [ -z "$actual_raw" ]; then
    fail "norintegrate-web/package.json: \"next\" dependency not found — cannot verify the CLAUDE.md Next.js version claim ('$claim')"
  else
    actual="${actual_raw#^}"
    actual="${actual#\~}"
    IFS='.' read -ra claim_parts <<< "$claim"
    IFS='.' read -ra actual_parts <<< "$actual"
    mismatch=0
    i=0
    while [ "$i" -lt "${#claim_parts[@]}" ]; do
      if [ "${claim_parts[$i]}" != "${actual_parts[$i]:-}" ]; then
        mismatch=1
        break
      fi
      i=$((i + 1))
    done
    if [ "$mismatch" -eq 1 ]; then
      fail "CLAUDE.md claims Next.js $claim, but norintegrate-web/package.json declares \"next\": \"$actual_raw\""
    fi
  fi
else
  fail "CLAUDE.md: technology stack table has no 'Next.js' row — cannot verify version claim"
fi

echo "=================================="
if [ "$fail_count" -gt 0 ]; then
  echo "check-docs.sh: $fail_count failure(s)"
  exit 1
fi

echo "check-docs.sh: all checks passed"
exit 0
