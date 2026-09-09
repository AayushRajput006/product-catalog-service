variable "aws_region" {
  description = "AWS region for the project infrastructure"
  type        = string
  default     = "ap-south-1"
}

variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  default     = "product_catalog"
}

variable "db_username" {
  description = "PostgreSQL master username"
  type        = string
  default     = "productadmin"
}

variable "db_password" {
  description = "PostgreSQL master password"
  type        = string
  sensitive   = true
}