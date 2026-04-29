data "aws_iam_policy_document" "ecs_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

# ---------- Task execution role (pulling images, reading secrets, pushing logs) ----------

resource "aws_iam_role" "ecs_task_execution" {
  name               = "${local.name_prefix}-ecs-execution"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume_role.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_managed" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "ecs_secrets_access" {
  statement {
    actions = ["secretsmanager:GetSecretValue"]

    resources = [
      aws_secretsmanager_secret.ghcr_credentials.arn,
      aws_secretsmanager_secret.jwt_issuer_uri.arn,
      aws_secretsmanager_secret.cors_origins.arn,
      aws_secretsmanager_secret.nextauth_secret.arn,
      aws_secretsmanager_secret.google_oauth.arn,
    ]
  }

  # Access to the RDS-managed master password
  statement {
    actions = ["secretsmanager:GetSecretValue"]

    resources = [
      aws_db_instance.main.master_user_secret[0].secret_arn,
    ]
  }
}

resource "aws_iam_role_policy" "ecs_secrets_access" {
  name   = "${local.name_prefix}-ecs-secrets"
  role   = aws_iam_role.ecs_task_execution.id
  policy = data.aws_iam_policy_document.ecs_secrets_access.json
}

# ---------- Task role (for application-level AWS SDK calls — empty for now) ----------

resource "aws_iam_role" "ecs_task" {
  name               = "${local.name_prefix}-ecs-task"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume_role.json
}
