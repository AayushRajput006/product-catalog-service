# Security group for the application / EKS nodes
resource "aws_security_group" "app" {
  name        = "product-catalog-app-sg"
  description = "Security group for Product Catalog application"
  vpc_id      = aws_vpc.main.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "product-catalog-app-sg"
    Project = "product-catalog-service"
  }
}

# Security group for PostgreSQL / RDS
resource "aws_security_group" "db" {
  name        = "product-catalog-db-sg"
  description = "Security group for Product Catalog PostgreSQL database"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "Allow PostgreSQL from EKS worker nodes"

    from_port = 5432
    to_port   = 5432
    protocol  = "tcp"

    security_groups = [
      aws_eks_cluster.main.vpc_config[0].cluster_security_group_id
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "product-catalog-db-sg"
    Project = "product-catalog-service"
  }
}