# ADR-016: AWS ECS Fargate Deployment

**Status:** Suspended
**Date:** 2026-04-20
**Suspended:** 2026-05-30

## Context

NorIntegrate requires production hosting for three services: a Spring Boot REST API, a Spring Boot MCP server, and a Next.js frontend. All three already have multi-stage Dockerfiles. The infrastructure must be defined as code (ADR-007) and be cost-effective for a solo-developer project.

The options evaluated were:

- **Amazon EKS**: Full Kubernetes. Powerful and industry-standard, but the control plane alone costs ~$73/month — more than all other infrastructure combined for this project. The operational complexity of managing Kubernetes (ingress controllers, pod autoscaling, RBAC, kubectl) is disproportionate for three containers.
- **AWS App Runner**: Simpler container hosting, but limited: no path-based routing across multiple services behind a single load balancer, no fine-grained networking control, and no shared ALB to keep costs down.
- **AWS Lambda + API Gateway**: Would require rewriting the Spring Boot applications for cold-start optimisation (e.g., GraalVM native image or Spring Cloud Function). The current boot time of ~5–10 seconds is acceptable for long-running containers but unacceptable for Lambda cold starts.
- **ECS Fargate**: Managed container runtime without cluster management. Native ALB integration with path-based routing. Right-sized for a small number of services. Terraform has mature, well-documented ECS resources.

For the frontend specifically:

- **Vercel**: Purpose-built for Next.js with zero-config SSR, edge CDN, and preview deployments. However, it introduces a second cloud provider — splitting billing, secrets management, and networking. For a portfolio project demonstrating infrastructure skills, hosting the frontend behind the same ALB better demonstrates end-to-end cloud architecture.

For the container registry:

- **Amazon ECR**: Tightly integrated with ECS (faster pulls within the same AWS network), but costs $0.10/GB/month for storage and requires managing AWS credentials in CI for pushing images.
- **GitHub Container Registry (GHCR)**: Free for public repositories, and GitHub Actions can push images using the built-in `GITHUB_TOKEN` — no additional credentials needed. ECS supports pulling from any Docker registry via `repositoryCredentials` in the task definition.

## Decision

Deploy all three services (API, MCP, Web) to ECS Fargate behind a single Application Load Balancer. Use GHCR for container images. Use RDS PostgreSQL for the database. Define all infrastructure in Terraform under `infra/`.

Key design choices:

- **Public subnets for ECS tasks** with `assign_public_ip = true`, avoiding the ~$30/month cost of a NAT Gateway. Security groups restrict inbound traffic to the ALB only. RDS remains in private subnets.
- **No VPC endpoints**: With ECS in public subnets, tasks have direct internet access for pulling images from GHCR and reaching AWS APIs. This avoids ~$28/month in VPC endpoint costs.
- **Path-based ALB routing**: `/api/*` routes to the API service, `/mcp/*` to the MCP service, and all other paths to the Web service. One ALB instead of three keeps costs down.
- **Managed RDS password**: Using `manage_master_user_password = true` lets AWS create and rotate the database password in Secrets Manager automatically.
- **Terraform workspaces** for dev/prod separation within a single state backend.

## Suspension Note

The AWS infrastructure was provisioned and validated but subsequently torn down due to ongoing cost (~$53/month for dev environment). The architectural decision remains sound — ECS Fargate is the right fit if the project needs production hosting. The Docker CI pipeline continues to build and push images to GHCR so the project is deployment-ready without maintaining active AWS resources.

## Consequences

- All infrastructure lives in one cloud provider with unified billing, IAM, and networking.
- Estimated dev environment cost: ~$53/month (3 Fargate tasks + ALB + RDS).
- The frontend does not benefit from Vercel's edge CDN or automatic preview deployments — acceptable for the current project scope. CloudFront can be added later if needed.
- GHCR credentials must be stored in AWS Secrets Manager for ECS to pull private images. Public repositories avoid this requirement entirely.
- ECS tasks in public subnets have public IP addresses, but security groups ensure they only accept traffic from the ALB. This is a standard pattern for cost-optimised Fargate deployments.
- Database schema must be applied manually after initial provisioning (`psql` via bastion or SSM Session Manager).
