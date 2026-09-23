#!/usr/bin/env bash
# Regenerate the permissive/CC-BY-4.0 section of THIRD-PARTY-NOTICES-WEB.md
# (NOR-31). Thin wrapper around scripts/lib/generate-third-party-notices-web.js
# — see that file's header for the copyright-resolution rules and
# scripts/lib/third-party-notices-web-inventory.js for the superset/exclusion
# rules shared with scripts/check-third-party-notices-web-drift.sh.
#
# What this script does NOT touch: the hand-written copyleft (LGPL/MPL)
# prose and the copyleft-inventory block above the permissive section — those
# stay exactly as a human wrote/reviewed them.
#
# Usage:
#   scripts/generate-third-party-notices-web.sh
#
# Offline: reads only norintegrate-web/package-lock.json and the local
# norintegrate-web/node_modules; makes no network calls. (Any copyright line
# that needed an npm-registry lookup — for a package not installed locally on
# this platform — is a reviewed entry in
# scripts/lib/generate-third-party-notices-web.js's COPYRIGHT_OVERRIDES, not
# a live fetch.)
#
# After running, re-check with:
#   scripts/check-third-party-notices-web-drift.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
node "$SCRIPT_DIR/lib/generate-third-party-notices-web.js"
