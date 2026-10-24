#!/usr/bin/env python3
"""Render THIRD-PARTY-NOTICES.md from the data collected by
generate-third-party-notices.sh.

Split of responsibilities:
  generate-third-party-notices.sh  — fetches raw data (POM license strings,
                                     bundled META-INF/NOTICE files). No
                                     judgement calls.
  render-third-party-notices.py    — applies the reviewed normalization map
                                     and dual-license elections below, then
                                     writes THIRD-PARTY-NOTICES.md.

The normalization map and ELECTIONS are deliberately explicit rather than
heuristic: mapping a POM's free-text license name to an SPDX id, and picking
one license for a dual-licensed artifact, are legal judgements that must be
reviewed by a human, not inferred. Any raw license string not present in
LICENSE_MAP is a hard error — that forces a new dependency's license through
review instead of silently defaulting.

Usage:
  scripts/generate-third-party-notices.sh   # collect (network)
  scripts/render-third-party-notices.py     # render (offline)
"""

import hashlib
import os
import subprocess
import sys
from collections import OrderedDict, defaultdict
from datetime import date

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT_DIR = os.path.join(REPO_ROOT, "build", "third-party-notices")
REPORT = os.path.join(OUT_DIR, "report.tsv")
NOTICE_DIR = os.path.join(OUT_DIR, "notices")
TARGET = os.path.join(REPO_ROOT, "THIRD-PARTY-NOTICES.md")

# Raw POM license string -> SPDX identifier.
LICENSE_MAP = {
    "Apache License, Version 2.0": "Apache-2.0",
    "The Apache Software License, Version 2.0": "Apache-2.0",
    "Apache 2.0": "Apache-2.0",
    "The Apache License, Version 2.0": "Apache-2.0",
    "Apache-2.0": "Apache-2.0",
    "Apache License 2.0": "Apache-2.0",
    "Apache License Version 2.0": "Apache-2.0",
    "MIT License": "MIT",
    "MIT": "MIT",
    "The MIT License": "MIT",
    "MIT-0": "MIT-0",
    "BSD-3-Clause": "BSD-3-Clause",
    "BSD-2-Clause": "BSD-2-Clause",
    "Eclipse Distribution License - v 1.0": "EDL-1.0",
    "EDL 1.0": "EDL-1.0",
    "Eclipse Public License - v 2.0": "EPL-2.0",
    # Self-declared, ambiguous between the 2- and 3-clause forms. Treated as
    # BSD-3-Clause (the stricter reading: it adds the non-endorsement clause,
    # so complying with it also satisfies BSD-2-Clause). Flagged in the output.
    "The BSD License": "BSD-3-Clause",
    "BSD licence": "BSD-3-Clause",
}

