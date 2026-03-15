# ADR-007: Terraform Infrastructure Strategy

**Status:** Accepted
**Date:** 2026-03-14

## Context

NorIntegrate requires cloud infrastructure: a managed PostgreSQL instance, a Kubernetes cluster (or equivalent container runtime), networking, and identity/access configuration. Infrastructure can be managed manually through a cloud console, through cloud-provider CLI scripts, or through an Infrastructure-as-Code (IaC) tool.

The options evaluated were:

- **Manual (console/CLI)**: Fast to start but produces undocumented, non-reproducible infrastructure. Unsuitable for a project intended to demonstrate professional engineering practices.
- **AWS CloudFormation / Azure ARM**: Provider-specific, verbose, and tightly coupled to one cloud.
- **Terraform 1.x (HashiCorp)**: Provider-agnostic, declarative HCL, mature plan/apply workflow, and widely adopted in the industry.
- **Pulumi**: Code-first IaC using general-purpose languages. Compelling, but adds a new runtime dependency and is less universally recognised in hiring/portfolio contexts.

Terraform was chosen for its plan/apply separation — a design where `terraform plan` produces a human-readable diff of intended changes before any mutation occurs. This is especially valuable for a solo developer: reviewing the plan before applying prevents accidental destruction of a database instance.

However, for this project scope, `terraform apply` is **not run in CI**. Infrastructure is defined in code under `infra/` to demonstrate IaC practices and make the intended cloud topology legible, but changes are applied manually after plan review. Automating `apply` would require secure remote state storage, fine-grained IAM roles for the CI runner, and a locking strategy — all of which exceed the project's current operational scope.

## Decision

Use Terraform 1.x for all infrastructure definition. Run `terraform plan` in CI to validate syntax and detect drift. Apply infrastructure changes manually. Remote state backend configuration is defined but the state file is not committed to source control.

## Consequences

- Infrastructure topology is documented as code and reviewable in pull requests.
- The CI pipeline catches Terraform syntax errors and breaking plan outputs before they reach production.
- A developer joining the project can read `infra/` to understand what cloud resources exist without consulting a console.
- `terraform apply` remains a manual, deliberate step — preventing automated destruction of stateful resources (the PostgreSQL instance in particular).
