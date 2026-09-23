#!/usr/bin/env node
// Regenerates the "Permissive-licensed npm dependencies" section of
// THIRD-PARTY-NOTICES-WEB.md (NOR-31) — the entries table, the Apache-2.0
// NOTICE reproduction, the CC-BY-4.0 attribution, the license-text
// references, and the machine-readable `permissive-inventory` block.
//
// It does NOT touch the hand-written copyleft (LGPL/MPL) prose above it, or
// the copyleft-inventory block, which stay exactly as a human wrote them —
// see scripts/check-third-party-notices-web-drift.sh's header for why that
// part is intentionally not generated.
//
// Usage:
//   node scripts/lib/generate-third-party-notices-web.js
// (invoked by scripts/generate-third-party-notices-web.sh)
//
// Copyright-line resolution per entry, in order:
//   1. A `Copyright ...` line grep'd from the package's own LICENSE file in
//      the local norintegrate-web/node_modules (works for MIT/ISC/BSD/0BSD,
//      which embed a per-package copyright line in the license text itself).
//   2. COPYRIGHT_OVERRIDES below — reviewed, for packages whose LICENSE file
//      is absent locally or doesn't carry a copyright line (Apache-2.0's
//      canonical text never does; a few packages ship no LICENSE file at
//      all). Sourced from the package's own `author`/`maintainers` field
//      (npm registry lookup where the package isn't present in local
//      node_modules for this platform).
//   3. Literally "UNVERIFIED" — no source found. Never silently filled in.
//
// This mirrors render-third-party-notices.py's ELECTIONS pattern: license
// judgement calls are explicit, reviewed entries in this file, not inferred.

"use strict";

const fs = require("fs");
const path = require("path");
const { execFileSync } = require("child_process");

const REPO_ROOT = path.resolve(__dirname, "..", "..");
const WEB_DIR = path.join(REPO_ROOT, "norintegrate-web");
const NODE_MODULES = path.join(WEB_DIR, "node_modules");
const NOTICES = path.join(REPO_ROOT, "THIRD-PARTY-NOTICES-WEB.md");
const INVENTORY_SCRIPT = path.join(__dirname, "third-party-notices-web-inventory.js");

// Reviewed fallback copyright lines for packages where the local LICENSE
// file (if any) carries no extractable copyright line. Keyed by bare
// package name (applies to every version of that package recorded here —
// re-review if a listed package's upstream author changes).
const COPYRIGHT_OVERRIDES = {
  // Apache-2.0's canonical license text has no copyright-holder line (only
  // a "Licensor" definition); these all resolve via their own
  // package.json `author` field (sharp, detect-libc) or, for the
  // platform-specific @img/sharp-* binaries not installed in this
  // machine's node_modules (the permissive inventory is filtered to what
  // can ship in the linux+musl Docker image, not this host's platform —
  // see scripts/lib/third-party-notices-web-inventory.js), the same
  // `author` read from registry.npmjs.org (2026-09-23; all @img/sharp-*
  // platform packages are published by the sharp project under the same
  // author as `sharp` itself).
  sharp: "Lovell Fuller <npm@lovell.info> (npm package.json `author`)",
  "detect-libc": "Lovell Fuller <npm@lovell.info> (npm package.json `author`)",
  "@img/sharp-linuxmusl-arm64": "Lovell Fuller <npm@lovell.info> (npm registry `author`, package not installed under this platform's node_modules)",
  "@img/sharp-linuxmusl-x64": "Lovell Fuller <npm@lovell.info> (npm registry `author`, package not installed under this platform's node_modules)",
  "@img/sharp-webcontainers-wasm32": "Lovell Fuller <npm@lovell.info> (npm registry `author`, package not installed under this platform's node_modules)",
  typescript: "Microsoft Corp. (npm package.json `author`)",
  "@swc/types": "강동윤 (Kang Dong-yoon) <kdy1997.dev@gmail.com> (npm package.json `author`)",
  "@swc/helpers": "강동윤 (Kang Dong-yoon) <kdy1997.dev@gmail.com> (npm package.json `author`)",
  // No LICENSE file in the published package; author taken from
  // registry.npmjs.org's package metadata (2026-09-23).
  "@swc/counter": "강동윤 (Kang Dong-yoon) <kdy1997.dev@gmail.com> (npm registry `author`, no local LICENSE file)",
  "@schummar/icu-type-parser": "Marco Schumacher <marco@schumacher.dev> (npm registry `author`, no local LICENSE file)",
  "@next/env": "Next.js Team <support@vercel.com> (npm registry `author`, no local LICENSE file)",
  "@emnapi/runtime": "toyobayashi (npm registry `author`, no local LICENSE file — package not installed under this platform's node_modules)",
  // CC-BY-4.0: attribution, not a copyright-notice license, but recorded
  // the same way for table consistency.
  "caniuse-lite": "Ben Briggs <beneb.info@gmail.com> (npm package.json `author`)",
};

