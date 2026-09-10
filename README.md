# Product Catalog Service

A cloud-deployed **e-commerce Product Catalog microservice** built with Spring Boot and PostgreSQL, with an end-to-end DevOps workflow covering automated testing, containerization, security scanning, Infrastructure as Code, AWS EKS deployment, Helm release management, and Prometheus/Grafana observability.

> **Project status:** Complete for the capstone scope. HPA, multi-node high availability, persistent observability storage, and heavyweight centralized logging/alerting are documented as production enhancements rather than enabled in the resource-conscious demo environment.

## Architecture

```text
Developer
   |
   v
GitHub
   |
   v
GitHub Actions
   |
   +--> Maven Build & Tests
   |
   +--> Docker Image
   |
   +--> Trivy Security Scan
   |
   +--> GHCR (commit-SHA image)
   |
   +--> GitHub OIDC --> AWS IAM
                         |
                         v
                      AWS EKS
                         |
                         v
                       Helm
                         |
                         v
              Product Catalog Service
                         |
                         v
                Private PostgreSQL RDS

Spring Boot /actuator/prometheus
             |
             v
         Prometheus
             |
             v
           Grafana
```

Terraform provisions the AWS networking, security, RDS, EKS, worker-node, and IAM/OIDC infrastructure. Kubernetes provides scheduling, health probes, self-healing, and LoadBalancer exposure. Helm manages application releases.

## Features

- Product CRUD operations
- Search/filter by category, brand, and price range
- Pagination
- Request validation
- Duplicate-SKU handling
- Global exception handling
- PostgreSQL persistence
- Spring Boot Actuator health/readiness/liveness
- Prometheus application/JVM/database-pool metrics
- Automated unit and application-context tests
- Docker and Docker Compose
- Immutable commit-SHA container images
- Trivy HIGH/CRITICAL vulnerability gate
- Terraform-managed AWS infrastructure
- Kubernetes deployment on Amazon EKS
- Helm-based application release management
- GitHub Actions CI/CD with AWS OIDC
- Prometheus + Grafana monitoring

## Technology Stack

| Area | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1.1 |
| REST | Spring Web MVC / Tomcat 11 |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Testing | JUnit 5, Mockito, Spring Boot Test, H2 test profile |
| Build | Maven |
| Containerization | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Registry | GitHub Container Registry (GHCR) |
| Security scanning | Trivy |
| Infrastructure as Code | Terraform |
| Cloud | AWS |
| Database hosting | Amazon RDS for PostgreSQL |
| Orchestration | Amazon EKS / Kubernetes |
| Release management | Helm |
| Metrics | Spring Boot Actuator + Micrometer |
| Monitoring | Prometheus + Grafana |

## REST API

Base path:

```text
/api/v1/products
```

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/v1/products` | Create a product |
| `GET` | `/api/v1/products/{id}` | Get product by ID |
| `GET` | `/api/v1/products` | List/search products |
| `PUT` | `/api/v1/products/{id}` | Update a product |
| `DELETE` | `/api/v1/products/{id}` | Delete a product |
| `GET` | `/actuator/health` | Application health |
| `GET` | `/actuator/prometheus` | Prometheus metrics |

Search parameters include `category`, `brand`, `minPrice`, `maxPrice`, and Spring pagination parameters such as `page` and `size`.

## Repository Structure

```text
product-catalog-service/
├── .github/
│   └── workflows/
│       └── ci.yml
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/productcatalogservice/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   └── test/
├── terraform/
├── k8s/
├── product-catalog-chart/
│   ├── templates/
│   ├── Chart.yaml
│   └── values.yaml
├── monitoring/
│   ├── prometheus-values.yaml
│   ├── grafana-values.yaml
│   └── product-catalog-dashboard.json
├── Dockerfile
├── docker-compose.yaml
└── pom.xml
```

Sensitive Kubernetes Secret manifests and Terraform state files must **not** be committed.

## Run Locally

### Prerequisites

- Java 25
- Maven or Maven Wrapper
- PostgreSQL
- Docker Desktop (for container-based execution)

Configure the database through environment variables where required:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Do not commit real credentials.

### Maven

```bash
./mvnw clean test
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

The API is available locally on port `8080`.

### Docker Compose

```bash
docker compose up --build
```

Verify:

```text
http://localhost:8080/actuator/health
http://localhost:8080/api/v1/products
```

Stop the environment with:

```bash
docker compose down
```

## Automated Testing

The project contains service-layer tests and a Spring application-context test.

The CI environment uses an isolated **H2 test profile**, preventing the GitHub-hosted runner from depending on a locally running PostgreSQL server.

Run:

```bash
./mvnw clean test
```

## CI/CD Pipeline

The GitHub Actions workflow runs for pushes and pull requests targeting `main` or `master`.

### Pull requests

```text
Checkout
   ↓
Java 25 setup
   ↓
Maven build/test
   ↓
Docker build
   ↓
Trivy security gate
```

Deployment is not performed for pull requests.

### Push delivery flow

