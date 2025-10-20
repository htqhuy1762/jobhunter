# JOBHUNTER MICROSERVICES

Dự án JobHunter được chuyển đổi sang kiến trúc Microservices với Spring Cloud.

## Kiến trúc tổng quan

```
Client → API Gateway (8080) → Microservices
                ↓
          Eureka Server (8761)
```

## Các Services

### Infrastructure Services
- **Eureka Server** (8761) - Service Discovery & Registry
- **API Gateway** (8080) - Entry point cho tất cả requests
- **MySQL** (3306) - Database cho các services
- **Redis** (6379) - Cache & Rate Limiting
- **MinIO** (9000) - Object Storage
- **Kafka** (9092) - Message Queue
- **Zipkin** (9411) - Distributed Tracing
- **Prometheus** (9090) - Metrics Collection
- **Grafana** (3001) - Monitoring Dashboard

### Business Services
- **Auth Service** (8081) - Authentication, Users, Roles, Permissions
- **Company Service** (8082) - Company Management
- **Job Service** (8083) - Job & Skill Management
- **Resume Service** (8084) - Resume/CV Management
- **File Service** (8085) - File Upload/Download
- **Notification Service** (8086) - Email & Notifications

## Yêu cầu hệ thống

- Docker & Docker Compose
- Java 17+
- Gradle 8.0+
- 8GB RAM (recommended)

## Cài đặt và chạy

### 1. Clone repository
```bash
cd D:\Jobhunter\microservices
```

### 2. Build tất cả services
```bash
# Build Eureka Server
cd eureka-server
./gradlew clean bootJar

# Build API Gateway
cd ../api-gateway
./gradlew clean bootJar

# Build các business services (khi đã tạo)
# cd ../auth-service
# ./gradlew clean bootJar
# ...
```

### 3. Chạy với Docker Compose
```bash
cd D:\Jobhunter\microservices
docker-compose up -d
```

### 4. Kiểm tra services

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Zipkin**: http://localhost:9411
- **Grafana**: http://localhost:3001 (admin/admin)
- **Prometheus**: http://localhost:9090
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)

## API Endpoints

### Authentication
```bash
# Login
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json
{
  "username": "admin@gmail.com",
  "password": "123456"
}

# Register
POST http://localhost:8080/api/v1/auth/register

# Refresh Token
GET http://localhost:8080/api/v1/auth/refresh
Cookie: refresh_token=xxx
```

### Jobs (Public)
```bash
# Get all jobs
GET http://localhost:8080/api/v1/jobs

# Get job by ID
GET http://localhost:8080/api/v1/jobs/1
```

### Companies (Public)
```bash
# Get all companies
GET http://localhost:8080/api/v1/companies
```

### Protected Endpoints (Require JWT Token)
```bash
# Create job
POST http://localhost:8080/api/v1/jobs
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
{
  "name": "Java Developer",
  "company": {"id": 1}
}
```

## Monitoring

### View Logs
```bash
# View all services logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f api-gateway
docker-compose logs -f auth-service
```

### Health Checks
```bash
# Gateway health
curl http://localhost:8080/actuator/health

# Eureka health
curl http://localhost:8761/actuator/health

# View registered services
curl http://localhost:8761/eureka/apps
```

### Metrics
```bash
# Gateway metrics
curl http://localhost:8080/actuator/prometheus

# View in Grafana
# 1. Go to http://localhost:3001
# 2. Login: admin/admin
# 3. Add Prometheus datasource: http://prometheus:9090
# 4. Import dashboard ID: 11378 (Spring Boot Dashboard)
```

## Distributed Tracing

Mỗi request qua Gateway sẽ có `trace-id` duy nhất:

```bash
# Make a request
curl http://localhost:8080/api/v1/jobs

# View trace in Zipkin
# Go to http://localhost:9411
# Search by trace ID or service name
```

## Scaling Services

```bash
# Scale job-service to 3 instances
docker-compose up -d --scale job-service=3

# Gateway sẽ tự động load balance giữa 3 instances
```

## Troubleshooting

### Service không start
```bash
# Check logs
docker-compose logs service-name

# Check if port is already in use
netstat -ano | findstr :8080

# Restart service
docker-compose restart service-name
```

### Eureka không hiển thị services
```bash
# Wait 30-60s for services to register
# Check Eureka logs
docker-compose logs eureka-server

# Verify network
docker network inspect microservices_jobhunter-network
```

### Database connection error
```bash
# Verify MySQL is running
docker-compose ps mysql

# Check MySQL logs
docker-compose logs mysql

# Connect to MySQL
docker exec -it jobhunter-mysql mysql -uroot -proot
```

## Development Workflow

### 1. Phát triển service mới
```bash
# Create new service folder
mkdir my-service
cd my-service

# Copy template từ auth-service
# Modify package name, application.yml, etc.

# Build
./gradlew bootJar

# Add to docker-compose.yml
```

### 2. Test local (không dùng Docker)
```bash
# Start infrastructure only
docker-compose up -d mysql redis kafka eureka-server

# Run service locally
./gradlew bootRun

# Service sẽ register với Eureka
```

### 3. Hot reload
```bash
# Enable Spring DevTools
# Add to build.gradle.kts:
# implementation("org.springframework.boot:spring-boot-devtools")

# Run with bootRun
./gradlew bootRun
```

## Production Deployment

### Kubernetes
```bash
# Build images
docker-compose build

# Tag images
docker tag jobhunter/api-gateway:latest registry.example.com/api-gateway:v1.0.0

# Push to registry
docker push registry.example.com/api-gateway:v1.0.0

# Apply Kubernetes manifests
kubectl apply -f k8s/
```

### Environment Variables
```bash
# Production settings
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=super-secret-key-change-this-in-production
MYSQL_ROOT_PASSWORD=strong-password
REDIS_PASSWORD=redis-password
```

## Lộ trình Migration

- [x] Phase 1: Setup Infrastructure (Eureka, Gateway, Docker)
- [ ] Phase 2: Migrate Auth Service
- [ ] Phase 3: Migrate Company Service
- [ ] Phase 4: Migrate Job Service
- [ ] Phase 5: Migrate Resume Service
- [ ] Phase 6: Migrate File Service
- [ ] Phase 7: Migrate Notification Service
- [ ] Phase 8: Production Ready (Security, Performance Testing)

## Tài liệu chi tiết

Xem file `MICROSERVICE_ARCHITECTURE.md` để biết thêm chi tiết về kiến trúc và quyết định thiết kế.

## License

MIT
plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "vn.hoidanit"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2023.0.0"

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