// name -> SPDX family used for grouping the "License texts" subsection.
// Anything not listed here defaults to its own license field.
const FAMILY_TEXT_FILE = {
  MIT: "MIT.txt",
  ISC: "ISC.txt",
  "BSD-3-Clause": "BSD-3-Clause.txt",
  "Apache-2.0": "Apache-2.0.txt",
  "0BSD": "0BSD.txt",
  "CC-BY-4.0": "CC-BY-4.0.txt",
};

function runInventory(kind) {
  const out = execFileSync("node", [INVENTORY_SCRIPT, kind], { encoding: "utf8" });
  return out
    .split("\n")
    .filter(Boolean)
    .map((line) => {
      const [id, license] = line.split("\t");
      const at = id.lastIndexOf("@");
      return { name: id.slice(0, at), version: id.slice(at + 1), license };
    });
}

function findLicenseFile(name) {
  const dir = path.join(NODE_MODULES, name);
  const candidates = ["LICENSE", "LICENSE.md", "LICENSE.txt", "LICENSE-MIT", "License", "license", "LICENCE"];
  for (const f of candidates) {
    const p = path.join(dir, f);
    if (fs.existsSync(p)) return p;
  }
  return null;
}

function copyrightFromLicenseFile(name) {
  const file = findLicenseFile(name);
  if (!file) return null;
  const text = fs.readFileSync(file, "utf8");
  const match = text.match(/^.*copyright.*$/im);
  if (!match) return null;
  // Boilerplate license text (e.g. Apache-2.0's "Licensor" definition)
  // mentions "copyright" without a holder — reject lines that aren't an
  // actual "Copyright ..." notice.
  if (!/copyright\s*(\(c\)|©|\d)/i.test(match[0])) return null;
  return match[0].trim();
}

function resolveCopyright(name) {
  const fromFile = copyrightFromLicenseFile(name);
  if (fromFile) return { text: fromFile, verified: true };
  if (Object.prototype.hasOwnProperty.call(COPYRIGHT_OVERRIDES, name)) {
    return { text: COPYRIGHT_OVERRIDES[name], verified: true };
  }
  return { text: "UNVERIFIED", verified: false };
}

function readTypescriptNotice() {
  const file = path.join(NODE_MODULES, "typescript", "ThirdPartyNoticeText.txt");
  if (!fs.existsSync(file)) return null;
  return fs.readFileSync(file, "utf8").trim();
}

// Reviewed evidence for entries whose copyright holder cannot be verified
// (legal, 2026-09-23). Printed under the table so the gap is explained, not hidden.
const UNVERIFIED_NOTES = {
  "client-only":
    "UNVERIFIED — no `author` field in package.json, no bundled LICENSE file; registry `maintainers` reflects only the publishing npm account (sebmarkbage), not an asserted copyright holder; upstream request to add a LICENSE file was closed \"not planned\" (facebook/react#27242, verified 2026-09-23)",
};

