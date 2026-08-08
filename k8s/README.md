# Kubernetes Manifests

Plain Kubernetes manifests for `norintegrate-api`, `norintegrate-mcp`, and `norintegrate-web`, organized as a [kustomize](https://kubectl.docs.kubernetes.io/references/kustomize/) base plus two overlays. See [ADR-020](../docs/adr/ADR-020-kubernetes-manifests-alongside-ecs-fargate.md) for why this exists alongside the ECS Fargate deployment in `infra/` (ADR-016) and why Helm was not used.

**No continuous hosting.** These manifests were verified against a local, ephemeral [OrbStack](https://orbstack.dev/) Kubernetes cluster and torn down afterward. Nothing described here runs anywhere persistently — there is no live cluster, no public endpoint, and no ongoing cost. This mirrors the posture ADR-016 documents for the (also torn down) ECS environment.

```
k8s/
├── base/                  # Shared Deployments, Services, ConfigMap, Ingress
└── overlays/
    ├── local/              # OrbStack smoke-testing: local images, in-cluster postgres
    └── prod-example/       # Illustrative only — placeholder hosts/tags, not deployable as-is
```

## Prerequisites

- `kubectl` (this repo was verified with v1.33.9, which bundles kustomize v5.6.0 — no separate kustomize install needed)
- A local Kubernetes cluster for the `local` overlay (this repo used OrbStack: `orbctl config set k8s.enable true`)
- `docker compose build api mcp web` to produce the images the `local` overlay references (`norintegrate-{api,mcp,web}:latest`)

## Namespace and Secrets (created out-of-band)

The `Namespace` and all `Secret` objects are **not** part of either overlay's `resources:`, on purpose: if the `Namespace` were included, `kubectl delete -k` would delete it — and every Secret in it — which breaks a second `apply` cycle with `CreateContainerConfigError` (Secret not found). Create them once, before applying either overlay:

```bash
kubectl create namespace norintegrate

kubectl -n norintegrate create secret generic norintegrate-db \
  --from-literal=DB_USERNAME=norintegrate \
  --from-literal=DB_PASSWORD="$(openssl rand -base64 24 | tr -d '/+=')"

kubectl -n norintegrate create secret generic norintegrate-api \
  --from-literal=JWT_ISSUER_URI=https://accounts.google.com

kubectl -n norintegrate create secret generic norintegrate-web \
  --from-literal=NEXTAUTH_SECRET="$(openssl rand -base64 32)" \
  --from-literal=GOOGLE_CLIENT_ID=local-smoke-placeholder \
  --from-literal=GOOGLE_CLIENT_SECRET=local-smoke-placeholder
```

No manifest in this directory contains a secret value — every sensitive env var is wired via `secretKeyRef` (see `grep -rn 'password\|secret:' k8s/ | grep -v secretKeyRef`, which should print nothing).

## Verification

### Render (no cluster required)

```bash
kubectl kustomize k8s/overlays/local
kubectl kustomize k8s/overlays/prod-example
```

CI (`.github/workflows/k8s.yml`) runs both, plus `kubeconform -strict`, on every change under `k8s/**`.

### Local smoke test (requires a cluster — not run in CI)

After creating the namespace/Secrets above:

```bash
kubectl apply -k k8s/overlays/local
kubectl -n norintegrate rollout status deployment/postgres --timeout=180s
kubectl -n norintegrate rollout status deployment/api --timeout=180s
kubectl -n norintegrate rollout status deployment/mcp --timeout=180s
kubectl -n norintegrate rollout status deployment/web --timeout=180s
kubectl -n norintegrate get pods -o wide
```

Functional check via port-forward (Ingress is not part of this check — see below):

```bash
kubectl -n norintegrate port-forward svc/api 18080:8080 &
kubectl -n norintegrate port-forward svc/mcp 18081:8081 &
kubectl -n norintegrate port-forward svc/web 13000:3000 &
curl -fsS http://127.0.0.1:18080/actuator/health   # {"status":"UP"}
curl -fsS http://127.0.0.1:18081/actuator/health
curl -fsS -o /dev/null -w '%{http_code}\n' http://127.0.0.1:13000/   # 200 or 307
```

Tear down (leaves the Namespace/Secrets intact for a repeat run):

```bash
kubectl delete -k k8s/overlays/local
kubectl -n norintegrate wait --for=delete pod --all --timeout=120s
```

When fully done:

```bash
kubectl delete namespace norintegrate
```

### Why Ingress isn't a pass/fail check

`k8s/base/ingress.yaml` documents the same path routing as `infra/alb.tf`'s ALB listener rules (`/api`, `/actuator` → api; `/mcp` → mcp; `/` → web), but OrbStack ships no ingress controller by default. Whether an `Ingress` object resolves is a property of the cluster's add-ons, not of these manifests, so functional verification uses `kubectl port-forward` against the Services instead. `overlays/prod-example/patch-ingress.yaml` additionally assumes an `ingress-nginx` controller and a TLS secret this project does not operate.

## `overlays/local` vs `overlays/prod-example`

| | `local` | `prod-example` |
|---|---|---|
| Purpose | Actually applied and smoke-tested against OrbStack | Illustrative — shows how the base would be shaped for a real cluster; **not deployable as committed** |
| Images | `norintegrate-{api,mcp,web}:latest`, built locally, `imagePullPolicy: IfNotPresent` | `ghcr.io/drwegie/norintegrate-*:REPLACE_WITH_RELEASE_TAG` — the tag is a placeholder on purpose |
| Database | In-cluster `postgres:18-alpine` Deployment (`overlays/local/postgres.yaml`), `emptyDir` storage | External managed database — `SPRING_DATASOURCE_URL` points at `REPLACE_WITH_RDS_ENDPOINT` |
| Ingress host | `norintegrate.localhost`, no TLS | `norintegrate.example.com` placeholder, `ingressClassName: nginx`, TLS via `secretName: norintegrate-tls` |

## Health endpoints: why the probes do not use `/actuator/health`

The obvious probe target does not work on either JVM service, and this was found
by actually running the manifests on a cluster rather than by reading the code:

| Service | `GET /actuator/health` | Probes actually used |
|---|---|---|
| api | **401** — every `/actuator/*` path is rejected, even though `SecurityConfig.java` lists `/actuator/health`, `/actuator/info` and `/actuator/prometheus` under `permitAll`. MVC-mapped paths such as `GET /api/v1/visa-types` do honour `permitAll` and return 200. | startup + readiness on `GET /api/v1/visa-types` (200, and it exercises the datasource); liveness on the TCP socket |
| mcp | **404** — no actuator endpoint is registered at runtime, although `spring-boot-starter-actuator` is a dependency and `application.yml` exposes `health`. `/`, `/mcp` and `/sse` also answer 404, so no HTTP path returns 200. | startup, liveness and readiness on the TCP socket |
| web | 200 on `/` | `GET /` |

This is an **application defect, not a Kubernetes one**. The same api image run
under `docker compose` returns 401 on `/actuator/health` and 200 on
`/api/v1/visa-types`, exactly as it does in-cluster.

It also affects the existing ECS deployment: `infra/alb.tf` configures the api
and mcp target groups with `health_check.path = "/actuator/health"`, which
neither service answers with 200, so those target groups could never become
healthy. ADR-016 is in `Suspended` status and the stack is torn down, which is
presumably why this went unnoticed.

The probes above are a deliberate, documented workaround so that these manifests
describe something that genuinely runs. They should be moved back to
`/actuator/health` once the underlying defect is fixed.

## `NEXT_PUBLIC_API_URL` does not take effect at runtime

Both overlays' `ConfigMap` set `NEXT_PUBLIC_API_URL`, but Next.js inlines every `NEXT_PUBLIC_*` env var into the client-side JavaScript bundle at `npm run build` time — it is not read from the process environment at container start. `docker/web.Dockerfile` does not accept it as a build `ARG`, so the value baked into the image is whatever it resolved to during the CI/local build, regardless of what a Pod's `ConfigMap` says at runtime. `infra/ecs.tf` sets the same env var for the same (non-)effect. This is an existing limitation of the frontend build, not something fixed by these manifests — noted here rather than left to look like it works (see ADR-020, Consequences).
