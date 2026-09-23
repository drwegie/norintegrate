#!/usr/bin/env node
// Shared inventory logic for THIRD-PARTY-NOTICES-WEB.md's two machine-readable
// blocks (copyleft-inventory, permissive-inventory). All three of
// scripts/check-third-party-notices-web-drift.sh,
// scripts/check-third-party-notices-web-build.sh, and
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
//   - optional-platform binaries that cannot ship inside the Docker image
//     (docker/web.Dockerfile's runner stage must be FROM node:*-alpine —
//     linux + musl libc; asserted against the Dockerfile on every load, see
//     assertRunnerStageMatchesImageAssumption below).
//     This rule is host-independent (fixed: linux+musl), not tied to
//     process.platform/process.arch, so the recorded inventory is the same
//     regardless of what machine or CI runner generates/checks it (NOR-31
//     follow-up, 2026-09-23: the original process.platform/arch filter
//     produced a different, host-dependent list on every OS/arch — verified
//     wrong both on macOS/darwin-arm64 and inside `docker run --platform
//     linux/amd64 node:24-alpine`, which reports linux/x64 but is musl, not
//     glibc). `cpu` is ignored entirely: both arm64 and x64 musl variants
//     are kept, superset-style, matching how the copyleft block already
//     lists every platform variant regardless of host.
// Over-inclusion is intentional and harmless (extra attribution is not a
// license violation); under-inclusion is not, so this errs wide.
//
// Two documented exceptions (FORCE_INCLUDE below): `typescript` and
// `tailwindcss` are both `"dev": true` in the lockfile (devDependencies in
// package.json) but were empirically found to reach the built output
// anyway — `typescript` inside the runtime image's /app/node_modules
// (Next.js standalone-tracing quirk), `tailwindcss` compiled into
// .next/static's CSS (imported by app/globals.css). Both are
// force-included here rather than silently missed by the non-dev filter.
// scripts/check-third-party-notices-web-build.sh checks a real build's
// output-file-tracing result against these two inventories so a third such
// gap doesn't have to be found by hand again.

"use strict";

const fs = require("fs");
const path = require("path");