function escapeMd(s) {
  return s.replace(/\|/g, "\\|");
}

function render(rows) {
  const out = [];
  const w = (s = "") => out.push(s);

  w("## Permissive-licensed npm dependencies (MIT, ISC, BSD, Apache-2.0, 0BSD) and CC-BY-4.0 attribution");
  w("");
  w(
    "This section covers `norintegrate-web`'s permissively-licensed and " +
      "CC-BY-4.0 runtime npm dependencies (everything in scope of this file " +
      "that is not the copyleft `@img/sharp-libvips-*` family covered above)."
  );
  w("");
  w("### Inventory method");
  w("");
  w(
    "The table below is the **SUPERSET** of `norintegrate-web/package-lock.json`'s " +
      "non-dev packages: every package the npm dependency tree resolves for " +
      "`next`, `react`, `react-dom`, `next-auth`, and `next-intl` and their " +
      "transitive dependencies, minus the exclusion list below. Over-inclusion " +
      "is intentional and harmless — attributing a package that turns out not " +
      "to reach the built image is not a license violation, but missing one " +
      "that does would be. The actual built image (`docker build -f " +
      "docker/web.Dockerfile`, standalone output, Alpine/musl) ships a smaller " +
      "set — see the copyleft section above for how that asymmetry is handled " +
      "for `@img/sharp-libvips-*`."
  );
  w("");
  w(
    "**Excluded (build-only tooling, never in the runtime image, reviewed by " +
      "name):** `@playwright/test`, `playwright`, `playwright-core` (E2E test " +
      "runner); `@swc/core` and every platform variant (build-time compiler — " +
      "absent from the actual image, confirmed by inspecting `/app/node_modules`); " +
      "`@parcel/watcher` and every platform variant (`next dev` file watcher, " +
      "unused by production builds); `next-intl-swc-plugin-extractor` (build-time " +
      "SWC plugin); `@eloqnt/config`, `@eloqnt/format-json`, `@eloqnt/format-po`, " +
      "`po-parser` (i18n message-extraction build tooling); `node-addon-api` " +
      "(header-only, build-time only); `is-extglob`, `is-glob`, `picomatch` " +
      "(glob matchers used only by the build tooling above, not on any runtime " +
      "code path); `@next/swc-*` every platform variant (build-time compiler, " +
      "confirmed absent from the actual image)."
  );
  w("");
  w(
    "**Excluded (other-platform optional binaries):** packages restricted via " +
      "`package-lock.json`'s `os`/`libc` fields to something other than what " +
      "`docker/web.Dockerfile`'s runtime image can run (`node:24-alpine` — " +
      "`linux` + musl libc) are dropped. This rule is fixed to the image's " +
      "platform, not the platform this file happens to be generated on — an " +
      "earlier version of this rule used `process.platform`/`process.arch`, " +
      "which produced a different (and wrong) list depending on which machine " +
      "or CI runner generated it; see git history for the NOR-31 follow-up " +
      "fix. `cpu` is ignored, so both `arm64` and `x64` musl variants are kept " +
      "(superset, same as the copyleft block). `@img/sharp-<platform>` and " +
      "`@img/sharp-libvips-<platform>` variants for non-`linux`/non-musl " +
      "platforms (`darwin`, `win32`, glibc `linux`) are the main packages this " +
      "affects; the copyleft ones remain listed in full above regardless of " +
      "platform, unaffected by this rule. Where a dropped package's " +
      "`LICENSE` file also isn't present in this machine's `node_modules` " +
      "(because it wasn't installed on this platform), its copyright line is " +
      "a reviewed `COPYRIGHT_OVERRIDES` entry sourced from the npm registry " +
      "instead — see the Entries table below."
  );
  w("");
  w(
    "**One documented exception:** `typescript` is declared as a " +
      "`devDependency` in `norintegrate-web/package.json` and recorded as " +
      "`\"dev\": true` in `package-lock.json`, so the SUPERSET rule above would " +
      "normally exclude it. It is force-included here because it was found " +
      "inside the actual built runtime image's `/app/node_modules` (`docker " +
      "run --entrypoint sh ... -c \"find /app/node_modules -maxdepth 1\"`, " +
      "2026-09-23) — a Next.js standalone-output tracing quirk that the " +
      "lockfile's `dev` flag does not reflect."
  );
  w("");
  w("### Entries");
  w("");
  w("| Package | Version | SPDX license | Copyright |");
  w("|---------|---------|--------------|-----------|");
  const sorted = [...rows].sort((a, b) => (a.name < b.name ? -1 : a.name > b.name ? 1 : a.version < b.version ? -1 : 1));
  let unverifiedCount = 0;
  for (const r of sorted) {
    const { text, verified } = resolveCopyright(r.name);
    if (!verified) unverifiedCount++;
    w(`| \`${escapeMd(r.name)}\` | ${r.version} | ${r.license} | ${escapeMd(text)} |`);
  }
  w("");
  w(`${unverifiedCount} of ${sorted.length} entries are \`UNVERIFIED\` (no LICENSE file present locally and no npm author/maintainer metadata found).`);
  w("");
  for (const r of sorted) {
    const note = UNVERIFIED_NOTES[r.name];
    if (note && !resolveCopyright(r.name).verified) w(`- \`${r.name}@${r.version}\`: ${note}`);
  }
  w("");

  w("### Apache-2.0 NOTICE reproduction");
  w("");
  w(
    "Of the Apache-2.0 packages above, only `typescript` bundles a `NOTICE`-" +
      "equivalent file with its published npm package " +
      "(`node_modules/typescript/ThirdPartyNoticeText.txt`, the npm-distributed " +
      "form of the `NOTICE.txt` published at " +
      "https://github.com/microsoft/TypeScript). Per Apache-2.0 §4(d), its " +
      "content is reproduced verbatim below. The other Apache-2.0 packages " +
      "(" +
      sorted
        .filter((r) => r.license === "Apache-2.0" && r.name !== "typescript")
        .map((r) => "`" + r.name + "`")
        .join(", ") +
      ") do not bundle a `NOTICE` file, so §4(d) " +
      "imposes no additional-notice obligation for them."
  );
  w("");
  const notice = readTypescriptNotice();
  if (notice) {
    w("<details>");
    w("<summary><code>typescript@" + (rows.find((r) => r.name === "typescript") || {}).version + " — ThirdPartyNoticeText.txt</code></summary>");
    w("");
    w("```text");
    w(notice);
    w("```");
    w("");
    w("</details>");
  } else {
    w("`ThirdPartyNoticeText.txt` was not found in the local `node_modules/typescript` — re-run this generator with `typescript` installed.");
  }
  w("");

  w("### CC-BY-4.0 attribution (`caniuse-lite`)");
  w("");
  w(
    "`caniuse-lite` packages Can I Use browser-support data under CC-BY-4.0. " +
      "The upstream project's own attribution guidance " +
      "(https://github.com/Fyrd/caniuse, README, checked 2026-09-23) is: " +
      '"For attribution just mention somewhere that the source is caniuse.com." ' +
      "Per that guidance and CC-BY-4.0 §3(a)(1), the required attribution is:"
  );
  w("");
  w(
    "> This product includes browser compatibility data from `caniuse-lite` " +
      `${(rows.find((r) => r.name === "caniuse-lite") || {}).version}, ` +
      "sourced from caniuse.com (https://caniuse.com), by Ben Briggs and " +
      "contributors, licensed under CC-BY-4.0 " +
      "(https://creativecommons.org/licenses/by/4.0/)."
  );
  w("");

  w("### License texts (permissive licenses)");
  w("");
  const licenseTextsDir = path.join(REPO_ROOT, "scripts", "license-texts");
  const families = [...new Set(sorted.map((r) => r.license))].sort();
  for (const family of families) {
    w(`#### ${family}`);
    w("");
    if (family === "MIT" || family === "ISC" || family === "BSD-3-Clause" || family === "0BSD") {
      w(
        `${family} embeds a per-package copyright line in its own license ` +
          "text; each package's own line is recorded in the Entries table " +
          "above. The generic template text (with the copyright line each " +
          "package fills in) is:"
      );
      w("");
    } else if (family === "Apache-2.0") {
      w(
        "Reused verbatim from `scripts/license-texts/Apache-2.0.txt` (the " +
          "same text the JVM side's `THIRD-PARTY-NOTICES.md` uses)."
      );
      w("");
    } else if (family === "CC-BY-4.0") {
      w(
        "Full legal code of the Creative Commons Attribution 4.0 " +
          "International Public License, reproduced from " +
          "https://creativecommons.org/licenses/by/4.0/legalcode.txt " +
          "(fetched 2026-09-23). The practical attribution requirement is " +
          "the blockquote above."
      );
      w("");
    }
    const fname = FAMILY_TEXT_FILE[family];
    if (fname) {
      const p = path.join(licenseTextsDir, fname);
      if (fs.existsSync(p)) {
        w("```text");
        w(fs.readFileSync(p, "utf8").trim());
        w("```");
      } else {
        w(`(license text file scripts/license-texts/${fname} not found)`);
      }
    } else {
      w(`Full text: see the package's own \`LICENSE\` file (license expression: \`${family}\`).`);
    }
    w("");
  }

  w("### Machine-readable permissive inventory");
  w("");
  w(
    "The list below is what `scripts/check-third-party-notices-web-drift.sh` " +
      "compares against `norintegrate-web/package-lock.json` (permissive side). " +
      "Every non-dev, non-excluded package (SUPERSET, see above), " +
      "`name@version<TAB>license`, one per line."
  );
  w("");
  w(`- Generated/verified: ${new Date().toISOString().slice(0, 10)}`);
  w("- Platform used for the exclusion pass: `linux`/musl (fixed — matches `docker/web.Dockerfile`'s `node:24-alpine`, independent of the host this was generated on)");
  w(`- Host this file was generated on (copyright-line lookups only): \`${process.platform}\`/\`${process.arch}\``);
  w("");
  w("<!-- permissive-inventory:start -->");
  for (const r of runInventory("permissive")) {
    w(`${r.name}@${r.version}\t${r.license}`);
  }
  w("<!-- permissive-inventory:end -->");
  w("");

  return out.join("\n");
}

