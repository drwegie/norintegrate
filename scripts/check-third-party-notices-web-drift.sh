#!/usr/bin/env bash
# Verify that THIRD-PARTY-NOTICES-WEB.md's recorded copyleft inventory still
# matches what actually resolves in norintegrate-web/package-lock.json.
#
# Why this exists: THIRD-PARTY-NOTICES-WEB.md documents the LGPL-covered
# `@img/sharp-libvips-*` packages sharp pulls in. Unlike THIRD-PARTY-NOTICES.md
# (JVM side), this file is hand-maintained rather than generated, because it
# has to explain the *combination* (dynamic linking, shared-library
# replacement) in prose that a script cannot produce. That means nothing
# regenerates it automatically when `sharp` or `next` gets bumped — this
# script is the guard against it quietly going stale.
#
# What counts as "copyleft" here: any non-dev package in package-lock.json
# whose `license` field contains one of LGPL, GPL, AGPL, MPL, EPL, CDDL,
# EUPL, OSL, CC-BY-SA (case-insensitive substring match on the SPDX
# expression), or whose license is missing entirely (an unknown license is
# treated as "assume the worst" rather than silently skipped).
#
# Usage:
#   scripts/check-third-party-notices-web-drift.sh
#
# Exit 0 = the two lists match (same name@version<TAB>license pairs).
# Exit 1 = drift; a unified diff is printed showing what moved. On drift:
#   1. re-run the `node -e` inventory query below by hand to see the new set
#   2. update THIRD-PARTY-NOTICES-WEB.md: the "Artifacts shipped..." tables,
#      the machine-readable inventory block, and the "Generated/verified"
#      date — and re-check the prose still describes how sharp combines with
#      norintegrate-web (this is a human judgement call, not automatable)
#
# Requirements: node (already a web CI dependency).

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LOCKFILE="$REPO_ROOT/norintegrate-web/package-lock.json"
NOTICES="$REPO_ROOT/THIRD-PARTY-NOTICES-WEB.md"

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

# Derive the actual copyleft set from the lockfile. Non-dev packages only
# (dev-only deps like lightningcss*/axe-core, both MPL-2.0, are never
# installed in the runtime image).
node -e '
const fs = require("fs");
const lock = JSON.parse(fs.readFileSync(process.argv[1], "utf8"));
const copyleftRe = /(LGPL|AGPL|GPL|MPL|EPL|CDDL|EUPL|OSL|CC-BY-SA)/i;
const rows = [];
for (const [key, meta] of Object.entries(lock.packages || {})) {
  if (!key) continue; // root package entry
  if (meta.dev) continue;
  const license = meta.license || "";
  if (!license || copyleftRe.test(license)) {
    const name = key.replace(/^node_modules\//, "");
    rows.push(`${name}@${meta.version}\t${license || "(missing)"}`);
  }
}
rows.sort();
process.stdout.write(rows.join("\n") + (rows.length ? "\n" : ""));
' "$LOCKFILE" > "$WORK/actual.txt"

# Extract the recorded inventory from between the markers.
awk '/<!-- copyleft-inventory:start -->/{f=1;next} /<!-- copyleft-inventory:end -->/{f=0} f&&NF' \
  "$NOTICES" | sort > "$WORK/recorded.txt"

if [ ! -s "$WORK/actual.txt" ]; then
  echo "ERROR: found no copyleft packages in $LOCKFILE — the check itself is broken." >&2
  exit 1
fi

if [ ! -s "$WORK/recorded.txt" ]; then
  echo "ERROR: no copyleft-inventory block found (or it is empty) in $NOTICES." >&2
  exit 1
fi

if diff -u "$WORK/recorded.txt" "$WORK/actual.txt" > "$WORK/diff.txt"; then
  echo "OK: $(wc -l < "$WORK/actual.txt" | tr -d ' ') copyleft packages match"
  exit 0
fi

echo "" >&2
echo "DRIFT: THIRD-PARTY-NOTICES-WEB.md no longer describes the copyleft packages" >&2
echo "in norintegrate-web/package-lock.json." >&2
echo "  '-' = recorded in the notices file but no longer present/copyleft" >&2
echo "  '+' = actually copyleft (or unlicensed) but missing from the notices file" >&2
echo "" >&2
cat "$WORK/diff.txt" >&2
echo "" >&2
echo "Update THIRD-PARTY-NOTICES-WEB.md — see the header of this script." >&2
exit 1