# Artifacts whose POM offers a choice of licenses. We must elect one and say
# so, otherwise the obligations that actually apply are ambiguous.
# "group:artifact" -> (elected SPDX id, the POM declaration this election was
# reviewed against, rationale shown in the file).
#
# Keyed without a version (ADR-022), so a routine bump does not turn a
# reviewed election into a hard error. The middle field is what keeps that
# safe: resolve_license() fails if a bumped artifact's POM declares anything
# other than the exact offering a human reviewed, so an upstream relicensing
# cannot ride in on a version bump. The raw strings are the POMs' own
# free-text, which is why most of them are not in LICENSE_MAP — electing one
# of them IS the normalization step for these artifacts.
ELECTIONS = {
    "ch.qos.logback:logback-classic": (
        "EPL-2.0",
        "EPL-2.0;LGPL-2.1-only",
        "Dual-licensed EPL-2.0 or LGPL-2.1-only. **EPL-2.0 is elected.** "
        "The LGPL-2.1-only option is expressly *not* taken, so LGPL's "
        "relinking/source-substitution obligations do not apply here.",
    ),
    "ch.qos.logback:logback-core": (
        "EPL-2.0",
        "EPL-2.0;LGPL-2.1-only",
        "Dual-licensed EPL-2.0 or LGPL-2.1-only. **EPL-2.0 is elected.** "
        "The LGPL-2.1-only option is expressly *not* taken, so LGPL's "
        "relinking/source-substitution obligations do not apply here.",
    ),
    "jakarta.annotation:jakarta.annotation-api": (
        "EPL-2.0",
        "EPL 2.0;GPL2 w/ CPE",
        "Dual-licensed EPL-2.0 or GPL-2.0-only WITH Classpath-exception-2.0. "
        "**EPL-2.0 is elected**; the GPL option is not taken.",
    ),
    "jakarta.transaction:jakarta.transaction-api": (
        "EPL-2.0",
        "EPL 2.0;GPL2 w/ CPE",
        "Dual-licensed EPL-2.0 or GPL-2.0-only WITH Classpath-exception-2.0. "
        "**EPL-2.0 is elected**; the GPL option is not taken.",
    ),
    "jakarta.persistence:jakarta.persistence-api": (
        "EDL-1.0",
        "Eclipse Public License v. 2.0;Eclipse Distribution License v. 1.0",
        "Dual-licensed EPL-2.0 or EDL-1.0. **EDL-1.0 is elected** (the "
        "BSD-3-Clause-equivalent option).",
    ),
    "net.logstash.logback:logstash-logback-encoder": (
        "Apache-2.0",
        "Apache License, Version 2.0;MIT License",
        "Dual-licensed Apache-2.0 or MIT. **Apache-2.0 is elected.**",
    ),
    "org.hdrhistogram:HdrHistogram": (
        "BSD-2-Clause",
        "Public Domain, per Creative Commons CC0;BSD-2-Clause",
        "Offered as CC0-1.0 (public domain dedication) or BSD-2-Clause. "
        "CC0-1.0 grants no patent license (CC0 §4(a) disclaims any patent "
        "rights the affirmer might hold), so **BSD-2-Clause is elected** "
        "instead.",
    ),
}

# Artifacts whose declared license name is ambiguous in the upstream POM.
# Keyed by "group:artifact", for the same reason as ELECTIONS.
AMBIGUOUS = {
    "org.antlr:ST4": 'POM declares only "The BSD License" without '
    "naming the clause count; recorded as BSD-3-Clause (stricter reading).",
    "org.antlr:antlr-runtime": 'POM declares only "BSD licence" without '
    "naming the clause count; recorded as BSD-3-Clause (stricter reading).",
}

# Licenses whose full text must travel with the distribution.
#
# EPL-2.0 is here because section 3.2(b) requires a copy of the Agreement to
# accompany each copy of the Program distributed in object-code form, and the
# logback JARs — the EPL artifacts with the strongest claim on us — bundle no
# license text of their own. A URL would not discharge that.
FULL_TEXT = [
    "Apache-2.0",
    "MIT",
    "BSD-3-Clause",
    "BSD-2-Clause",
    "EDL-1.0",
    "EPL-2.0",
]

LICENSE_URLS = {
    "Apache-2.0": "https://www.apache.org/licenses/LICENSE-2.0",
    "MIT": "https://opensource.org/license/mit",
    "MIT-0": "https://opensource.org/license/mit-0",
    "BSD-3-Clause": "https://opensource.org/license/bsd-3-clause",
    "BSD-2-Clause": "https://opensource.org/license/bsd-2-clause",
    "EDL-1.0": "https://www.eclipse.org/org/documents/edl-v10.php",
    "EPL-2.0": "https://www.eclipse.org/legal/epl-2.0/",
    "CC0-1.0": "https://creativecommons.org/publicdomain/zero/1.0/legalcode",
}


def read_report():
    rows = []
    with open(REPORT, encoding="utf-8") as fh:
        for line in fh:
            line = line.rstrip("\n")
            if not line.strip():
                continue
            parts = line.split("\t")
            if len(parts) < 5:
                sys.exit(f"malformed report row: {line!r}")
            rows.append(
                {
                    "ga": parts[0],
                    "version": parts[1],
                    "raw": parts[2],
                    "source": parts[3],
                    "pom": parts[4],
                }
            )
    return rows


