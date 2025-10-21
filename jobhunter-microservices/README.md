# 🚀 JobHunter Microservices Architecture

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0-blue)](https://spring.io/projects/spring-cloud)
[![Docker](https://img.shields.io/badge/Docker-ready-blue)](https://www.docker.com/)

## 📖 Tổng Quan

Hệ thống JobHunter đã được **chuyển đổi hoàn toàn** từ kiến trúc Monolith sang Microservices với đầy đủ các tính năng enterprise-grade.

### ✨ Tính Năng Nổi Bật

✅ **API Gateway** với Rate Limiting, Circuit Breaker, JWT Authentication  
✅ **Service Discovery** tự động với Netflix Eureka  
✅ **Message Queue** với RabbitMQ cho async communication  
✅ **Distributed Tracing** với Zipkin  
✅ **Resilience Pattern** - Circuit Breaker, Retry, Fallback  
✅ **Docker Support** đầy đủ với Docker Compose  
✅ **Health Checks** và Monitoring với Actuator  
✅ **Centralized Configuration** với Spring Cloud Config  

---

## 🏗️ Kiến Trúc Hệ Thống

### Infrastructure Services
- **Eureka Server** (Port 8761): Service Discovery & Registry
- **API Gateway** (Port 8080): Entry point, routing, load balancing, rate limiting
- **MySQL** (Port 3306): Database
- **Redis** (Port 6379): Caching & Rate Limiting
- **RabbitMQ** (Port 5672/15672): Message Broker for async communication
- **Zipkin** (Port 9411): Distributed Tracing

### Business Services
- **Auth Service** (Port 8081): Authentication & Authorization, User/Role/Permission Management
- **Company Service** (Port 8082): Company Management
- **Job Service** (Port 8083): Job & Skill Management
- **Resume Service** (Port 8084): Resume/CV Management
- **File Service** (Port 8085): File Upload/Download
- **Notification Service** (Port 8086): Email & Notification

---

## 🚀 Quick Start

### Bước 1: Chuẩn bị môi trường

```bash
# Copy file cấu hình môi trường
cp .env.example .env

# Chỉnh sửa .env với thông tin email của bạn
# MAIL_USERNAME=your-email@gmail.com
# MAIL_PASSWORD=your-app-password
```

### Bước 2: Build tất cả services

```bash
# Windows
build-all.bat

# Linux/Mac
chmod +x build-all.sh
./build-all.sh
```

### Bước 3: Khởi động hệ thống

```bash
# Khởi động tất cả services
docker-compose up -d

# Kiểm tra trạng thái
docker-compose ps

# Xem logs
docker-compose logs -f
```

### Bước 4: Dừng hệ thống

```bash
# Dừng tất cả services
docker-compose down

# Dừng và xóa volumes
docker-compose down -v
```

---

## 💻 Development Mode

Chạy từng service riêng lẻ để phát triển:

```bash
# 1. Khởi động infrastructure
docker-compose up -d mysql redis rabbitmq zipkin

# 2. Chạy Eureka Server
cd eureka-server && gradlew bootRun

# 3. Chạy API Gateway
cd api-gateway && gradlew bootRun

# 4. Chạy các business services
cd auth-service && gradlew bootRun
cd company-service && gradlew bootRun
# ... và các services khác
```

---

## 🔍 Monitoring & Management

### Dashboards & UIs

| Service | URL | Mô tả |
|---------|-----|-------|
| **Eureka Dashboard** | http://localhost:8761 | Xem tất cả services đang chạy |
| **API Gateway** | http://localhost:8080 | Entry point cho tất cả API |
| **Zipkin Tracing** | http://localhost:9411 | Distributed tracing & performance monitoring |
| **RabbitMQ Management** | http://localhost:15672 | Message queue dashboard (admin/admin123) |

### Health Checks

```bash
# Kiểm tra health của Gateway
curl http://localhost:8080/actuator/health

# Xem tất cả routes
curl http://localhost:8080/actuator/gateway/routes

# Xem metrics
curl http://localhost:8080/actuator/prometheus
```

---

## 📡 API Endpoints

Tất cả requests đi qua API Gateway tại `http://localhost:8080`

### 🔐 Authentication APIs
```http
POST   /api/v1/auth/register          # Đăng ký tài khoản
POST   /api/v1/auth/login             # Đăng nhập
GET    /api/v1/auth/refresh           # Refresh token
POST   /api/v1/auth/logout            # Đăng xuất
GET    /api/v1/auth/account           # Thông tin tài khoản
```

### 👥 User Management APIs
```http
GET    /api/v1/users                  # Danh sách users (Admin)
POST   /api/v1/users                  # Tạo user (Admin)
GET    /api/v1/users/{id}             # Chi tiết user
PUT    /api/v1/users                  # Cập nhật user
DELETE /api/v1/users/{id}             # Xóa user (Admin)
```

### 🏢 Company APIs
```http
GET    /api/v1/companies              # Danh sách công ty
POST   /api/v1/companies              # Tạo công ty (HR)
GET    /api/v1/companies/{id}         # Chi tiết công ty
PUT    /api/v1/companies              # Cập nhật công ty (HR)
DELETE /api/v1/companies/{id}         # Xóa công ty (Admin)
```

### 💼 Job APIs
```http
GET    /api/v1/jobs                   # Danh sách việc làm (Public)
POST   /api/v1/jobs                   # Đăng tin tuyển dụng (HR)
GET    /api/v1/jobs/{id}              # Chi tiết công việc
PUT    /api/v1/jobs                   # Cập nhật công việc (HR)
DELETE /api/v1/jobs/{id}              # Xóa công việc (HR)
GET    /api/v1/skills                 # Danh sách kỹ năng
```

### 📄 Resume APIs
```http
GET    /api/v1/resumes                # Danh sách CV của user
POST   /api/v1/resumes                # Nộp hồ sơ ứng tuyển
GET    /api/v1/resumes/{id}           # Chi tiết hồ sơ
PUT    /api/v1/resumes                # Cập nhật hồ sơ
DELETE /api/v1/resumes/{id}           # Xóa hồ sơ
```

### 📁 File APIs
```http
POST   /api/v1/files/upload           # Upload file
GET    /api/v1/storage/{filename}     # Download file
```

### 📧 Notification APIs
```http
POST   /api/v1/subscribers            # Đăng ký nhận thông báo (Public)
GET    /api/v1/subscribers            # Danh sách subscribers (Admin)
POST   /api/v1/emails/send            # Gửi email (Admin)
```

---

## 🎯 Các Cải Tiến Đã Hoàn Thành

### ✅ Infrastructure
- [x] API Gateway với Rate Limiting, Circuit Breaker
- [x] Service Discovery với Netflix Eureka
- [x] Distributed Tracing với Zipkin
- [x] Message Queue với RabbitMQ
- [x] Redis cho caching và rate limiting
- [x] MySQL database

### ✅ Resilience Patterns
- [x] Circuit Breaker cho tất cả services
- [x] Fallback Controllers trong API Gateway
- [x] Retry mechanism với Resilience4j
- [x] Rate Limiting per endpoint
- [x] Health checks tự động

### ✅ Async Communication
- [x] RabbitMQ configuration
- [x] Email Queue với Producer/Consumer
- [x] Message retry mechanism
- [x] Dead Letter Queue support

### ✅ Docker & Deployment
- [x] Dockerfile cho từng service
- [x] Docker Compose orchestration
- [x] Application profiles (local, docker)
- [x] Build scripts tự động
- [x] Environment variables support

### ✅ Monitoring & Observability
- [x] Spring Boot Actuator endpoints
- [x] Prometheus metrics
- [x] Distributed tracing với Zipkin
- [x] Centralized logging
- [x] Health indicators

---

## 📚 Tài Liệu

- 📖 **[DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)** - Hướng dẫn triển khai chi tiết
- 🔧 **[build-all.bat](./build-all.bat)** - Script build cho Windows
- 🔧 **[build-all.sh](./build-all.sh)** - Script build cho Linux/Mac
- 🏥 **[health-check.bat](./health-check.bat)** - Health check script cho Windows
- 🏥 **[health-check.sh](./health-check.sh)** - Health check script cho Linux/Mac

---

## 🛠️ Troubleshooting

### Service không kết nối được Eureka?
```bash
# Kiểm tra Eureka logs
docker-compose logs eureka-server

# Restart Eureka
docker-compose restart eureka-server
```

### Gateway trả về 503 Service Unavailable?
```bash
# Circuit Breaker có thể đang OPEN, chờ 60s hoặc restart
docker-compose restart api-gateway
```

### Email không gửi được?
```bash
# Kiểm tra RabbitMQ
docker-compose logs rabbitmq

# Kiểm tra Notification Service
docker-compose logs notification-service
```

### Database connection failed?
```bash
# Kiểm tra MySQL
docker-compose exec mysql mysql -uroot -proot -e "SHOW DATABASES;"

# Restart MySQL
docker-compose restart mysql
```

---

## 🤝 Contributing

Mọi đóng góp đều được chào đón! Vui lòng:
1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

---

## 📄 License

MIT License

---

**Developed with ❤️ by JobHunter Team**


### Notifications
- POST `/api/v1/subscribers` - Đăng ký nhận thông báo (Public)
- GET `/api/v1/subscribers` - Danh sách subscribers (Admin)
- POST `/api/v1/emails/send` - Gửi email (Admin)

---

## 🎯 Các Cải Tiến Đã Hoàn Thành

### ✅ Infrastructure
- [x] API Gateway với Rate Limiting, Circuit Breaker
- [x] Service Discovery với Netflix Eureka
- [x] Distributed Tracing với Zipkin
- [x] Message Queue với RabbitMQ
- [x] Redis cho caching và rate limiting
- [x] MySQL database

### ✅ Resilience Patterns
- [x] Circuit Breaker cho tất cả services
- [x] Fallback Controllers trong API Gateway
- [x] Retry mechanism với Resilience4j
- [x] Rate Limiting per endpoint
- [x] Health checks tự động

### ✅ Async Communication
- [x] RabbitMQ configuration
- [x] Email Queue với Producer/Consumer
- [x] Message retry mechanism
- [x] Dead Letter Queue support

### ✅ Docker & Deployment
- [x] Dockerfile cho từng service
- [x] Docker Compose orchestration
- [x] Application profiles (local, docker)
- [x] Build scripts tự động
- [x] Environment variables support

### ✅ Monitoring & Observability
- [x] Spring Boot Actuator endpoints
- [x] Prometheus metrics
- [x] Distributed tracing với Zipkin
- [x] Centralized logging
- [x] Health indicators

---

## 📚 Tài Liệu

- 📖 **[DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)** - Hướng dẫn triển khai chi tiết
- 🔧 **[build-all.bat](./build-all.bat)** - Script build cho Windows
- 🔧 **[build-all.sh](./build-all.sh)** - Script build cho Linux/Mac

---

## 🛠️ Troubleshooting

### Service không kết nối được Eureka?
```bash
# Kiểm tra Eureka logs
docker-compose logs eureka-server

# Restart Eureka
docker-compose restart eureka-server
```

### Gateway trả về 503 Service Unavailable?
```bash
# Circuit Breaker có thể đang OPEN, chờ 60s hoặc restart
docker-compose restart api-gateway
```

### Email không gửi được?
```bash
# Kiểm tra RabbitMQ
docker-compose logs rabbitmq

# Kiểm tra Notification Service
docker-compose logs notification-service
```

---

## 🤝 Contributing

Mọi đóng góp đều được chào đón! Vui lòng:
1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request
# 3. Chạy API Gateway
cd api-gateway && gradlew bootRun

# 4. Chạy các business services
cd auth-service && gradlew bootRun
cd company-service && gradlew bootRun
# ... và các services khác
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