```text
Source Push
   ↓
Build & Test
   ↓
Docker Build
   ↓
Trivy Scan
   ↓
GHCR Push (commit SHA)
   ↓
GitHub OIDC
   ↓
AWS Authentication
   ↓
EKS kubeconfig
   ↓
Helm Upgrade / Install
   ↓
Kubernetes Rollout
```

Container images use the Git commit SHA as the deployment tag, providing traceability from source revision to the running workload.

AWS authentication uses **GitHub OpenID Connect (OIDC)** and short-lived role credentials instead of storing long-lived AWS access keys in GitHub.

The deployment stage follows the equivalent of:

```bash
helm upgrade --install product-catalog ./product-catalog-chart \
  --set image.tag=<GIT_SHA> \
  --force-conflicts \
  --wait \
  --timeout 5m
```

## Container Security

Trivy scans the application image before delivery.

The pipeline treats **HIGH** and **CRITICAL** vulnerabilities as a failing security gate. During development, vulnerabilities from embedded/runtime dependencies were fixed instead of disabling the scan.

The Docker image uses a multi-stage build so Maven/build tooling is not required in the final runtime stage.

## AWS Infrastructure with Terraform

Terraform provisions the project infrastructure in AWS, including:

- VPC
- Public and private subnets
- Internet Gateway and route tables
- Security groups
- Private Amazon RDS PostgreSQL instance
- Amazon EKS cluster
- Managed worker node group
- IAM roles/policies
- GitHub OIDC integration

The learning environment intentionally avoids a NAT Gateway to reduce cost.

### Typical Terraform workflow

```bash
cd terraform
terraform fmt
terraform init
terraform validate
terraform plan
terraform apply
```

Always review the plan before applying it. Unexpected replacement of persistent or expensive infrastructure such as EKS/RDS should be investigated rather than accepted automatically.

## Kubernetes and Helm

The application runs on Amazon EKS and is exposed through a Kubernetes `LoadBalancer` Service.

The Deployment includes:

- CPU/memory requests and limits
- Readiness probe
- Liveness probe
- Environment-aware database configuration
- Kubernetes Secret references
- Resource-aware rolling-update configuration

Useful commands:

```bash
kubectl get nodes
kubectl get pods -A
kubectl get deployment product-catalog-service
kubectl get service product-catalog-service

helm status product-catalog
helm history product-catalog
```

The final verified Helm release reached **revision 9 — deployed**.

### Resource-aware rollout

The demo cluster uses a single small worker node. After Prometheus and Grafana were added, a normal surge rollout could temporarily require more requested memory than the node could schedule.

The final application strategy is therefore:

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 0
    maxUnavailable: 1