def resolve_license(row):
    ga, raw = row["ga"], row["raw"]
    if ga in ELECTIONS:
        elected, reviewed_raw, _ = ELECTIONS[ga]
        # ELECTIONS is keyed without a version, so a bump silently carries the
        # election forward. That is only safe while the POM still offers the
        # same choice it offered when a human made the election.
        if raw != reviewed_raw:
            sys.exit(
                f"{ga}:{row['version']} declares {raw!r}, but its ELECTIONS "
                f"entry was reviewed against {reviewed_raw!r}. The upstream "
                "licence offering changed — re-review the election before "
                "regenerating."
            )
        return elected
    if ";" in raw:
        sys.exit(
            f"{ga}:{row['version']} declares multiple licenses ({raw!r}) but "
            "has no entry in ELECTIONS. A dual-licensed dependency needs a "
            "reviewed election."
        )
    if raw not in LICENSE_MAP:
        sys.exit(
            f"{ga}:{row['version']} declares license {raw!r}, which is not in "
            "LICENSE_MAP. Add a reviewed mapping before regenerating."
        )
    return LICENSE_MAP[raw]


def collect_notices():
    """Group bundled NOTICE files by content so identical text appears once."""
    by_hash = OrderedDict()
    if not os.path.isdir(NOTICE_DIR):
        return by_hash
    for name in sorted(os.listdir(NOTICE_DIR)):
        path = os.path.join(NOTICE_DIR, name)
        with open(path, encoding="utf-8", errors="replace") as fh:
            body = fh.read().strip()
        if not body:
            continue
        # Reverse of generate-third-party-notices.sh's `tr ':/' '+~'`
        # encoding. "+" and "~" cannot appear inside a Maven
        # groupId/artifactId/version, so this decode is unambiguous — unlike
        # the "_" it replaces, which real artifact names can contain.
        gav = name.replace(".NOTICE.txt", "").replace("+", ":").replace("~", "/")
        # The notices themselves are extracted per resolved version; this file
        # reports per group:artifact (ADR-022), so drop the version and let
        # the set collapse the duplicates two co-resolved versions produce.
        ga = gav.rsplit(":", 1)[0]
        digest = hashlib.sha256(body.encode("utf-8")).hexdigest()
        by_hash.setdefault(digest, {"body": body, "gas": set()})["gas"].add(ga)
    return by_hash


def git_commit():
    try:
        return subprocess.check_output(
            ["git", "-C", REPO_ROOT, "rev-parse", "--short", "HEAD"],
            text=True,
        ).strip()
    except Exception:
        return "unknown"


