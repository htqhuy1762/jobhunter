# JobHunter Microservices Architecture

## Kiến trúc hệ thống

Hệ thống JobHunter đã được chuyển đổi sang kiến trúc microservices với các services sau:

### Infrastructure Services
- **Eureka Server** (Port 8761): Service Discovery & Registry
- **API Gateway** (Port 8080): Entry point, routing, load balancing, rate limiting
- **MySQL** (Port 3306): Database
- **Redis** (Port 6379): Caching & Rate Limiting
- **Zipkin** (Port 9411): Distributed Tracing

### Business Services
- **Auth Service** (Port 8081): Authentication & Authorization, User/Role/Permission Management
- **Company Service** (Port 8082): Company Management
- **Job Service** (Port 8083): Job & Skill Management
- **Resume Service** (Port 8084): Resume/CV Management
- **File Service** (Port 8085): File Upload/Download
- **Notification Service** (Port 8086): Email & Notification

## Cách chạy hệ thống

### 1. Chạy từng service riêng lẻ (Development)

```bash
# Chạy Eureka Server
cd eureka-server
gradlew bootRun

# Chạy Auth Service
cd auth-service
gradlew bootRun

# Chạy API Gateway
cd api-gateway
gradlew bootRun

# Tương tự cho các services khác...
```

### 2. Chạy toàn bộ hệ thống với Docker Compose

```bash
# Build tất cả services
cd jobhunter-microservices

# Build từng service
cd auth-service && gradlew clean build && cd ..
cd api-gateway && gradlew clean build && cd ..
cd company-service && gradlew clean build && cd ..
cd job-service && gradlew clean build && cd ..
cd resume-service && gradlew clean build && cd ..
cd file-service && gradlew clean build && cd ..
cd notification-service && gradlew clean build && cd ..
cd eureka-server && gradlew clean build && cd ..

# Chạy docker compose
docker-compose up -d
```

### 3. Kiểm tra hệ thống

- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Zipkin Tracing: http://localhost:9411

## API Endpoints (qua Gateway)

Tất cả requests đi qua API Gateway tại `http://localhost:8080`

### Authentication
- POST `/api/v1/auth/login` - Login
- POST `/api/v1/auth/register` - Register
- GET `/api/v1/auth/refresh` - Refresh token
- POST `/api/v1/auth/logout` - Logout

### Companies
- GET `/api/v1/companies` - List companies
- POST `/api/v1/companies` - Create company (Admin)
- GET `/api/v1/companies/{id}` - Get company detail
- PUT `/api/v1/companies` - Update company (Admin)
- DELETE `/api/v1/companies/{id}` - Delete company (Admin)

### Jobs & Skills
- GET `/api/v1/jobs` - List jobs (Public)
- POST `/api/v1/jobs` - Create job (Protected)
- GET `/api/v1/jobs/{id}` - Get job detail
- PUT `/api/v1/jobs` - Update job (Protected)
- DELETE `/api/v1/jobs/{id}` - Delete job (Protected)
- GET `/api/v1/skills` - List skills

### Resumes
- GET `/api/v1/resumes` - List resumes
- POST `/api/v1/resumes` - Create resume
- GET `/api/v1/resumes/{id}` - Get resume detail
- PUT `/api/v1/resumes` - Update resume
- DELETE `/api/v1/resumes/{id}` - Delete resume

### Files
- POST `/api/v1/files/upload` - Upload file
- GET `/api/v1/files/{filename}` - Download file

### Notifications
- POST `/api/v1/subscribers` - Subscribe (Public)
- GET `/api/v1/subscribers` - List subscribers (Protected)

## Tính năng của Gateway

- **Rate Limiting**: Giới hạn số request/second
- **Circuit Breaker**: Tự động fallback khi service down
- **Load Balancing**: Phân tải giữa các instance
- **CORS Configuration**: Hỗ trợ frontend
- **JWT Authentication**: Xác thực token
- **Distributed Tracing**: Theo dõi request qua các services

## Environment Variables

Tạo file `.env` trong thư mục `jobhunter-microservices`:

```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

## Monitoring & Observability

- **Health Checks**: Mỗi service có endpoint `/actuator/health`
- **Metrics**: Prometheus metrics tại `/actuator/prometheus`
- **Tracing**: Zipkin UI để xem distributed traces

## Kiến trúc Chi tiết

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       v
┌──────────────────┐
│   API Gateway    │ (8080)
│  Rate Limiting   │
│ Circuit Breaker  │
└──────┬───────────┘
       │
       v
┌──────────────────┐
│  Eureka Server   │ (8761)
│Service Discovery │
└──────┬───────────┘
       │
       ├─────> Auth Service (8081)
       ├─────> Company Service (8082)
       ├─────> Job Service (8083)
       ├─────> Resume Service (8084)
       ├─────> File Service (8085)
       └─────> Notification Service (8086)
              
       ┌─────────┐  ┌───────┐
       │  MySQL  │  │ Redis │
       └─────────┘  └───────┘
```

## Lưu ý

1. Đảm bảo MySQL đã chạy và có database `jobhunter`
2. Đảm bảo Redis đã được cài đặt và chạy
3. Các services cần đăng ký với Eureka trước khi Gateway có thể route request
4. Kiểm tra logs của từng service nếu có lỗi
version: '3.8'

services:
  # ========== INFRASTRUCTURE SERVICES ==========
  
  # MySQL Database
  mysql:
    image: mysql:8.0
    container_name: jobhunter-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: jobhunter
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - microservices-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis
  redis:
    image: redis:7-alpine
    container_name: jobhunter-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - microservices-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Zipkin for Distributed Tracing
  zipkin:
    image: openzipkin/zipkin:latest
    container_name: jobhunter-zipkin
    ports:
      - "9411:9411"
    networks:
      - microservices-network

  # ========== MICROSERVICES ==========
  
  # Eureka Server (Service Discovery)
  eureka-server:
    build:
      context: ./eureka-server
      dockerfile: Dockerfile
    container_name: eureka-server
    ports:
      - "8761:8761"
    networks:
      - microservices-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  # Auth Service
  auth-service:
    build:
      context: ./auth-service
      dockerfile: Dockerfile
    container_name: auth-service
    ports:
      - "8081:8081"
    depends_on:
      mysql:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
    networks:
      - microservices-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jobhunter?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans

  # API Gateway
  api-gateway:
    build:
      context: ./api-gateway
      dockerfile: Dockerfile
    container_name: api-gateway
    ports:
      - "8080:8080"
    depends_on:
      eureka-server:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - microservices-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_DATA_REDIS_HOST=redis
      - MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans

  # Company Service
  company-service:
    build:
      context: ./company-service
      dockerfile: Dockerfile
    container_name: company-service
    ports:
      - "8082:8082"
    depends_on:
      mysql:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
    networks:
      - microservices-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jobhunter?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans

  # Job Service
  job-service:
    build:
      context: ./job-service
      dockerfile: Dockerfile
    container_name: job-service
    ports:
      - "8083:8083"
    depends_on:
      mysql:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
    networks:
      - microservices-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jobhunter?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans

  # Resume Service
  resume-service:
    build:
      context: ./resume-service
      dockerfile: Dockerfile
    container_name: resume-service
    ports:
      - "8084:8084"
    depends_on:
      mysql:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
    networks:
      - microservices-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jobhunter?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans

  # File Service
  file-service:
    build:
      context: ./file-service
      dockerfile: Dockerfile
    container_name: file-service
    ports:
      - "8085:8085"
    depends_on:
      eureka-server:
        condition: service_healthy
    networks:
      - microservices-network
    volumes:
      - file_storage:/app/storage
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans

  # Notification Service
  notification-service:
    build:
      context: ./notification-service
      dockerfile: Dockerfile
    container_name: notification-service
    ports:
      - "8086:8086"
    depends_on:
      mysql:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
    networks:
      - microservices-network
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/jobhunter?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans
      - MAIL_USERNAME=${MAIL_USERNAME}
      - MAIL_PASSWORD=${MAIL_PASSWORD}

networks:
  microservices-network:
    driver: bridge

volumes:
  mysql_data:
  redis_data:
  file_storage:

