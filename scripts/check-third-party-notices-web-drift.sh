#!/usr/bin/env bash
# Verify that THIRD-PARTY-NOTICES-WEB.md's recorded inventories still match
# what actually resolves in norintegrate-web/package-lock.json.
#
# Why this exists: THIRD-PARTY-NOTICES-WEB.md documents both the LGPL-covered
# `@img/sharp-libvips-*` family sharp pulls in (copyleft-inventory) and, since
# NOR-31, the permissive/CC-BY-4.0/0BSD runtime superset (permissive-inventory).
# Unlike THIRD-PARTY-NOTICES.md (JVM side), the prose in this file is
# hand-maintained rather than generated, because it has to explain the
# *combination* (dynamic linking, shared-library replacement) in prose that a
# script cannot produce. That means nothing regenerates the prose automatically
# when `sharp`, `next`, `next-auth`, or `next-intl` get bumped — this script is
# the guard against the two machine-readable blocks quietly going stale.
#
# What counts as "copyleft": any non-dev package in package-lock.json whose
# `license` field contains one of LGPL, GPL, AGPL, MPL, EPL, CDDL, EUPL, OSL,
# CC-BY-SA (case-insensitive substring match on the SPDX expression), or whose
# license is missing entirely (an unknown license is treated as "assume the
# worst" rather than silently skipped). Every platform/os variant is included
# (e.g. every @img/sharp-libvips-<platform>) — this block is not filtered to
# the build host.
#
# What counts as "permissive": every other non-dev package (SUPERSET of the
# runtime dependency tree, plus a couple of documented force-included
# devDependencies — see FORCE_INCLUDE), minus packages that cannot ship
# inside docker/web.Dockerfile's runtime image (node:*-alpine — fixed to
# linux + musl libc, independent of whatever host/CI runner generates or
# checks this file), minus the build-only exclusion list. See
# scripts/lib/third-party-notices-web-inventory.js for the exact rules (shared
# with scripts/generate-third-party-notices-web.sh and
# scripts/check-third-party-notices-web-build.sh so the superset/exclusion
# logic lives in one place).
#
# This script also checks that the Entries table in THIRD-PARTY-NOTICES-WEB.md
# lists exactly the same name@version set as the permissive-inventory block —
# the table is generated from the same data, but nothing stops someone from
# hand-editing just one of the two, so this catches that drift too.
#
# Usage:
#   scripts/check-third-party-notices-web-drift.sh
#
# Exit 0 = both recorded lists match their actual counterparts.
# Exit 1 = drift; a unified diff is printed showing what moved. On drift:
#   1. re-run `scripts/generate-third-party-notices-web.sh` to see/apply the
#      new permissive-inventory block (or the node inventory script by hand
#      for copyleft)
#   2. update THIRD-PARTY-NOTICES-WEB.md: the relevant tables, the
#      machine-readable inventory block(s), and the "Generated/verified" date
#      — and re-check the prose still describes how the packages combine with
#      norintegrate-web (this is a human judgement call, not automatable)
#
# Requirements: node (already a web CI dependency).

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LOCKFILE="$REPO_ROOT/norintegrate-web/package-lock.json"
NOTICES="$REPO_ROOT/THIRD-PARTY-NOTICES-WEB.md"
INVENTORY_SCRIPT="$SCRIPT_DIR/lib/third-party-notices-web-inventory.js"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

if [ ! -f "$LOCKFILE" ]; then
  echo "ERROR: $LOCKFILE not found." >&2
  exit 1
fi
if [ ! -f "$NOTICES" ]; then
  echo "ERROR: $NOTICES not found." >&2
  exit 1
fi

check_block() {
  local kind="$1" marker="$2"
  node "$INVENTORY_SCRIPT" "$kind" | sort > "$WORK/actual-$kind.txt"

  awk -v m="$marker" \
    '$0 ~ "<!-- " m ":start -->" {f=1;next} $0 ~ "<!-- " m ":end -->" {f=0} f&&NF' \
    "$NOTICES" | sort > "$WORK/recorded-$kind.txt"

  if [ ! -s "$WORK/actual-$kind.txt" ]; then
    echo "ERROR: found no $kind packages in $LOCKFILE — the check itself is broken." >&2
    exit 1
  fi

  if [ ! -s "$WORK/recorded-$kind.txt" ]; then
    echo "ERROR: no $marker block found (or it is empty) in $NOTICES." >&2
    exit 1
  fi

  if diff -u "$WORK/recorded-$kind.txt" "$WORK/actual-$kind.txt" > "$WORK/diff-$kind.txt"; then
    echo "OK: $(wc -l < "$WORK/actual-$kind.txt" | tr -d ' ') $kind packages match"
    return 0
  fi

  echo "" >&2
  echo "DRIFT: THIRD-PARTY-NOTICES-WEB.md's $marker block no longer matches" >&2
  echo "norintegrate-web/package-lock.json." >&2
  echo "  '-' = recorded in the notices file but no longer present/$kind" >&2
  echo "  '+' = actually $kind but missing from the notices file" >&2
  echo "" >&2
  cat "$WORK/diff-$kind.txt" >&2
  echo "" >&2
  return 1
}

check_entries_table_matches_permissive_inventory() {
  # Entries table rows look like: | `name` | version | license | copyright |
  # Scoped to the "### Entries" section only (other sections of this file —
  # e.g. the copyleft "Artifacts shipped" tables — also have `name` |
  # version-shaped rows, just not the ones this check cares about).
  awk '
    /^### Entries$/ {capture=1; next}
    capture && /^### / {capture=0}
    capture && /^\| `/ {
      line=$0
      sub(/^\| `/, "", line)
      split(line, parts, "`")
      name=parts[1]
      rest=parts[2]
      sub(/^ \| /, "", rest)
      sub(/ \|.*$/, "", rest)
      print name "@" rest
    }
  ' "$NOTICES" | sort > "$WORK/entries-table.txt"

  awk '/<!-- permissive-inventory:start -->/{f=1;next} /<!-- permissive-inventory:end -->/{f=0} f&&NF {print $1}' \
    "$NOTICES" | sort > "$WORK/permissive-block-ids.txt"

  if [ ! -s "$WORK/entries-table.txt" ]; then
    echo "ERROR: no rows found under a \"### Entries\" heading in $NOTICES — the check itself is broken." >&2
    exit 1
  fi

  if diff -u "$WORK/permissive-block-ids.txt" "$WORK/entries-table.txt" > "$WORK/diff-entries.txt"; then
    echo "OK: Entries table matches the permissive-inventory block ($(wc -l < "$WORK/entries-table.txt" | tr -d ' ') packages)"
    return 0
  fi

  echo "" >&2
  echo "DRIFT: THIRD-PARTY-NOTICES-WEB.md's \"### Entries\" table lists a different" >&2
  echo "name@version set than its permissive-inventory block." >&2
  echo "  '-' = in the permissive-inventory block but not the Entries table" >&2
  echo "  '+' = in the Entries table but not the permissive-inventory block" >&2
  echo "" >&2
  cat "$WORK/diff-entries.txt" >&2
  echo "" >&2
  return 1
}

status=0
check_block copyleft copyleft-inventory || status=1
check_block permissive permissive-inventory || status=1
check_entries_table_matches_permissive_inventory || status=1

if [ "$status" -ne 0 ]; then
  echo "Update THIRD-PARTY-NOTICES-WEB.md — see the header of this script." >&2
  exit 1
fi

exit 0
