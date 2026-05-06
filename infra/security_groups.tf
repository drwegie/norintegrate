resource "aws_security_group" "alb" {
  name_prefix = "${local.name_prefix}-alb-"
  description = "ALB - allow HTTP from internet"
  vpc_id      = aws_vpc.main.id

  tags = { Name = "${local.name_prefix}-alb-sg" }

  lifecycle { create_before_destroy = true }
}

resource "aws_vpc_security_group_ingress_rule" "alb_http" {
  security_group_id = aws_security_group.alb.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 80
  to_port           = 80
  ip_protocol       = "tcp"
}

resource "aws_vpc_security_group_egress_rule" "alb_to_ecs" {
  security_group_id            = aws_security_group.alb.id
  referenced_security_group_id = aws_security_group.ecs.id
  from_port                    = 3000
  to_port                      = 8081
  ip_protocol                  = "tcp"
}

# ---------- ECS ----------

resource "aws_security_group" "ecs" {
  name_prefix = "${local.name_prefix}-ecs-"
  description = "ECS tasks - allow traffic from ALB, outbound to RDS and internet"
  vpc_id      = aws_vpc.main.id

  tags = { Name = "${local.name_prefix}-ecs-sg" }

  lifecycle { create_before_destroy = true }
}

resource "aws_vpc_security_group_ingress_rule" "ecs_from_alb" {
  security_group_id            = aws_security_group.ecs.id
  referenced_security_group_id = aws_security_group.alb.id
  from_port                    = 3000
  to_port                      = 8081
  ip_protocol                  = "tcp"
}

resource "aws_vpc_security_group_egress_rule" "ecs_all_outbound" {
  security_group_id = aws_security_group.ecs.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

# ---------- RDS ----------

resource "aws_security_group" "rds" {
  name_prefix = "${local.name_prefix}-rds-"
  description = "RDS - allow PostgreSQL from ECS only"
  vpc_id      = aws_vpc.main.id

  tags = { Name = "${local.name_prefix}-rds-sg" }

  lifecycle { create_before_destroy = true }
}

resource "aws_vpc_security_group_ingress_rule" "rds_from_ecs" {
  security_group_id            = aws_security_group.rds.id
  referenced_security_group_id = aws_security_group.ecs.id
  from_port                    = 5432
  to_port                      = 5432
  ip_protocol                  = "tcp"
}
