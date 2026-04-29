output "alb_dns_name" {
  description = "ALB DNS name — use this to access the application"
  value       = aws_lb.main.dns_name
}

output "rds_endpoint" {
  description = "RDS PostgreSQL endpoint (host:port)"
  value       = aws_db_instance.main.endpoint
}

output "rds_master_secret_arn" {
  description = "ARN of the RDS-managed master password secret"
  value       = aws_db_instance.main.master_user_secret[0].secret_arn
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = aws_ecs_cluster.main.name
}

output "api_service_name" {
  description = "ECS API service name (for aws ecs update-service)"
  value       = aws_ecs_service.api.name
}

output "mcp_service_name" {
  description = "ECS MCP service name"
  value       = aws_ecs_service.mcp.name
}

output "web_service_name" {
  description = "ECS Web service name"
  value       = aws_ecs_service.web.name
}
