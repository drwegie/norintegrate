#!/usr/bin/env node
// Verifies that THIRD-PARTY-NOTICES-WEB.md's two inventory blocks actually
// cover what a real `next build` puts under
// norintegrate-web/.next/standalone/node_modules (NOR-31 M2).
//
// Why this exists: the copyleft/permissive inventories are derived from
// package-lock.json (a static, deterministic source), but what Next.js's
// standalone output-file-tracing actually includes is a runtime decision
// (import graph, not just "is it a non-dev dependency"). NOR-31's earlier
// `typescript` finding — force-included only after being found empirically
// inside a built image — is exactly the kind of gap this check exists to
// catch automatically instead of relying on someone noticing by hand again.
//
// What it does:
//   1. Walks norintegrate-web/.next/standalone/node_modules recursively
//      (including nested node_modules under scoped and unscoped packages),
//      collecting every {name, version} pair from each package's own
//      package.json.
//   2. Reads both machine-readable blocks (copyleft-inventory,
//      permissive-inventory) out of THIRD-PARTY-NOTICES-WEB.md.
//   3. For every {name, version} found on disk but not recorded in either
//      block: looks it up in package-lock.json. If the lockfile says it
//      can't ship inside the linux+musl Docker image (see
//      scripts/lib/third-party-notices-web-inventory.js's canShipInImage),
//      it's an expected artifact of *this* build's host platform (e.g.
//      @img/sharp-darwin-arm64 on a macOS build) — skipped. Otherwise it's
//      an undocumented package actually reaching the built output — fail.
//
// Usage:
//   node scripts/lib/check-third-party-notices-web-build.js
// (invoked by scripts/check-third-party-notices-web-build.sh)
//
// Requires norintegrate-web/.next/standalone to exist — run
// `cd norintegrate-web && npm run build` first.

"use strict";

const fs = require("fs");
const path = require("path");

const inventory = require("./third-party-notices-web-inventory.js");

const REPO_ROOT = inventory.REPO_ROOT;
const WEB_DIR = path.join(REPO_ROOT, "norintegrate-web");
const STANDALONE_NODE_MODULES = path.join(WEB_DIR, ".next", "standalone", "node_modules");
const NOTICES = path.join(REPO_ROOT, "THIRD-PARTY-NOTICES-WEB.md");

function isDir(p) {
  try {
    return fs.statSync(p).isDirectory();
  } catch {
    return false;
  }
}

function addPackage(pkgDir, results) {
  const pkgJsonPath = path.join(pkgDir, "package.json");
  if (!fs.existsSync(pkgJsonPath)) return;
  let pkg;
  try {
    pkg = JSON.parse(fs.readFileSync(pkgJsonPath, "utf8"));
  } catch {
    return; // malformed package.json — not our problem to diagnose here
  }
  if (pkg.name && pkg.version) results.push({ name: pkg.name, version: pkg.version });
}

// Recursively walks a node_modules directory, including nested
// node_modules (npm dedupes most things to the top level, but not
// everything — see e.g. the nested @formatjs/fast-memoize and @swc/core
// versions already documented in THIRD-PARTY-NOTICES-WEB.md's prose).
function walkNodeModules(dir, results) {
  if (!fs.existsSync(dir)) return;
  let entries;
  try {
    entries = fs.readdirSync(dir);
  } catch {
    return;
  }
  for (const entry of entries) {
    if (entry === ".bin" || entry === ".package-lock.json") continue;
    const entryPath = path.join(dir, entry);
    if (!isDir(entryPath)) continue;
    if (entry.startsWith("@")) {
      let scoped;
      try {
        scoped = fs.readdirSync(entryPath);
      } catch {
        continue;
      }
      for (const s of scoped) {
        const pkgDir = path.join(entryPath, s);
        if (!isDir(pkgDir)) continue;
        addPackage(pkgDir, results);
        walkNodeModules(path.join(pkgDir, "node_modules"), results);
      }
    } else {
      addPackage(entryPath, results);
      walkNodeModules(path.join(entryPath, "node_modules"), results);
    }
  }
}

