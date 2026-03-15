# ADR-006: Jenkins to GitHub Actions CI/CD Migration

**Status:** Accepted
**Date:** 2026-03-14

## Context

The team's prior CI/CD experience is with Jenkins, a self-hosted, plugin-driven automation server. Jenkins is powerful and highly configurable, but it comes with significant operational overhead: maintaining the Jenkins master and agent nodes, managing plugin version compatibility, and writing Groovy-based `Jenkinsfile` pipelines that are often only partially type-checked.

NorIntegrate is hosted on GitHub. The alternatives evaluated were Jenkins (self-hosted, familiar) and GitHub Actions (managed, co-located with source).

Key factors favouring GitHub Actions:

- **Managed infrastructure**: GitHub provides hosted runners (ubuntu-latest) at no cost for public repositories. There is no server to provision, patch, or monitor — a meaningful difference for a single-developer project.
- **YAML-native pipelines**: Workflows are declared in `.github/workflows/` as YAML files, versioned alongside the source code. There is no separate Jenkins instance to keep in sync with the repo.
- **Path filtering**: GitHub Actions supports `on.push.paths` filters, allowing a workflow to trigger only when files under a specific module directory change. In a monorepo (ADR-009), this prevents rebuilding `norintegrate-mcp` when only `norintegrate-api` changes. Jenkins requires a plugin (e.g. Generic Webhook Trigger) and custom logic to achieve the same result.
- **Marketplace actions**: Standard steps such as `actions/checkout`, `actions/setup-java`, and `gradle/gradle-build-action` are maintained by their respective owners and require no plugin management.
- **Secrets management**: GitHub repository secrets are natively available to Actions workflows, eliminating the need for a separate credentials store.

## Decision

Use GitHub Actions for all CI/CD. Each deployable module has a dedicated workflow file in `.github/workflows/`, path-filtered to its module directory and the common library. Jenkins is not used.

## Consequences

- Build definitions are plain YAML, readable and diffable in pull requests.
- Path-filtered workflows keep CI fast: changing a controller in `norintegrate-api` does not trigger the MCP server build.
- The team must learn the GitHub Actions event model and expression syntax, but Groovy/Jenkinsfile knowledge is no longer required.
- Self-hosted runners can be added later if private infrastructure is needed, without changing the workflow YAML significantly.
