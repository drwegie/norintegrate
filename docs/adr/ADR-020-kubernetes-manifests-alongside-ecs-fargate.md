# ADR-020: Kubernetes Manifests Alongside ECS Fargate

## Status

Accepted

## Context

ADR-016 chose ECS Fargate for production hosting and documents that decision's rationale (cost, operational simplicity for three containers, native ALB path routing). That infrastructure was provisioned, validated, and subsequently torn down to avoid ongoing cost (~$53/month) — ADR-016 remains the record of the deployment target this project actually operated.

Closing the ECS task definitions as the only container-orchestration artifact in the repository leaves a gap: the same three services, expressed only as `infra/ecs.tf`, do not demonstrate the ability to target a different orchestrator. Kubernetes is the dominant orchestrator in the job market this project's portfolio targets (PORT-2), and the differentiator worth demonstrating is **kubectl-based operation** of a real manifest set — not a hosted cluster.

Constraints:

- **No continuously hosted cluster.** EKS's control plane alone was costed at ~$73/month and rejected by ADR-016 for the same cost-discipline reason ECS's dev environment was eventually torn down. Adding a second, more expensive control plane contradicts that reasoning.
- **No false claims of live infrastructure** (a direct lesson from NOR-4): this ADR and `k8s/README.md` describe what was verified locally, not what is continuously running.
- All three services already have multi-stage Dockerfiles (`docker/api.Dockerfile`, `docker/mcp.Dockerfile`, `docker/web.Dockerfile`) with ports, health endpoints, and non-root users already defined, and `docker-compose.yml` / `infra/ecs.tf` already encode the env vars, ports, and health checks each service needs — the manifests below are a mechanical re-expression of values that already exist in three other places in this repository, not a new design.

## Decision

Add `k8s/` with plain Kubernetes manifests organized as a kustomize base plus two overlays (`k8s/overlays/local`, `k8s/overlays/prod-example`). Helm is not used.

- **kustomize over Helm.** `kubectl kustomize` / `kubectl apply -k` is built into `kubectl` (v1.33.9 here, bundling kustomize v5.6.0) — no new tool to install, and every rendered object is plain YAML with no templating indirection to read through. At this project's scale (3 services × 2 overlays), Helm's value proposition — versioned chart packaging, a values schema, dependency charts — goes largely unused, while its Go-template layer would obscure the underlying objects a reader is trying to evaluate. Helm was not installed on the machine this ADR was written on; adopting it would have added an environment dependency for no scoped benefit.
- **Secrets are `secretKeyRef`-only; no secret value is ever committed.** `norintegrate-db` (`DB_USERNAME`, `DB_PASSWORD`), `norintegrate-api` (`JWT_ISSUER_URI`), and `norintegrate-web` (`NEXTAUTH_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`) are created out-of-band with `kubectl create secret generic` (documented in `k8s/README.md`), the same three-secret shape as the `secrets` blocks in `infra/ecs.tf`'s three task definitions. A production setup would use External Secrets Operator to source these from AWS Secrets Manager, mirroring `infra/ecs.tf`'s `valueFrom` references — but with no running cluster, committing a controller-dependent CRD reference that has never been exercised would be worse than not mentioning it at all, so this ADR states the intent without shipping the manifest.
- **`configMapGenerator` / `secretGenerator` are not used.** Both add a hash-suffix mechanism whose entire purpose is triggering a rolling restart on config change — a property that matters for a continuously running deployment and is not exercised by a project with no continuously running deployment. Plain `ConfigMap` + strategic-merge `patches` keeps the object count and concept count down.
- **Verification is two-layered.** Locally, against a real (OrbStack) Kubernetes cluster: `kubectl apply -k`, `rollout status` on every Deployment, and `kubectl delete -k`, repeated twice to catch state left over from the first cycle — see `k8s/README.md` for the exact commands and their output. `kubectl apply --dry-run=client` does not work without a live cluster (it still performs OpenAPI/API-group discovery against `--server`), so `kubectl kustomize` alone — which only proves the YAML assembles, not that it is valid — is not sufficient by itself. In CI, `.github/workflows/k8s.yml` renders every overlay with `kubectl kustomize` and validates the result with `kubeconform -strict`, path-filtered to `k8s/**` per the monorepo CI strategy (ADR-009).
- **No continuously hosted cluster.** `k8s/README.md` states plainly that this was verified locally against OrbStack's Kubernetes and is not deployed anywhere persistent, the same posture ADR-016 documents for the (currently torn down) ECS environment.

Values carried over from the existing Dockerfiles/compose/Terraform (not reinvented):

