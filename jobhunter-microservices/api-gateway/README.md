- `POST /api/v1/subscribers` - Subscribe to newsletter

### Protected Endpoints (Authentication Required)
- `GET /api/v1/users/**` → Auth Service
- `POST /api/v1/jobs/**` → Job Service
- `PUT /api/v1/companies/**` → Company Service
- `POST /api/v1/resumes/**` → Resume Service
- `POST /api/v1/files/**` → File Service

## Rate Limiting

### Login Endpoint
- **Rate**: 5 requests/minute per IP
- **Burst**: 10 requests

### Register Endpoint
- **Rate**: 3 requests/minute per IP
- **Burst**: 5 requests

### Public Endpoints
- **Rate**: 100 requests/minute per IP
- **Burst**: 200 requests

## Circuit Breaker

### Configuration
- **Failure Threshold**: 50% (trong sliding window 10 requests)
- **Wait Duration**: 60 seconds
- **Half-Open Calls**: 3 requests

### Fallback
Khi service down, Gateway trả về response:
```json
{
  "error": "Service Unavailable",
  "message": "Service is currently unavailable. Please try again later.",
  "statusCode": 503,
  "timestamp": 1234567890
}
```

## Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Metrics (Prometheus)
```bash
curl http://localhost:8080/actuator/prometheus
```

### Gateway Routes
```bash
curl http://localhost:8080/actuator/gateway/routes
```

## Logging

Logs được lưu tại: `logs/gateway.log`

Format:
```
2025-01-20 10:30:45 - ==> Request: POST /api/v1/auth/login from /127.0.0.1:54321
2025-01-20 10:30:46 - JWT validated for user: user@example.com with roles: ROLE_USER
2025-01-20 10:30:46 - <== Response: POST /api/v1/auth/login - Status: 200 OK - Duration: 245ms
```

## Distributed Tracing

Gateway tự động tạo `trace-id` cho mỗi request và forward đến các services.

View traces tại: http://localhost:9411 (Zipkin UI)

## Testing

### Test Authentication
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# Access protected endpoint
curl http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Test Rate Limiting
```bash
# Send multiple requests quickly
for i in {1..20}; do
  curl http://localhost:8080/api/v1/auth/login
done
```

### Test Circuit Breaker
```bash
# Stop a service (e.g., company-service)
# Then call company endpoint
curl http://localhost:8080/api/v1/companies/1
# Should receive fallback response
```

## Troubleshooting

### Gateway không kết nối được Redis
```bash
# Check Redis connection
redis-cli ping
# Should return PONG

# Update Redis config in application.yml
spring.data.redis.host=your-redis-host
```

### JWT validation failed
- Kiểm tra `jwt.secret` phải giống với Auth Service
- Kiểm tra token chưa hết hạn
- Kiểm tra format: `Bearer <token>`

### Service not found (503)
- Kiểm tra service đã đăng ký với Eureka chưa
- Xem Eureka Dashboard: http://localhost:8761
- Kiểm tra `spring.application.name` của service

## Next Steps

1. Deploy Gateway lên Kubernetes
2. Add API documentation (Swagger/OpenAPI)
3. Implement request/response caching
4. Add request body validation
5. Implement API versioning strategy
# API Gateway Service

## Mô tả
API Gateway là điểm vào duy nhất cho tất cả các request từ client đến hệ thống microservices. Gateway chịu trách nhiệm:

- **Routing**: Chuyển request đến đúng service
- **Authentication**: Xác thực JWT token
- **Authorization**: Kiểm tra quyền truy cập
- **Rate Limiting**: Giới hạn số request/giây
- **Circuit Breaker**: Ngăn chặn cascade failure
- **Load Balancing**: Phân tải request
- **Logging**: Ghi log tất cả request/response

## Công nghệ sử dụng
- Spring Cloud Gateway (Reactive WebFlux)
- Netflix Eureka Client (Service Discovery)
- Resilience4j (Circuit Breaker)
- Redis (Rate Limiting)
- JWT (Authentication)
- Zipkin (Distributed Tracing)
- Prometheus (Metrics)

## Cấu hình

### Prerequisites
- Java 17+
- Redis Server (Port 6379)
- Eureka Server (Port 8761)

### Environment Variables
```bash
JWT_SECRET=your-secret-key-here
REDIS_HOST=localhost
REDIS_PORT=6379
EUREKA_SERVER=http://localhost:8761/eureka/
```

## Build & Run

### Development
```bash
./gradlew bootRun
```

### Build Docker Image
```bash
./gradlew bootJar
docker build -t jobhunter/api-gateway:latest .
```

### Run with Docker
```bash
docker run -p 8080:8080 \
  -e REDIS_HOST=redis \
  -e EUREKA_SERVER=http://eureka-server:8761/eureka/ \
  jobhunter/api-gateway:latest
```

## API Routes

### Public Endpoints (No Authentication)
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration
- `GET /api/v1/auth/refresh` - Refresh token
- `GET /api/v1/jobs/**` - View jobs (read-only)
- `GET /api/v1/companies/**` - View companies (read-only)
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

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2023.0.0"

dependencies {
    // Spring Cloud Gateway
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    
    // Service Discovery
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    
    // Config Client
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    
    // Circuit Breaker
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
    
    // Redis for Rate Limiting
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    
    // Security & JWT
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    
    // Distributed Tracing
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

