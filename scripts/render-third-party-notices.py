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
# gav -> (elected SPDX id, rationale shown in the file)
ELECTIONS = {
    "ch.qos.logback:logback-classic:1.5.34": (
        "EPL-2.0",
        "Dual-licensed EPL-2.0 or LGPL-2.1-only. **EPL-2.0 is elected.** "
        "The LGPL-2.1-only option is expressly *not* taken, so LGPL's "
        "relinking/source-substitution obligations do not apply here.",
    ),
    "ch.qos.logback:logback-core:1.5.34": (
        "EPL-2.0",
        "Dual-licensed EPL-2.0 or LGPL-2.1-only. **EPL-2.0 is elected.** "
        "The LGPL-2.1-only option is expressly *not* taken, so LGPL's "
        "relinking/source-substitution obligations do not apply here.",
    ),
    "jakarta.annotation:jakarta.annotation-api:3.0.0": (
        "EPL-2.0",
        "Dual-licensed EPL-2.0 or GPL-2.0-only WITH Classpath-exception-2.0. "
        "**EPL-2.0 is elected**; the GPL option is not taken.",
    ),
    "jakarta.transaction:jakarta.transaction-api:2.0.1": (
        "EPL-2.0",
        "Dual-licensed EPL-2.0 or GPL-2.0-only WITH Classpath-exception-2.0. "
        "**EPL-2.0 is elected**; the GPL option is not taken.",
    ),
    "jakarta.persistence:jakarta.persistence-api:3.2.0": (
        "EDL-1.0",
        "Dual-licensed EPL-2.0 or EDL-1.0. **EDL-1.0 is elected** (the "
        "BSD-3-Clause-equivalent option).",
    ),
    "net.logstash.logback:logstash-logback-encoder:9.0": (
        "Apache-2.0",
        "Dual-licensed Apache-2.0 or MIT. **Apache-2.0 is elected.**",
    ),
    "org.hdrhistogram:HdrHistogram:2.2.2": (
        "CC0-1.0",
        "Offered as CC0-1.0 (public domain dedication) or BSD-2-Clause. "
        "**CC0-1.0 is elected**; the BSD-2-Clause text is reproduced below "
        "as well, since the upstream POM presents the two together.",
    ),
}

# Artifacts whose declared license name is ambiguous in the upstream POM.
AMBIGUOUS = {
    "org.antlr:ST4:4.3.4": 'POM declares only "The BSD License" without '
    "naming the clause count; recorded as BSD-3-Clause (stricter reading).",
    "org.antlr:antlr-runtime:3.5.3": 'POM declares only "BSD licence" without '
    "naming the clause count; recorded as BSD-3-Clause (stricter reading).",
}

# Licenses whose full text must travel with the distribution.
FULL_TEXT = ["Apache-2.0", "MIT", "BSD-3-Clause", "BSD-2-Clause", "EDL-1.0"]

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
            if len(parts) < 4:
                sys.exit(f"malformed report row: {line!r}")
            rows.append(
                {"gav": parts[0], "raw": parts[1], "source": parts[2], "pom": parts[3]}
            )
    return rows