| | api | mcp | web |
|---|---|---|---|
| Container port | 8080 (`docker/api.Dockerfile`, `application.yml`) | 8081 (`docker/mcp.Dockerfile`, `application.yml`) | 3000 (`docker/web.Dockerfile`) |
| Liveness/readiness | `GET /actuator/health` — permitted unauthenticated by `SecurityConfig.java` | `GET /actuator/health` — `McpSecurityConfig.kt` permits all requests | `GET /` — same check `infra/alb.tf`'s web target group uses |
| `runAsUser`/`runAsGroup` | 100/101 | 100/101 | 1001/65533 |

The `runAsUser`/`runAsGroup` values were not guessed: both Dockerfiles create a non-numeric `USER` (`appuser`, `nextjs`), and `runAsNonRoot: true` alone is not sufficient — kubelet cannot resolve a non-numeric image `USER` to a UID and fails every container with `CreateContainerConfigError: container has runAsNonRoot and image has non-numeric user`. The actual UIDs/GIDs were obtained by running each built image with `docker run --entrypoint sh <image> -c id` and are set explicitly.

The Ingress path map (`/api`, `/actuator` → `api`; `/mcp` → `mcp`; `/` → `web`) mirrors `infra/alb.tf`'s listener rules one-for-one. Ingress is documentation of that routing shape, not part of the local verification: OrbStack ships no ingress controller by default, so functional verification uses `kubectl port-forward` instead (see `k8s/README.md`).

`overlays/prod-example/patch-config.yaml` sets `SPRING_PROFILES_ACTIVE=prod`, which activates the `on-profile: prod` Hikari-tuning block in both `application.yml` files. `infra/ecs.tf` never sets this env var, so that block has been dead code on the ECS deployment this project actually ran — a fact this migration surfaced as a side effect, not something ADR-016 anticipated. `overlays/prod-example` is not a functional regression fix for ECS; it is a more faithful *expression* of an existing Spring profile that ECS silently never activated.

### Alternatives considered and rejected

- **Vendoring `docs/schema.sql` into a `ConfigMap` for `overlays/local`.** kustomize's built-in load restrictor refused to reference a file outside the overlay directory (`security; file '.../docs/schema.sql' is not in or below '.../k8s/overlays/local'`, verified locally, exit 1). The workaround flag, `--load-restrictor=LoadRestrictionsNone`, does not exist on `kubectl apply -k` (only on the standalone `kustomize` binary), so using it would break the DoD's actual apply path. Instead, `overlays/local` sets `SPRING_JPA_HIBERNATE_DDL_AUTO=update` so Hibernate creates the schema itself; ADR-016's note that schema is applied manually against RDS is consistent with this — neither environment auto-seeds from `docs/seed.sql`.
- **`readOnlyRootFilesystem: true`.** Rejected for now: the JVM needs a writable `/tmp` and Next.js standalone output writes `.next/cache`. Achievable with two `emptyDir` mounts per pod, but that trades six extra volume declarations across three Deployments for a hardening property this project's threat model (ADR-017) does not currently require.
- **Ingress as a pass/fail verification gate.** Rejected — whether an ingress controller is installed is an OrbStack configuration detail unrelated to whether these manifests are correct. `port-forward` against the Services is the actual functional check; the Ingress documents the routing shape ALB already encodes.

## Consequences

### Positive

- The same three services now have two independent, machine-checked expressions of container orchestration (ECS task definitions, Kubernetes manifests), which is a stronger portfolio signal than either alone — and the K8s expression cost zero ongoing infrastructure spend to produce.
- `overlays/prod-example` corrects a real gap ADR-016's ECS setup left unaddressed (`SPRING_PROFILES_ACTIVE` never set), captured here rather than silently repeated.
- CI (`k8s.yml`) catches schema-level breakage (bad API version, missing required field, `pathType` typos) on every future `k8s/**` change, at zero CI-time cost to changes that do not touch `k8s/` (ADR-009's path-filter pattern).

### Negative

- `k8s/*.yaml` and `infra/ecs.tf` / `docker-compose.yml` are three independent sources of truth for the same ports, env vars, and health checks. `kubeconform` validates schema only — it cannot detect drift between them (e.g., a new env var added to `application.yml` but forgotten in `k8s/base/configmap.yaml`). That drift is caught by human review, not by CI, unlike the ADR indexing and version-claim checks `scripts/check-docs.sh` already automates for documentation.
- The local `apply` → `rollout status` → `delete` verification in `k8s/README.md` is a point-in-time result, not a standing guarantee — nothing re-runs it continuously, since there is no hosted cluster to run it against.
- `NEXT_PUBLIC_API_URL` is set in `overlays/prod-example/patch-config.yaml` for completeness, but — like the identically-named env var in `infra/ecs.tf` — it has no effect at runtime: Next.js inlines `NEXT_PUBLIC_*` variables into the client bundle at `npm run build` time, and `docker/web.Dockerfile` does not accept it as a build `ARG`. This is an existing, unresolved limitation of the frontend build, not something this ADR's manifests fix; `k8s/README.md` states this explicitly rather than implying the value takes effect.
