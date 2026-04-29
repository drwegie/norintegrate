# NorIntegrate Infrastructure

Terraform configuration for deploying NorIntegrate to AWS. All services (API, MCP, Web) run on ECS Fargate behind a single ALB. PostgreSQL runs on RDS. Container images are stored in GHCR.

## Architecture

```
Internet → ALB (port 80) → ECS Fargate (public subnets)
                              ├── norintegrate-web  (3000)
                              ├── norintegrate-api  (8080) ─┐
                              └── norintegrate-mcp  (8081) ─┤
                                                            ↓
                                                   RDS PostgreSQL 18
                                                   (private subnets)
```

## Prerequisites

- [Terraform](https://developer.hashicorp.com/terraform/install) >= 1.5
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html) configured with credentials
- Container images pushed to GHCR (triggered by merging to `main`)

## Setup

### 1. Bootstrap the remote state backend (one-time)

```bash
cd bootstrap
terraform init
terraform apply
cd ..
```

This creates an S3 bucket and DynamoDB table for Terraform state storage and locking.

### 2. Initialize the main infrastructure

```bash
terraform init
```

### 3. Create a workspace

```bash
terraform workspace new dev    # or: terraform workspace new prod
```

### 4. Review and apply

```bash
# Review the plan
terraform plan -var-file=terraform.tfvars

# Apply (manual — not automated in CI, per ADR-007)
terraform apply -var-file=terraform.tfvars

# For production:
terraform workspace select prod
terraform plan -var-file=prod.tfvars
terraform apply -var-file=prod.tfvars
```

### 5. Post-apply manual steps

After the first `terraform apply`, populate these secrets in AWS Secrets Manager:

| Secret | Value |
|--------|-------|
| `norintegrate-dev/ghcr-credentials` | `{"username":"<github-username>","password":"<github-pat>"}` |
| `norintegrate-dev/jwt-issuer-uri` | `https://accounts.google.com` |
| `norintegrate-dev/cors-allowed-origins` | `http://<alb-dns-name>` |
| `norintegrate-dev/nextauth-secret` | Output of `openssl rand -base64 32` |
| `norintegrate-dev/google-oauth` | `{"client_id":"<id>","client_secret":"<secret>"}` |

The RDS master password is auto-managed by AWS — no manual setup needed.

### 6. Initialize the database

Connect to RDS and apply the schema:

```bash
# Get the RDS endpoint
terraform output rds_endpoint

# Get the master password from Secrets Manager
aws secretsmanager get-secret-value \
  --secret-id $(terraform output -raw rds_master_secret_arn) \
  --query SecretString --output text

# Apply schema (via a bastion, SSM Session Manager, or temporarily allowing your IP)
psql -h <rds-endpoint> -U norintegrate -d norintegrate -f ../docs/schema.sql
psql -h <rds-endpoint> -U norintegrate -d norintegrate -f ../docs/seed.sql
```

## Cost Estimate (dev)

| Resource | Monthly |
|----------|---------|
| ECS Fargate (3 tasks, 0.25 vCPU / 0.5 GB) | ~$23 |
| ALB | ~$16 |
| RDS db.t4g.micro (single-AZ, 20 GB gp3) | ~$13 |
| S3 + DynamoDB (state) | <$1 |
| **Total** | **~$53/month** |

## Design Decisions

- **No NAT Gateway**: ECS tasks run in public subnets with `assign_public_ip = true`. Security groups restrict inbound to ALB-only. Saves ~$30/month.
- **No VPC endpoints**: Public subnet ECS has direct internet access. Saves ~$28/month.
- **GHCR over ECR**: Free for public repos, native `GITHUB_TOKEN` auth in GitHub Actions.
- **Managed RDS password**: AWS auto-creates and rotates the master password in Secrets Manager.
- **Single ALB with path-based routing**: `/api/*` → API, `/mcp/*` → MCP, default → Web.

See [ADR-007](../docs/adr/ADR-007-terraform-infrastructure-strategy.md) and [ADR-016](../docs/adr/ADR-016-aws-ecs-fargate-deployment.md) for full rationale.
