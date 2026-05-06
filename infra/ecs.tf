resource "aws_ecs_cluster" "main" {
  name = "${local.name_prefix}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

# ---------- API service ----------

resource "aws_ecs_task_definition" "api" {
  family                   = "${local.name_prefix}-api"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.api_cpu
  memory                   = var.api_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = "api"
      image     = "${local.ghcr_registry}/norintegrate-api:${var.api_image_tag}"
      essential = true

      portMappings = [
        { containerPort = 8080, protocol = "tcp" }
      ]

      environment = [
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${aws_db_instance.main.endpoint}/norintegrate" },
        { name = "CORS_ALLOWED_ORIGINS", value = "http://${aws_lb.main.dns_name}" },
      ]

      secrets = [
        { name = "DB_USERNAME", valueFrom = "${aws_db_instance.main.master_user_secret[0].secret_arn}:username::" },
        { name = "DB_PASSWORD", valueFrom = "${aws_db_instance.main.master_user_secret[0].secret_arn}:password::" },
        { name = "JWT_ISSUER_URI", valueFrom = aws_secretsmanager_secret.jwt_issuer_uri.arn },
      ]

      repositoryCredentials = {
        credentialsParameter = aws_secretsmanager_secret.ghcr_credentials.arn
      }

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.api.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "api" {
  name            = "${local.name_prefix}-api"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count   = var.api_desired_count
  launch_type     = "FARGATE"

  health_check_grace_period_seconds = 300

  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.api.arn
    container_name   = "api"
    container_port   = 8080
  }
}

# ---------- MCP service ----------

resource "aws_ecs_task_definition" "mcp" {
  family                   = "${local.name_prefix}-mcp"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.mcp_cpu
  memory                   = var.mcp_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = "mcp"
      image     = "${local.ghcr_registry}/norintegrate-mcp:${var.mcp_image_tag}"
      essential = true

      portMappings = [
        { containerPort = 8081, protocol = "tcp" }
      ]

      environment = [
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${aws_db_instance.main.endpoint}/norintegrate" },
      ]

      secrets = [
        { name = "DB_USERNAME", valueFrom = "${aws_db_instance.main.master_user_secret[0].secret_arn}:username::" },
        { name = "DB_PASSWORD", valueFrom = "${aws_db_instance.main.master_user_secret[0].secret_arn}:password::" },
      ]

      repositoryCredentials = {
        credentialsParameter = aws_secretsmanager_secret.ghcr_credentials.arn
      }

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.mcp.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "mcp" {
  name            = "${local.name_prefix}-mcp"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.mcp.arn
  desired_count   = var.mcp_desired_count
  launch_type     = "FARGATE"

  health_check_grace_period_seconds = 300

  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.mcp.arn
    container_name   = "mcp"
    container_port   = 8081
  }
}

# ---------- Web service ----------

resource "aws_ecs_task_definition" "web" {
  family                   = "${local.name_prefix}-web"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.web_cpu
  memory                   = var.web_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = "web"
      image     = "${local.ghcr_registry}/norintegrate-web:${var.web_image_tag}"
      essential = true

      portMappings = [
        { containerPort = 3000, protocol = "tcp" }
      ]

      environment = [
        { name = "NEXTAUTH_URL", value = "http://${aws_lb.main.dns_name}" },
        { name = "NEXT_PUBLIC_API_URL", value = "http://${aws_lb.main.dns_name}" },
      ]

      secrets = [
        { name = "NEXTAUTH_SECRET", valueFrom = aws_secretsmanager_secret.nextauth_secret.arn },
        { name = "GOOGLE_CLIENT_ID", valueFrom = "${aws_secretsmanager_secret.google_oauth.arn}:client_id::" },
        { name = "GOOGLE_CLIENT_SECRET", valueFrom = "${aws_secretsmanager_secret.google_oauth.arn}:client_secret::" },
      ]

      repositoryCredentials = {
        credentialsParameter = aws_secretsmanager_secret.ghcr_credentials.arn
      }

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.web.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "web" {
  name            = "${local.name_prefix}-web"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.web.arn
  desired_count   = var.web_desired_count
  launch_type     = "FARGATE"

  health_check_grace_period_seconds = 60

  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.web.arn
    container_name   = "web"
    container_port   = 3000
  }
}
