# ADR-009: Monorepo with Path-Filtered CI/CD

**Status:** Accepted
**Date:** 2026-03-14

## Context

NorIntegrate consists of three Gradle modules: `norintegrate-common`, `norintegrate-api`, and `norintegrate-mcp`. These could be hosted as three separate Git repositories (polyrepo) or as a single repository (monorepo).

The polyrepo approach was considered. Its primary advantages are strict isolation — each repo has its own commit history, issue tracker, and access control — and the guarantee that CI for one module cannot be triggered by changes to another. It is also the model many teams default to because it mirrors how libraries are published independently.

However, polyrepo creates friction that is disproportionate for a project of this size:

- **Cross-cutting changes** (e.g. adding a new field to a domain record in `norintegrate-common` and updating both consumers) require coordinated commits across three repositories and careful version management. In a monorepo, this is a single atomic commit.
- **Shared tooling**: Gradle version catalogs, `.editorconfig`, code style configuration, GitHub Actions reusable workflows, and Terraform all live in one place. In a polyrepo, these must be duplicated or extracted to a fourth "platform" repo.
- **Dependency management**: A polyrepo would require publishing `norintegrate-common` as a versioned JAR (to a registry or GitHub Packages) before `norintegrate-api` or `norintegrate-mcp` can consume it. In a monorepo Gradle build, `project(":norintegrate-common")` is a direct source dependency — no publishing step required during development.

The main risk of a monorepo — that a change to one module triggers unnecessary CI for all others — is addressed by GitHub Actions path filters. Each workflow declares `on.push.paths` to include only its module directory plus `norintegrate-common/` (since a change there affects all consumers).

## Decision

Host all modules in a single Git repository. Use GitHub Actions path filters (`on.push.paths`) on each workflow to scope builds to the relevant module directories.

## Consequences

- Domain-wide refactors are single atomic commits with a single PR and review.
- CI is not redundantly triggered: pushing to `norintegrate-api/` does not rebuild or re-test `norintegrate-mcp`.
- There is no `norintegrate-common` artifact version to manage; Gradle resolves it as a project dependency at build time.
- If modules ever need to be extracted into separate repositories (e.g. for independent team ownership), the clean module boundary defined in ADR-003 makes that feasible without major refactoring.