```

This is an intentional trade-off for the single-node learning environment: the old application pod can be terminated before the replacement is scheduled, at the cost of possible brief deployment downtime.

## Monitoring

### Spring Boot metrics

Micrometer exposes Prometheus-compatible metrics through:

```text
/actuator/prometheus
```

### Prometheus

Prometheus is installed with Helm in the `monitoring` namespace and scrapes the Product Catalog Service.

A useful verification query is:

```promql
up{job="product-catalog-service"}
```

### Grafana

Grafana connects to the in-cluster Prometheus service.

The exported dashboard is version-controlled at:

```text
monitoring/product-catalog-dashboard.json
```

The **Product Catalog Service Monitoring** dashboard contains six primary panels:

| Panel | PromQL |
|---|---|
| Application Status | `up{job="product-catalog-service"}` |
| CPU Usage | `process_cpu_usage{job="product-catalog-service"} * 100` |
| JVM Heap Memory | `jvm_memory_used_bytes{job="product-catalog-service",area="heap"}` |
| HTTP Request Rate | `rate(http_server_requests_seconds_count{job="product-catalog-service"}[1m])` |
| Active DB Connections | `hikaricp_connections_active{job="product-catalog-service"}` |
| JVM Live Threads | `jvm_threads_live_threads{job="product-catalog-service"}` |

Prometheus and Grafana persistence are intentionally disabled for the lightweight demo environment. Their configuration/dashboard definitions remain version-controlled.

## Security Practices

- RDS is private rather than publicly accessible.
- PostgreSQL access is restricted through AWS security groups.
- Runtime database credentials are supplied through Kubernetes Secrets.
- Secrets and Terraform state are excluded from Git.
- Container images are scanned with Trivy.
- HIGH/CRITICAL findings fail the delivery gate.
- GitHub Actions uses OIDC instead of permanent AWS credentials.
- GHCR images use immutable commit-SHA tags.
- Kubernetes uses health probes and resource constraints.
- Terraform plans are reviewed before infrastructure changes.
- Public documentation does not contain passwords, tokens, or AWS account identifiers.

## Engineering Challenges and Solutions

| Challenge | Resolution |
|---|---|
| GitHub runner could not connect to local PostgreSQL | Added an H2 test profile for CI |
| Docker container could not reach host DB through `localhost` | Used container-aware networking / Compose service discovery |
| EKS application timed out connecting to private RDS | Corrected PostgreSQL security-group access from the EKS security boundary |
| GitHub OIDC role assumption was denied | Corrected the IAM/OIDC trust configuration |
| Terraform proposed an unexpected EKS replacement | Rejected the destructive plan and corrected the configuration before applying |
| Helm conflicted with fields previously managed by `kubectl` | Standardized deployment ownership on Helm and resolved server-side apply conflicts |
| Surge pod remained Pending after monitoring installation | Used `maxSurge: 0`, `maxUnavailable: 1` for the single-node environment |
| Monitoring increased cluster memory pressure | Kept Prometheus/Grafana lightweight and avoided unnecessary components |

## Resource and Cost Decisions

This project intentionally prioritizes demonstrating the DevOps lifecycle without maintaining an unnecessarily large AWS environment.

The final demo uses:

- One `t3.small` EKS worker
- One Product Catalog application replica
- Private RDS
- No NAT Gateway
- Lightweight Prometheus
- Lightweight Grafana
- No persistent Prometheus/Grafana storage
- Kubernetes-native application logs

The final measured allocation was approximately:

| Resource | Requests | Limits |
|---|---:|---:|
| CPU | 750m (38%) | 1250m (64%) |
| Memory | 1036 Mi (72%) | 2004 Mi (139%) |

Kubernetes scheduling is based primarily on requests; limits may be overcommitted.

### Why no HPA?

HPA was tested as a design consideration but intentionally not retained. Autoscaling application replicas on a single resource-constrained worker would not provide meaningful high availability and could create unschedulable pods.

### Why no Loki/ELK or Alertmanager?

A heavyweight logging/alerting stack was not necessary to demonstrate the core observability objective and would increase memory/cost pressure. Kubernetes-native logs and events are used for troubleshooting, while Prometheus and Grafana demonstrate metrics collection and visualization.

These are **scope and infrastructure decisions**, not accidental omissions.

## Final Implementation Status

| Component | Status |
|---|---|
| Spring Boot REST microservice | ✅ Complete |
| PostgreSQL persistence | ✅ Complete |
| Validation / exception handling | ✅ Complete |
| Automated tests | ✅ Complete |
| Docker | ✅ Complete |
| Docker Compose | ✅ Complete |
| GitHub Actions CI | ✅ Complete |
| GHCR publication | ✅ Complete |
| Trivy security scanning | ✅ Complete |
| Terraform AWS infrastructure | ✅ Complete |
| Private RDS | ✅ Complete |
| Amazon EKS | ✅ Complete |
| Kubernetes Deployment / Service | ✅ Complete |
| Readiness / liveness probes | ✅ Complete |
| GitHub OIDC | ✅ Complete |
| Automated CD | ✅ Complete |
| Helm release management | ✅ Complete |
| Prometheus | ✅ Complete |
| Grafana dashboard | ✅ Complete |
| HPA | ➖ Intentionally omitted for demo environment |
| Centralized Loki/ELK logging | ➖ Optional production enhancement |
| Multi-node HA | ➖ Optional production enhancement |

## Screenshots / Demo Evidence

For academic submission or portfolio presentation, useful evidence includes:

1. Spring Boot application and REST API responses
2. Automated Maven test success
3. Docker/Compose application + PostgreSQL
4. GitHub Actions successful workflow
5. GHCR commit-SHA image
6. Trivy scan
7. Terraform plan/apply
8. AWS VPC, RDS and EKS resources
9. EKS node and application pod Running
10. Kubernetes LoadBalancer + external health/API response
11. Helm status/history
12. Prometheus Product Catalog target/query
13. Grafana Prometheus data-source verification
14. Six-panel Product Catalog monitoring dashboard

> Avoid screenshots that expose passwords, tokens, Secret values, AWS account IDs, or other credentials.

## Production Enhancements

If this project were expanded beyond the capstone environment, possible improvements include:

- Multi-node EKS worker capacity
- Horizontal Pod Autoscaler
- Multi-AZ/high-availability database strategy
- NAT/VPC endpoint architecture as required by the workload
- Persistent Prometheus/Grafana storage
- Alertmanager or Grafana alert delivery
- Centralized logging with Loki or an appropriate cloud logging service
- Testcontainers integration testing
- Load/chaos testing
- Helm rollback drills
- Remote Terraform state and environment separation
- SBOM and additional software-supply-chain controls
- OpenAPI/Swagger documentation

## Key Learning Outcomes

This project demonstrates practical experience with:

- Building and testing a Java microservice
- Container and database networking
- CI/CD pipeline design
- Immutable container delivery
- Vulnerability remediation
- Infrastructure as Code
- AWS networking and private database connectivity
- Kubernetes scheduling and health management
- Helm release ownership and upgrades
- GitHub-to-AWS workload identity federation
- Metrics instrumentation and visualization
- Debugging real deployment, networking, IAM, scheduling, and resource-capacity problems
- Making cost-aware engineering trade-offs

---

**Author:** Aayush Raj

**Project:** Building a Scalable E-commerce Product Catalog Microservice