const REPO_ROOT = path.resolve(__dirname, "..", "..");
const LOCKFILE = path.join(REPO_ROOT, "norintegrate-web", "package-lock.json");
const WEB_DOCKERFILE = path.join(REPO_ROOT, "docker", "web.Dockerfile");

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
  [
    "tailwindcss",
    "dev:true in package-lock.json (declared devDependency), but its " +
      "output is compiled into .next/static CSS served to every browser: " +
      "the built stylesheet starts with `/*! tailwindcss v4.3.3 | MIT " +
      "License | https://tailwindcss.com */`, and it's imported by " +
      "norintegrate-web/app/globals.css (`@import \"tailwindcss\";`).",
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

// The runtime image's platform: docker/web.Dockerfile is FROM node:*-alpine
// (linux, musl libc). Fixed, not derived from process.platform/process.arch,
// so this rule gives the same answer on every machine/CI runner.
//
// IMAGE_OS/IMAGE_LIBC below are only correct as long as that's actually
// true. Rather than silently going stale if someone switches the runner
// stage's base image (glibc, a different distro, a non-Node base with a
// custom entrypoint, ...), assert it against docker/web.Dockerfile on every
// load and fail loudly if it no longer holds — this is the same
// "verify, don't assume" posture as the Apache-2.0 NOTICE checks elsewhere
// in this file.
function assertRunnerStageMatchesImageAssumption() {
  let content;
  try {
    content = fs.readFileSync(WEB_DOCKERFILE, "utf8");
  } catch (err) {
    throw new Error(
      `Could not read ${WEB_DOCKERFILE} to verify the IMAGE_OS/IMAGE_LIBC ` +
        `assumption below: ${err.message}`
    );
  }
  const match = content.match(/^FROM\s+(\S+)\s+AS\s+runner\s*$/im);
  if (!match) {
    throw new Error(
      `Could not find a "FROM <image> AS runner" stage in ${WEB_DOCKERFILE}. ` +
        "This script's permissive-inventory filter assumes the runner stage " +
        "is a node:*-alpine (linux + musl) image — update the Dockerfile to " +
        "have an explicitly named runner stage, or update this assumption " +
        "(and re-review the whole permissive inventory) if the runtime base " +
        "image has changed."
    );
  }
  const runnerImage = match[1];
  if (!/^node:.*-alpine$/i.test(runnerImage)) {
    throw new Error(
      `${WEB_DOCKERFILE}'s runner stage is "FROM ${runnerImage} AS runner", ` +
        "not a node:*-alpine image. scripts/lib/third-party-notices-web-" +
        "inventory.js hard-codes IMAGE_OS=\"linux\"/IMAGE_LIBC=\"musl\" on " +
        "the assumption that the runtime image is Alpine — if the base " +
        "image changed, update IMAGE_OS/IMAGE_LIBC here (or the filtering " +
        "logic, if the new base isn't musl-based at all) and re-review the " +
        "whole permissive inventory before trusting it again."
    );
  }
}

assertRunnerStageMatchesImageAssumption();

const IMAGE_OS = "linux";
const IMAGE_LIBC = "musl";

function canShipInImage(meta) {
  if (meta.os && !meta.os.includes(IMAGE_OS)) return false;
  if (meta.libc && !meta.libc.includes(IMAGE_LIBC)) return false;
  return true; // cpu is ignored on purpose — see module header.
}

// Whether to drop packages that cannot ship inside the Docker image (see
// canShipInImage) and the build-only EXCLUDE list (see isExcluded). Both are
// NOR-31 *permissive*-section rules. The existing copyleft-inventory block
// predates NOR-31, lists every platform variant (e.g. every
// @img/sharp-libvips-<platform>) regardless of build host, and was never
// subject to the build-only exclusion list either (main's behavior) — so
// neither filter applies when filterToImage is false. Only the new
// permissive bucket is filtered by both.
function loadRows(filterToImage) {
  const lock = JSON.parse(fs.readFileSync(LOCKFILE, "utf8"));
  const rows = new Map(); // "name@version" -> { name, version, license }

  for (const [key, meta] of Object.entries(lock.packages || {})) {
    if (!key) continue; // root package entry
    const name = bareName(key);
    const forced = FORCE_INCLUDE.has(name);
    if (meta.dev && !forced) continue;
    if (filterToImage && isExcluded(name)) continue;
    if (filterToImage && !forced && !canShipInImage(meta)) continue;
    const id = `${name}@${meta.version}`;
    rows.set(id, { name, version: meta.version, license: meta.license || "" });
  }
  return [...rows.values()];
}

// kind: "copyleft" | "permissive". Returns [{ name, version, license }, ...].
function getInventory(kind) {
  if (kind !== "copyleft" && kind !== "permissive") {
    throw new Error(`getInventory: kind must be "copyleft" or "permissive", got ${JSON.stringify(kind)}`);
  }
  return loadRows(kind === "permissive").filter((r) =>
    kind === "copyleft"
      ? !r.license || COPYLEFT_RE.test(r.license)
      : r.license && !COPYLEFT_RE.test(r.license)
  );
}

function main() {
  const kind = process.argv[2];
  if (kind !== "copyleft" && kind !== "permissive") {
    console.error("Usage: third-party-notices-web-inventory.js <copyleft|permissive>");
    process.exit(2);
  }

  const rows = getInventory(kind);
  const lines = rows
    .map((r) => `${r.name}@${r.version}\t${r.license || "(missing)"}`)
    .sort();
  process.stdout.write(lines.join("\n") + (lines.length ? "\n" : ""));
}

if (require.main === module) {
  main();
}

module.exports = {
  REPO_ROOT,
  LOCKFILE,
  COPYLEFT_RE,
  IMAGE_OS,
  IMAGE_LIBC,
  FORCE_INCLUDE,
  isExcluded,
  bareName,
  canShipInImage,
  getInventory,
};