def resolve_license(row):
    gav, raw = row["gav"], row["raw"]
    if gav in ELECTIONS:
        return ELECTIONS[gav][0]
    if ";" in raw:
        sys.exit(
            f"{gav} declares multiple licenses ({raw!r}) but has no entry in "
            "ELECTIONS. A dual-licensed dependency needs a reviewed election."
        )
    if raw not in LICENSE_MAP:
        sys.exit(
            f"{gav} declares license {raw!r}, which is not in LICENSE_MAP. "
            "Add a reviewed mapping before regenerating."
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
        gav = name.replace(".NOTICE.txt", "").replace("_", ":")
        digest = hashlib.sha256(body.encode("utf-8")).hexdigest()
        by_hash.setdefault(digest, {"body": body, "gavs": []})["gavs"].append(gav)
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
    groups = defaultdict(list)
    for row in rows:
        groups[resolve_license(row)].append(row)

    notices = collect_notices()
    apache_gavs = {r["gav"] for r in groups.get("Apache-2.0", [])}

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
        "third-party npm code; that inventory is being handled separately and "
        "is *not* covered by this file."
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
        "The dependency list itself is a reviewed snapshot captured from "
        "Gradle's `resolvedArtifacts` API, **not** from parsing `./gradlew "
        "dependencies` tree output — that text form both invents entries "
        "(BOM/platform pseudo-artifacts declare no code) and drops real ones "
        "(`(*)` conflict-resolved starters). If the dependency graph changes, "
        "re-capture the list and re-run both scripts."
    )
    w("")
    w(f"- Generated: {date.today().isoformat()}")
    w(f"- Source commit: `{git_commit()}`")
    w(f"- Artifacts covered: **{len(rows)}**")
    w("")
    w("## Summary")
    w("")
    w("| License | Artifacts |")
    w("|---------|-----------|")
    for spdx in sorted(groups, key=lambda k: (-len(groups[k]), k)):
        w(f"| {spdx} | {len(groups[spdx])} |")
    w(f"| **Total** | **{len(rows)}** |")
    w("")
    w(
        "No dependency in this list is under a copyleft license that would "
        "impose source-disclosure obligations on norintegrate's own code. "
        "Where an artifact is offered under a choice of licenses, the elected "
        "license is stated explicitly with the artifact."
    )
    w("")

    if ELECTIONS:
        w("## Dual-licensed dependencies and elected licenses")
        w("")
        for gav in sorted(ELECTIONS):
            if gav not in {r["gav"] for r in rows}:
                continue
            spdx, why = ELECTIONS[gav]
            w(f"- **`{gav}`** → elected **{spdx}**. {why}")
        w("")

    if AMBIGUOUS:
        present = {g: n for g, n in AMBIGUOUS.items() if g in {r["gav"] for r in rows}}
        if present:
            w("## Dependencies with an imprecise upstream license declaration")
            w("")
            for gav in sorted(present):
                w(f"- **`{gav}`** — {present[gav]}")
            w("")

    w("## Dependencies by license")
    w("")
    for spdx in sorted(groups, key=lambda k: (-len(groups[k]), k)):
        url = LICENSE_URLS.get(spdx)
        w(f"### {spdx}" + (f" ({url})" if url else ""))
        w("")
        for row in sorted(groups[spdx], key=lambda r: r["gav"]):
            note = ""
            if row["gav"] in ELECTIONS:
                note = " — elected, see above"
            elif row["gav"] in AMBIGUOUS:
                note = " — see note above"
            w(f"- `{row['gav']}`{note}  ")
            w(f"  <sub>[POM]({row['pom']})</sub>")
        w("")

    w("## Bundled NOTICE files (Apache-2.0 section 4(d))")
    w("")
    if not notices:
        w("No dependency bundles a `META-INF/NOTICE` file.")
        w("")
    else:
        total = sum(len(v["gavs"]) for v in notices.values())
        w(
            f"{total} of the distributed artifacts bundle a `META-INF/NOTICE` "
            f"file. Their contents are reproduced below, deduplicated to "
            f"{len(notices)} distinct notices — several artifacts from the same "
            "project ship byte-identical text."
        )
        w("")
        for entry in notices.values():
            gavs = sorted(entry["gavs"])
            shown = [g for g in gavs if g in apache_gavs] or gavs
            w("<details>")
            label = shown[0] + (f" (+{len(gavs) - 1} more)" if len(gavs) > 1 else "")
            w(f"<summary><code>{label}</code></summary>")
            w("")
            if len(gavs) > 1:
                w("Applies to:")
                w("")
                for g in gavs:
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
        if spdx == "EPL-2.0":
            w("")
            w(
                "The EPL-2.0 artifacts above are consumed as unmodified binaries "
                "from Maven Central. Their source is available from the upstream "
                "projects; norintegrate makes no modifications to them."
            )
        w("")

    with open(TARGET, "w", encoding="utf-8") as fh:
        fh.write("\n".join(out).rstrip() + "\n")
    print(f"wrote {TARGET} ({len(rows)} artifacts, {len(groups)} licenses)")


if __name__ == "__main__":
    main()
