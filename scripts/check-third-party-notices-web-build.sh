#!/usr/bin/env bash
# Verify that THIRD-PARTY-NOTICES-WEB.md's inventory blocks cover every
# package that actually reaches norintegrate-web/.next/standalone/node_modules
# (NOR-31 M2). Thin wrapper around
# scripts/lib/check-third-party-notices-web-build.js — see that file's
# header for the full explanation and the skip rule.
#
# Unlike scripts/check-third-party-notices-web-drift.sh (which compares the
# recorded inventories against package-lock.json, a static source), this
# script compares them against a *real build's* output-file-tracing result —
# catching the class of gap that only shows up empirically (this is how
# `typescript` was originally found: a devDependency the lockfile alone
# would never have flagged, but that reaches the built image anyway).
#
# Usage:
#   cd norintegrate-web && npm run build   # produces .next/standalone
#   scripts/check-third-party-notices-web-build.sh
#
# Exit 0 = every package under .next/standalone/node_modules is either
#          recorded in one of the two inventory blocks, or provably cannot
#          ship inside the linux+musl Docker image per the lockfile.
# Exit 1 = something reaches the build output undocumented.
#
# Requirements: node; norintegrate-web/.next/standalone must already exist.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
node "$SCRIPT_DIR/lib/check-third-party-notices-web-build.js"
