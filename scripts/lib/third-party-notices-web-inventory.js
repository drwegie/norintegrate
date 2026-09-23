#!/usr/bin/env node
// Shared inventory logic for THIRD-PARTY-NOTICES-WEB.md's two machine-readable
// blocks (copyleft-inventory, permissive-inventory). Both
// scripts/check-third-party-notices-web-drift.sh and
// scripts/generate-third-party-notices-web.sh call this so the superset /
// exclusion rules live in exactly one place (NOR-31).
//
// Usage:
//   node scripts/lib/third-party-notices-web-inventory.js copyleft
//   node scripts/lib/third-party-notices-web-inventory.js permissive
//
// Prints "name@version<TAB>license", one per line, sorted.
//
// Scope (NOR-31 legal entry, 2026-09-23): the SUPERSET of
// norintegrate-web/package-lock.json's non-dev packages, minus:
//   - the build-only EXCLUDE list below (test runners, compilers, and their
//     platform variants — never in the runtime image; legal reviewed this
//     list by exact name)
//   - optional-platform binaries whose declared os/cpu doesn't match the
//     platform this script runs on (their LICENSE files aren't present in
//     the local node_modules to read a copyright line from; the platform
//     actually shipped in the Docker image is a strict subset handled the
//     same way)
// Over-inclusion is intentional and harmless (extra attribution is not a
// license violation); under-inclusion is not, so this errs wide.
//
// One documented exception: `typescript` is `"dev": true` in the lockfile
// (it's a devDependency in package.json) but was empirically found inside
// the built runtime image's /app/node_modules (docker run against
// docker/web.Dockerfile, 2026-09-23) — a Next.js standalone-tracing quirk.
// It is force-included here rather than silently missed by the non-dev
// filter.

"use strict";

const fs = require("fs");
const path = require("path");

const REPO_ROOT = path.resolve(__dirname, "..", "..");
const LOCKFILE = path.join(REPO_ROOT, "norintegrate-web", "package-lock.json");

const COPYLEFT_RE = /(LGPL|AGPL|GPL|MPL|EPL|CDDL|EUPL|OSL|CC-BY-SA)/i;

// Exact package names excluded as build-only tooling (never present in the
// runtime image). Reviewed by legal (NOR-31 REGISTER.md entry, 2026-09-23).
const EXCLUDE_EXACT = new Set([
  "@playwright/test",
  "playwright",
  "playwright-core",
  "next-intl-swc-plugin-extractor",
  "@eloqnt/config",
  "@eloqnt/format-json",
  "@eloqnt/format-po",
  "po-parser",
  "node-addon-api",
  "is-extglob",
  "is-glob",
  "picomatch",
]);

// Name prefixes excluded as build-only tooling, covering every
// platform/target variant of the same package.
const EXCLUDE_PREFIX = ["@swc/core", "@next/swc-", "@parcel/watcher"];

// Packages force-included despite failing the plain "non-dev" filter. See
// header comment for why. Map of name -> reason (kept for documentation;
// the version/license still come from the lockfile).
const FORCE_INCLUDE = new Map([
  [
    "typescript",
    "dev:true in package-lock.json (declared devDependency) but present " +
      "in the built runtime image's /app/node_modules — verified via " +
      "`docker run` against docker/web.Dockerfile on 2026-09-23.",
  ],
]);

function isExcluded(name) {
  if (EXCLUDE_EXACT.has(name)) return true;
  return EXCLUDE_PREFIX.some((p) => name === p || name.startsWith(p));
}

function bareName(key) {
  const parts = key.split("node_modules/");
  return parts[parts.length - 1];
}

// Whether to drop packages restricted (via lockfile os/cpu) to a platform
// other than the one this script runs on. The existing copyleft-inventory
// block predates NOR-31 and intentionally lists every platform variant
// (e.g. every @img/sharp-libvips-<platform>) regardless of build host, so
// the copyleft bucket must NOT be platform-filtered — only the new
// permissive bucket is (see module header: "drop other-platform optional
// binaries" is a NOR-31 permissive-section rule, not a copyleft one).
function loadRows(filterPlatform) {
  const lock = JSON.parse(fs.readFileSync(LOCKFILE, "utf8"));
  const rows = new Map(); // "name@version" -> { name, version, license }

  for (const [key, meta] of Object.entries(lock.packages || {})) {
    if (!key) continue; // root package entry
    const name = bareName(key);
    const forced = FORCE_INCLUDE.has(name);
    if (meta.dev && !forced) continue;
    if (isExcluded(name)) continue;
    if (filterPlatform && !forced) {
      if (meta.os && !meta.os.includes(process.platform)) continue;
      if (meta.cpu && !meta.cpu.includes(process.arch)) continue;
    }
    const id = `${name}@${meta.version}`;
    rows.set(id, { name, version: meta.version, license: meta.license || "" });
  }
  return [...rows.values()];
}

function main() {
  const kind = process.argv[2];
  if (kind !== "copyleft" && kind !== "permissive") {
    console.error("Usage: third-party-notices-web-inventory.js <copyleft|permissive>");
    process.exit(2);
  }

  const rows = loadRows(kind === "permissive").filter((r) =>
    kind === "copyleft"
      ? !r.license || COPYLEFT_RE.test(r.license)
      : r.license && !COPYLEFT_RE.test(r.license)
  );

  const lines = rows
    .map((r) => `${r.name}@${r.version}\t${r.license || "(missing)"}`)
    .sort();
  process.stdout.write(lines.join("\n") + (lines.length ? "\n" : ""));
}

main();
