variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "eu-north-1"
}

variable "environment" {
  description = "Environment name (dev, prod)"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "norintegrate"
}

# ---------- VPC ----------

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

# ---------- RDS ----------

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_multi_az" {
  description = "Enable Multi-AZ for RDS"
  type        = bool
  default     = false
}

variable "db_allocated_storage" {
  description = "Allocated storage in GB for RDS"
  type        = number
  default     = 20
}

variable "db_max_allocated_storage" {
  description = "Maximum storage autoscaling limit in GB"
  type        = number
  default     = 50
}

variable "deletion_protection" {
  description = "Enable deletion protection for RDS"
  type        = bool
  default     = false
}

variable "skip_final_snapshot" {
  description = "Skip final snapshot when deleting RDS"
  type        = bool
  default     = true
}

# ---------- ECS ----------

variable "api_cpu" {
  description = "CPU units for API task (256 = 0.25 vCPU)"
  type        = number
  default     = 256
}

variable "api_memory" {
  description = "Memory in MB for API task"
  type        = number
  default     = 512
}

variable "mcp_cpu" {
  description = "CPU units for MCP task"
  type        = number
  default     = 256
}

variable "mcp_memory" {
  description = "Memory in MB for MCP task"
  type        = number
  default     = 512
}

variable "web_cpu" {
  description = "CPU units for Web task"
  type        = number
  default     = 256
}

variable "web_memory" {
  description = "Memory in MB for Web task"
  type        = number
  default     = 512
}

variable "api_desired_count" {
  description = "Desired number of API tasks"
  type        = number
  default     = 1
}

variable "mcp_desired_count" {
  description = "Desired number of MCP tasks"
  type        = number
  default     = 1
}

variable "web_desired_count" {
  description = "Desired number of Web tasks"
  type        = number
  default     = 1
}

variable "api_image_tag" {
  description = "Docker image tag for API"
  type        = string
  default     = "latest"
}

variable "mcp_image_tag" {
  description = "Docker image tag for MCP"
  type        = string
  default     = "latest"
}

variable "web_image_tag" {
  description = "Docker image tag for Web"
  type        = string
  default     = "latest"
}

variable "ghcr_owner" {
  description = "GHCR repository owner (GitHub username or org)"
  type        = string
  default     = "drwegie"
}

# ---------- Logging ----------

variable "log_retention_days" {
  description = "CloudWatch log retention in days"
  type        = number
  default     = 14
}