function spliceIntoFile(sectionText) {
  const original = fs.readFileSync(NOTICES, "utf8");
  const startMarker = "<!-- permissive-section:start -->";
  const endMarker = "<!-- permissive-section:end -->";
  const wrapped = `${startMarker}\n\n${sectionText.trim()}\n\n${endMarker}`;

  if (original.includes(startMarker) && original.includes(endMarker)) {
    const startIdx = original.indexOf(startMarker);
    const endIdx = original.indexOf(endMarker) + endMarker.length;
    return original.slice(0, startIdx) + wrapped + original.slice(endIdx);
  }

  // First run: insert right before the copyleft "## License texts" section
  // so the permissive section reads after the LGPL/MPL combination
  // narrative and before the LGPL/GPL full license texts.
  const anchor = "\n## License texts\n";
  const anchorIdx = original.indexOf(anchor);
  if (anchorIdx === -1) {
    throw new Error(`Could not find anchor ${JSON.stringify(anchor)} in ${NOTICES}`);
  }
  return original.slice(0, anchorIdx) + "\n" + wrapped + "\n" + original.slice(anchorIdx);
}

function main() {
  const rows = runInventory("permissive");
  const sectionText = render(rows);
  const updated = spliceIntoFile(sectionText);
  fs.writeFileSync(NOTICES, updated);
  console.log(`wrote ${NOTICES} (${rows.length} permissive/CC-BY-4.0 entries)`);
}

main();
