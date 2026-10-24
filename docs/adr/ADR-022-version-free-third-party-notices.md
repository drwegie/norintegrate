# ADR-022: Version-Free Third-Party Notices Inventories

**Status:** Accepted
**Date:** 2026-10-24

## Context

NorIntegrate carries two generated legal documents: `THIRD-PARTY-NOTICES.md` for the JVM modules' `runtimeClasspath`, and `THIRD-PARTY-NOTICES-WEB.md` for `norintegrate-web`'s npm tree. Each is guarded by a CI check that fails when the committed file stops describing what actually ships — `scripts/check-third-party-drift.sh` in `.github/workflows/docs.yml` (which runs on every push), and `scripts/check-third-party-notices-web-drift.sh` in `.github/workflows/web.yml` and `docker.yml`.

Both guards recorded dependencies **with their versions**: `group:artifact:version` on the JVM side, `name@version` in the web permissive inventory and its Entries table. That made the guards fire on *any* version change, not just on a change to what ships.

Repairing either file requires running its generator. The web generator needs an installed `node_modules` tree; the JVM generator needs network access to Maven Central and a populated Gradle module cache. Dependabot has none of these, so **every Dependabot pull request that moved a shipped dependency failed a required check and could not be made green by the bot.** The standing workaround was manual: open a human branch, repeat the bump there, regenerate, and close the Dependabot PR as superseded. That cost a full review cycle per bump, on exactly the updates — security patches — that should land fastest.

The root cause is not Dependabot. It is that *generated* lists were gated at a granularity finer than the obligation they record.

## Decision

Neither notices file records versions for the artifacts it attributes, and neither drift check compares them.

**Web (`THIRD-PARTY-NOTICES-WEB.md`).** The permissive inventory and the Entries table record `name` and licence only. The copyleft inventory **keeps** `name@version`.

**JVM (`THIRD-PARTY-NOTICES.md`).** The reviewed `ARTIFACTS` list in `scripts/generate-third-party-notices.sh` is now 151 `group:artifact` coordinates. Versions are no longer recorded anywhere: the generator resolves them live through the new `scripts/lib/resolve-runtime-artifacts.sh` (shared with the drift check, so "what ships" has one definition) and refuses to run if the resolved artifact set differs from the reviewed one. The rendered file lists artifacts without versions and links to each artifact's version-independent Maven Central page instead of the POM the licence was read from.

Three alternatives were weighed:

- **CI regenerates the notices and pushes to the PR branch.** Keeps versions, but a push made with `GITHUB_TOKEN` does not re-trigger checks, so the gate would sit permanently "expected" on the new head commit. Making it work needs a fine-grained PAT or a GitHub App token held as a repository secret, plus a bot that writes to Dependabot branches. A standing credential and a write-back bot are a large amount of new surface for a formatting problem — and on the JVM side the bot would also need Maven Central access and a warm Gradle cache.
- **Generate the sections at image-build time and commit only an approved `(package, licence)` set.** The most thorough removal of the derived-artifact-in-git problem, but it restructures two reviewed legal documents and five scripts, and it takes the notices text out of the repository where it is currently reviewable in a diff.
- **Keep the status quo and absorb the manual folding.** Rejected: it is a recurring cost on the updates that matter most, and it had already produced a documented workaround rather than a fix.

## Consequences

**The gates now fire on substance, not noise.** A version bump of an already-recorded dependency produces no drift, so a Dependabot PR goes green on its own. A new dependency, a dropped one, or a changed licence still fails — and those are precisely the cases where the hand-written prose, the SPDX normalization map and the dual-licence elections have to be re-read by a person. Both checks got *more* informative by checking less. This was verified on both sides: a `@auth/core` patch bump and a Tomcat `11.0.26 → 11.0.25` bump both pass, while adding an npm package, changing a declared licence, and a PostgreSQL downgrade that drops `org.checkerframework:checker-qual` from the graph all still fail.

**Attribution is unaffected.** What MIT, ISC, BSD, Apache-2.0, 0BSD, EPL-2.0, EDL-1.0 and CC-BY-4.0 oblige us to reproduce is the artifact, its licence, its copyright line and its bundled `NOTICE` text. None of those is a function of the version.

**Copyleft keeps its versions, on purpose.** For the `@img/sharp-libvips-*` family the version *is* the obligation: LGPL-3.0 §4(d) corresponding source is a link to a specific libvips and sharp-libvips tag, and the prose names those tags. A `sharp` bump must still stop a PR and get read by a person, so the copyleft block is still compared at `name@version`.

**JVM version data is now fresher, not staler.** Previously the versions in `ARTIFACTS` were whatever a human last typed; now every run fetches POMs and `META-INF/NOTICE` files for the versions Gradle actually resolves, and the exact set is written to `build/third-party-notices/report.tsv`. The reviewed part — which artifacts ship — stays a human decision, enforced in both the generator and the drift check.

**Elections are now keyed by `group:artifact`, with a guard.** A dual-licence election carried forward across a bump would be unsafe if upstream changed what it offers, so each `ELECTIONS` entry now also records the exact POM declaration it was reviewed against, and `render-third-party-notices.py` fails if a POM declares anything else. The same renderer refuses to collapse two co-resolved versions of one artifact that disagree about the licence — which is how `io.swagger.core.v3:swagger-annotations-jakarta`, resolved at two versions at once, became a single entry safely.

**`scripts/lib/check-third-party-notices-web-build.js` is correspondingly looser.** A package found in the standalone build output now counts as documented if its exact `name@version` is in the copyleft block *or* its bare name is in the permissive block. A genuinely new package reaching the build output still fails the check.

**Neither file states which version of each dependency shipped.** That information is still in `norintegrate-web/package-lock.json` and in the committed Gradle build files, both of which are versioned alongside the image. Nothing in the licences requires it in the notices, and a version-less file is silent where a stale one would have been wrong.
