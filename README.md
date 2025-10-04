# 🚀 JobHunter - Backend API

Hệ thống backend cho nền tảng tuyển dụng việc làm, kết nối ứng viên và nhà tuyển dụng.

---

## 📖 Giới Thiệu

**JobHunter** là RESTful API được xây dựng bằng Spring Boot, cung cấp các tính năng quản lý tuyển dụng:

- Quản lý người dùng, công ty, tin tuyển dụng, hồ sơ ứng tuyển
- Xác thực với JWT (Access Token + Refresh Token)
- Phân quyền theo Role và Permission
- Lưu trữ token và cache với Redis

---

## 🛠️ Công Nghệ

- **Java 21** + **Spring Boot 3.2.4**
- **Spring Security** + **JWT**
- **MySQL 8.0** - Database chính
- **Redis** - Cache và quản lý token
- **Docker** - Container hóa
- **Gradle** - Build tool
- **Swagger UI** - API documentation

---

## ✨ Tính Năng

### Authentication
- Đăng ký, đăng nhập, đăng xuất
- JWT với Access Token (30 phút) và Refresh Token (7 ngày)
- Refresh Token lưu trong Redis
- Access Token Blacklist khi logout

### Authorization
- Role-Based Access Control (RBAC)
- Permission gắn với từng API endpoint
- Các role: Super Admin, Admin, Business Employee, User

### Quản Lý
- **Users**: CRUD, phân quyền, quản lý profile
- **Companies**: CRUD, upload logo, tìm kiếm, phân trang
- **Jobs**: CRUD, tìm kiếm theo skill/location/salary
- **Resumes**: Apply job, theo dõi trạng thái hồ sơ

---

## 📦 Yêu Cầu

- Java JDK 21+
- MySQL 8.0+
- Redis 6.x+
- Docker (optional)

---

## 🚀 Cài Đặt

### 1. Clone project
```bash
git clone <repo-url>
cd 01-java-spring-jobhunter-starter
```

### 2. Chạy MySQL và Redis bằng Docker
```bash
docker-compose up -d
```

### 3. Cấu hình `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jobhunter
spring.datasource.username=root
spring.datasource.password=your_password

spring.data.redis.host=localhost
spring.data.redis.port=6379

hoidanit.jwt.base64-secret=your-secret-key
hoidanit.jwt.access-token-validity-in-seconds=1800
hoidanit.jwt.refresh-token-validity-in-seconds=604800
```

### 4. Chạy ứng dụng
```bash
./gradlew bootRun
```

### 5. Truy cập
- **API**: `http://localhost:8080/api/v1`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

---

## 📁 Cấu Trúc

```
src/main/java/vn/hoidanit/jobhunter/
├── config/          # Cấu hình Spring Security, Redis, CORS
├── controller/      # REST API endpoints
├── service/         # Business logic
├── repository/      # JPA repositories
├── domain/          # Entity classes và DTOs
└── util/            # Utilities và exception handlers
```

---
## 📖 API Endpoints

### Auth
```
POST   /api/v1/auth/register     # Đăng ký
POST   /api/v1/auth/login        # Đăng nhập
POST   /api/v1/auth/logout       # Đăng xuất
GET    /api/v1/auth/account      # Thông tin user
GET    /api/v1/auth/refresh      # Refresh token
```

### Users
```
GET    /api/v1/users              # Danh sách users
GET    /api/v1/users/{id}         # Chi tiết user
POST   /api/v1/users              # Tạo user
PUT    /api/v1/users              # Cập nhật
DELETE /api/v1/users/{id}         # Xóa user
```

### Companies, Jobs, Resumes
Tương tự pattern CRUD như Users