def main():
    if not os.path.exists(REPORT):
        sys.exit(
            "build/third-party-notices/report.tsv not found — run "
            "scripts/generate-third-party-notices.sh first."
        )
    rows = read_report()

    # One `group:artifact` can resolve to several versions at once (the three
    # modules' runtimeClasspaths are unioned). This file reports per artifact,
    # not per version (ADR-022), so collapse them — and refuse to collapse two
    # versions that disagree about the license, which is a real finding, not a
    # formatting problem.
    by_ga = OrderedDict()
    for row in rows:
        spdx = resolve_license(row)
        entry = by_ga.get(row["ga"])
        if entry is None:
            by_ga[row["ga"]] = {"ga": row["ga"], "spdx": spdx, "versions": [row["version"]]}
            continue
        if entry["spdx"] != spdx:
            sys.exit(
                f"{row['ga']} resolves at versions {entry['versions'][0]} and "
                f"{row['version']} with different licenses ({entry['spdx']} vs "
                f"{spdx}). Pin one version, or split the entry, after review."
            )
        entry["versions"].append(row["version"])

    entries = list(by_ga.values())
    groups = defaultdict(list)
    for entry in entries:
        groups[entry["spdx"]].append(entry)

    notices = collect_notices()
    apache_gas = {e["ga"] for e in groups.get("Apache-2.0", [])}

    out = []
    w = out.append

    w("# Third-Party Notices")
    w("")
    w(
        "This file lists the third-party software distributed with "
        "norintegrate, together with the notices those licenses require."
    )
    w("")
    w("## Scope")
    w("")
    w(
        "**In scope:** the runtime dependencies (Gradle `runtimeClasspath`) of "
        "the three JVM modules — `norintegrate-api`, `norintegrate-common` and "
        "`norintegrate-mcp`. These modules are shipped as fat JARs inside the "
        "`api` and `mcp` Docker images, so every artifact listed here is "
        "actually redistributed."
    )
    w("")
    w(
        "**Out of scope:** the npm dependencies of `norintegrate-web`. The web "
        "image ships a Next.js `.next/standalone` bundle, so it does carry "
        "third-party npm code. Both its copyleft (LGPL) components and, since "
        "NOR-31, its permissive- and CC-BY-4.0-licensed npm dependencies are "
        "covered by `THIRD-PARTY-NOTICES-WEB.md` instead."
    )
    w("")
    w(
        "**Not included:** build-only dependencies (ktlint, JaCoCo, the Kotlin "
        "Gradle plugin, and the rest of the build classpath). They are not part "
        "of any distributed artifact, so they carry no redistribution notice "
        "obligation."
    )
    w("")
    w("## How this file is generated")
    w("")
    w("```bash")
    w("scripts/generate-third-party-notices.sh   # collect POM licenses + NOTICE files")
    w("scripts/render-third-party-notices.py     # render this file")
    w("```")
    w("")
    w(
        "The first script resolves each artifact's license from its Maven "
        "Central POM (walking the `<parent>` chain when a POM declares none) "
        "and extracts any `META-INF/NOTICE` bundled in its JAR. The second "
        "applies the reviewed SPDX normalization and dual-license elections, "
        "both of which are explicit in the script rather than inferred."
    )
    w("")
    w(
        "Which artifacts ship is a reviewed list of `group:artifact` "
        "coordinates in `scripts/generate-third-party-notices.sh`; their "
        "versions are not recorded anywhere, but resolved from Gradle's "
        "`resolvedArtifacts` API on every run — **not** by parsing `./gradlew "
        "dependencies` tree output, which both invents entries (BOM/platform "
        "pseudo-artifacts declare no code) and drops real ones (`(*)` "
        "conflict-resolved starters). The generator refuses to run if the "
        "resolved artifact set differs from the reviewed one."
    )
    w("")
    w(
        "Artifacts are therefore listed below **without versions** (ADR-022). "
        "A version-pinned list is invalidated by every routine dependency "
        "bump, and repairing it needs the network and a populated Gradle "
        "cache — which is why Dependabot could never make its own pull "
        "requests pass. What the licenses here oblige us to reproduce — the "
        "artifact, its license, its NOTICE text — does not depend on the "
        "version. The exact versions that shipped are recorded in "
        "`build/third-party-notices/report.tsv` at generation time, and are "
        "resolvable at any point from the committed Gradle build files."
    )
    w("")
    w(
        "To check that this file still describes what actually ships — the "
        "failure mode a fixed list invites — run:"
    )
    w("")
    w("```bash")
    w("scripts/check-third-party-drift.sh")
    w("```")
    w("")
    w(
        "It re-resolves `runtimeClasspath` and diffs it against the recorded "
        "list at `group:artifact` granularity, exiting non-zero if an "
        "artifact has been added or dropped."
    )
    w("")
    w(f"- Generated: {date.today().isoformat()}")
    w(f"- Source commit: `{git_commit()}`")
    w(f"- Artifacts covered: **{len(entries)}**")
    w("")
    w("## Summary")
    w("")
    w("| License | Artifacts |")
    w("|---------|-----------|")
    for spdx in sorted(groups, key=lambda k: (-len(groups[k]), k)):
        w(f"| {spdx} | {len(groups[spdx])} |")
    w(f"| **Total** | **{len(entries)}** |")
    w("")
    epl_count = len(groups.get("EPL-2.0", []))
    w(
        "No dependency is under a strong copyleft license (the GPL/AGPL/SSPL "
        f"family); none is present. {epl_count} dependencies are under "
        "EPL-2.0, which is *weak* copyleft: its reciprocal obligations attach "
        "to modifications of the EPL-licensed files themselves, not to code "
        "that merely depends on them. norintegrate consumes all of them as "
        "unmodified binaries from Maven Central, so no source-disclosure "
        "obligation attaches to norintegrate's own code. Where an artifact is "
        "offered under a choice of licenses, the elected license is stated "
        "explicitly with the artifact."
    )
    w("")

    if ELECTIONS:
        w("## Dual-licensed dependencies and elected licenses")
        w("")
        for ga in sorted(ELECTIONS):
            if ga not in by_ga:
                continue
            spdx, _, why = ELECTIONS[ga]
            w(f"- **`{ga}`** → elected **{spdx}**. {why}")
        w("")

    if AMBIGUOUS:
        present = {g: n for g, n in AMBIGUOUS.items() if g in by_ga}
        if present:
            w("## Dependencies with an imprecise upstream license declaration")
            w("")
            for ga in sorted(present):
                w(f"- **`{ga}`** — {present[ga]}")
            w("")

    w("## Dependencies by license")
    w("")
    for spdx in sorted(groups, key=lambda k: (-len(groups[k]), k)):
        url = LICENSE_URLS.get(spdx)
        w(f"### {spdx}" + (f" ({url})" if url else ""))
        w("")
        for entry in sorted(groups[spdx], key=lambda e: e["ga"]):
            note = ""
            if entry["ga"] in ELECTIONS:
                note = " — elected, see above"
            elif entry["ga"] in AMBIGUOUS:
                note = " — see note above"
            w(f"- `{entry['ga']}`{note}  ")
            # Version-independent landing page rather than the POM URL the
            # license was read from: a versioned link would put back exactly
            # the staleness this file was changed to avoid.
            group, artifact = entry["ga"].split(":", 1)
            w(f"  <sub>[Maven Central](https://central.sonatype.com/artifact/{group}/{artifact})</sub>")
        w("")

    w("## Bundled NOTICE files (Apache-2.0 section 4(d))")
    w("")
    if not notices:
        w("No dependency bundles a `META-INF/NOTICE` file.")
        w("")
    else:
        total = sum(len(v["gas"]) for v in notices.values())
        w(
            f"{total} of the distributed artifacts bundle a `META-INF/NOTICE` "
            f"file. Their contents are reproduced below, deduplicated to "
            f"{len(notices)} distinct notices — several artifacts from the same "
            "project ship byte-identical text."
        )
        w("")
        for entry in notices.values():
            gas = sorted(entry["gas"])
            shown = [g for g in gas if g in apache_gas] or gas
            w("<details>")
            label = shown[0] + (f" (+{len(gas) - 1} more)" if len(gas) > 1 else "")
            w(f"<summary><code>{label}</code></summary>")
            w("")
            if len(gas) > 1:
                w("Applies to:")
                w("")
                for g in gas:
                    w(f"- `{g}`")
                w("")
            w("```text")
            w(entry["body"])
            w("```")
            w("")
            w("</details>")
            w("")

    w("## License texts")
    w("")
    for spdx in FULL_TEXT:
        if spdx not in groups:
            continue
        path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "license-texts", f"{spdx}.txt")
        w(f"### {spdx}")
        w("")
        if spdx == "EPL-2.0":
            w(
                "The EPL-2.0 artifacts above are consumed as unmodified "
                "binaries from Maven Central; norintegrate modifies none of "
                "them. Their source is available from the upstream projects. "
                "The full text is reproduced here because the logback JARs "
                "bundle no license text themselves."
            )
            w("")
        if os.path.exists(path):
            with open(path, encoding="utf-8") as fh:
                w("```text")
                w(fh.read().strip())
                w("```")
        else:
            w(f"Full text: {LICENSE_URLS.get(spdx, '(see upstream)')}")
        w("")

    for spdx in sorted(groups):
        if spdx in FULL_TEXT:
            continue
        w(f"### {spdx}")
        w("")
        w(f"Full text: {LICENSE_URLS.get(spdx, '(see upstream)')}")
        w("")

    with open(TARGET, "w", encoding="utf-8") as fh:
        fh.write("\n".join(out).rstrip() + "\n")
    print(
        f"wrote {TARGET} ({len(entries)} artifacts from {len(rows)} "
        f"resolved versions, {len(groups)} licenses)"
    )


if __name__ == "__main__":
    main()
