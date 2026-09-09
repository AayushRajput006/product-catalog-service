resource "aws_db_subnet_group" "main" {
  name = "product-catalog-db-subnet-group"

  subnet_ids = [
    aws_subnet.private_1.id,
    aws_subnet.private_2.id
  ]

  tags = {
    Name    = "product-catalog-db-subnet-group"
    Project = "product-catalog-service"
  }
}

resource "aws_db_instance" "postgres" {
  identifier = "product-catalog-postgres"

  engine         = "postgres"
  engine_version = "17"

  instance_class    = "db.t4g.micro"
  allocated_storage = 20
  storage_type      = "gp3"
  storage_encrypted = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.db.id]

  publicly_accessible = false
  multi_az            = false

  backup_retention_period = 1

  skip_final_snapshot = true
  deletion_protection = false

  tags = {
    Name    = "product-catalog-postgres"
    Project = "product-catalog-service"
  }
}