function readBlock(content, marker) {
  const start = `<!-- ${marker}:start -->`;
  const end = `<!-- ${marker}:end -->`;
  const startIdx = content.indexOf(start);
  const endIdx = content.indexOf(end);
  if (startIdx === -1 || endIdx === -1) {
    throw new Error(`Could not find ${marker} block in ${NOTICES}`);
  }
  const body = content.slice(startIdx + start.length, endIdx);
  const ids = new Set();
  for (const line of body.split("\n")) {
    const trimmed = line.trim();
    if (!trimmed) continue;
    const [id] = trimmed.split("\t");
    ids.add(id);
  }
  return ids;
}

// Builds a name@version -> lockfile meta lookup so an undocumented package
// found on disk can be checked against canShipInImage. Multiple lockfile
// entries can share the same name+version (nested resolutions); they should
// declare consistent os/cpu/libc, so first-wins is fine.
function buildLockLookup() {
  const lock = JSON.parse(fs.readFileSync(inventory.LOCKFILE, "utf8"));
  const map = new Map();
  for (const [key, meta] of Object.entries(lock.packages || {})) {
    if (!key) continue;
    const name = inventory.bareName(key);
    const id = `${name}@${meta.version}`;
    if (!map.has(id)) map.set(id, meta);
  }
  return map;
}

function main() {
  if (!fs.existsSync(STANDALONE_NODE_MODULES)) {
    console.error(
      `ERROR: ${STANDALONE_NODE_MODULES} not found. Run ` +
        "\"cd norintegrate-web && npm run build\" first."
    );
    process.exit(1);
  }

  const found = [];
  walkNodeModules(STANDALONE_NODE_MODULES, found);
  const foundIds = new Set(found.map((p) => `${p.name}@${p.version}`));

  const noticesContent = fs.readFileSync(NOTICES, "utf8");
  const recorded = new Set([
    ...readBlock(noticesContent, "copyleft-inventory"),
    ...readBlock(noticesContent, "permissive-inventory"),
  ]);

  const lockLookup = buildLockLookup();

  const gaps = [];
  const skipped = [];
  for (const id of [...foundIds].sort()) {
    if (recorded.has(id)) continue;
    const meta = lockLookup.get(id);
    if (meta && !inventory.canShipInImage(meta)) {
      skipped.push({ id, reason: "cannot ship on linux+musl per lockfile os/libc — local-build-only artifact" });
      continue;
    }
    gaps.push({ id, reason: meta ? "ships on linux+musl per lockfile but missing from both inventory blocks" : "not found in package-lock.json at all" });
  }

  console.log(`Found ${foundIds.size} distinct package(s) under ${path.relative(REPO_ROOT, STANDALONE_NODE_MODULES)}.`);
  console.log(`${recorded.size} recorded across both inventory blocks in ${path.relative(REPO_ROOT, NOTICES)}.`);
  if (skipped.length) {
    console.log(`${skipped.length} found-but-unrecorded package(s) skipped (cannot ship on linux+musl, expected local-build artifact):`);
    for (const s of skipped) console.log(`  - ${s.id}`);
  }

  if (gaps.length) {
    console.error("");
    console.error(`FAIL: ${gaps.length} package(s) reach the standalone build output but are not in either THIRD-PARTY-NOTICES-WEB.md inventory block:`);
    for (const g of gaps) console.error(`  - ${g.id}: ${g.reason}`);
    console.error("");
    console.error("Fix: add the package to the permissive inventory (rerun scripts/generate-third-party-notices-web.sh,");
    console.error("adding it to FORCE_INCLUDE in scripts/lib/third-party-notices-web-inventory.js first if it's a");
    console.error("devDependency that reaches the build output despite that, as typescript and tailwindcss already are),");
    console.error("or explain in the copyleft prose if it's copyleft.");
    process.exit(1);
  }

  console.log("OK: every package reaching the standalone build output is documented.");
  process.exit(0);
}

main();
