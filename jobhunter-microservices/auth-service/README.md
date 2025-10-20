# AUTH-SERVICE

Authentication & Authorization Service cho hệ thống JobHunter Microservices.

## Chức năng chính

### 1. Authentication
- ✅ Login (POST /api/v1/auth/login)
- ✅ Register (POST /api/v1/auth/register)
- ✅ Refresh Token (GET /api/v1/auth/refresh)
- ✅ Logout (POST /api/v1/auth/logout)
- ✅ Get Account (GET /api/v1/auth/account)

### 2. User Management
- ✅ Create User (POST /api/v1/users)
- ✅ Update User (PUT /api/v1/users)
- ✅ Delete User (DELETE /api/v1/users/{id})
- ✅ Get User by ID (GET /api/v1/users/{id})
- ✅ Get All Users with Pagination (GET /api/v1/users)

### 3. Role Management
- ✅ Create Role (POST /api/v1/roles)
- ✅ Update Role (PUT /api/v1/roles)
- ✅ Delete Role (DELETE /api/v1/roles/{id})
- ✅ Get All Roles (GET /api/v1/roles)

### 4. Permission Management
- ✅ Create Permission (POST /api/v1/permissions)
- ✅ Update Permission (PUT /api/v1/permissions)
- ✅ Delete Permission (DELETE /api/v1/permissions/{id})
- ✅ Get All Permissions (GET /api/v1/permissions)

## Công nghệ sử dụng

- **Spring Boot 3.2.0**
- **Spring Security + JWT**
- **Spring Cloud Eureka Client**
- **MySQL** - Database
- **Redis** - Lưu trữ Refresh Token
- **Spring Data JPA**
- **Lombok**
- **Zipkin** - Distributed Tracing

## Cấu hình

### Database
Tạo database MySQL:
```sql
CREATE DATABASE jobhunter_auth;
```

Cập nhật trong `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jobhunter_auth
    username: root
    password: 123456
```

### Redis
Redis phải chạy trên `localhost:6379`

```bash
# Nếu dùng Docker
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

### Eureka Server
Eureka Server phải chạy trên `http://localhost:8761`

## Build & Run

### Build
```bash
cd D:\Jobhunter\jobhunter-microservices\auth-service
.\gradlew.bat clean bootJar
```

### Run
```bash
java -jar build\libs\auth-service-0.0.1-SNAPSHOT.jar
```

Hoặc:
```bash
.\gradlew.bat bootRun
```

## QUAN TRỌNG: Xử lý Refresh Token

Trong microservice này, **Refresh Token được xử lý như sau**:

### Flow Login:
1. User gửi username + password
2. Auth-Service validate → Tạo Access Token + Refresh Token
3. **Lưu Refresh Token vào Redis** với key: `refresh_token:{email}`
4. Trả về Access Token (response body) + Refresh Token (cookie)

### Flow Refresh Token:
1. Client gửi Refresh Token (từ cookie)
2. Auth-Service:
   - Validate Refresh Token (JWT signature + expiration)
   - **Kiểm tra Refresh Token có tồn tại trong Redis không**
   - Nếu hợp lệ:
     - **XÓA Refresh Token cũ khỏi Redis** ← QUAN TRỌNG!
     - Tạo Access Token mới + Refresh Token mới
     - **Lưu Refresh Token mới vào Redis**
     - Trả về AT mới + RT mới
   - Nếu không hợp lệ: 401 Unauthorized

### Flow Logout:
1. Client gửi request logout
2. Auth-Service **XÓA Refresh Token khỏi Redis**
3. Clear cookie
4. User phải login lại

### Tại sao phải xóa RT cũ?

✅ **Bảo mật**: Chỉ RT mới nhất hợp lệ, RT cũ bị vô hiệu hóa ngay lập tức
✅ **Ngăn chặn tấn công**: Nếu RT bị đánh cắp, khi user refresh, RT bị cắp sẽ không dùng được
✅ **One Token Per User**: Mỗi user chỉ có 1 RT active duy nhất

## API Endpoints

### Public (Không cần authentication)

#### 1. Login
```bash
POST http://localhost:8081/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin@gmail.com",
  "password": "123456"
}
```

Response:
```json
{
  "access_token": "eyJhbGci...",
  "user": {
    "id": 1,
    "email": "admin@gmail.com",
    "name": "Admin",
    "role": {
      "id": 1,
      "name": "SUPER_ADMIN",
      "permissions": [...]
    }
  }
}
```

#### 2. Register
```bash
POST http://localhost:8081/api/v1/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "123456",
  "age": 25,
  "gender": "MALE",
  "address": "Ha Noi"
}
```

#### 3. Refresh Token
```bash
GET http://localhost:8081/api/v1/auth/refresh
Cookie: refresh_token=xxx
```

### Protected (Cần authentication)

#### 4. Get Account
```bash
GET http://localhost:8081/api/v1/auth/account
Authorization: Bearer YOUR_ACCESS_TOKEN
```

#### 5. Logout
```bash
POST http://localhost:8081/api/v1/auth/logout
Authorization: Bearer YOUR_ACCESS_TOKEN
Cookie: refresh_token=xxx
```

#### 6. Get All Users
```bash
GET http://localhost:8081/api/v1/users?page=0&size=10
Authorization: Bearer YOUR_ACCESS_TOKEN
```

#### 7. Create User
```bash
POST http://localhost:8081/api/v1/users
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "name": "New User",
  "email": "newuser@example.com",
  "password": "123456",
  "age": 30,
  "gender": "FEMALE",
  "role": {
    "id": 2
  }
}
```

## Kiểm tra Service

### 1. Health Check
```bash
curl http://localhost:8081/actuator/health
```

### 2. Kiểm tra đã đăng ký với Eureka
```bash
curl http://localhost:8761/eureka/apps/AUTH-SERVICE
```

### 3. Kiểm tra Redis
```bash
# Login trước
POST http://localhost:8081/api/v1/auth/login

# Kiểm tra Redis
redis-cli
> KEYS refresh_token:*
> GET refresh_token:admin@gmail.com
```

## Monitoring

### Prometheus Metrics
```bash
curl http://localhost:8081/actuator/prometheus
```

### Distributed Tracing
Mỗi request sẽ có trace ID, xem tại Zipkin: http://localhost:9411

## Lỗi thường gặp

### 1. Cannot connect to MySQL
```
Error: Access denied for user 'root'@'localhost'
```
Giải quyết: Kiểm tra MySQL đang chạy và password đúng

### 2. Cannot connect to Redis
```
Error: Unable to connect to Redis
```
Giải quyết:
```bash
docker ps | grep redis
# Nếu không chạy:
docker start redis
```

### 3. Eureka connection refused
```
Error: Connection refused: localhost/127.0.0.1:8761
```
Giải quyết: Chạy Eureka Server trước khi chạy Auth-Service

### 4. Refresh token invalid
```
Error: Refresh token is invalid
```
Giải quyết: 
- RT đã hết hạn → Login lại
- RT cũ đã bị xóa → Login lại
- Redis bị clear → Login lại

## Testing

### Test Login Flow
```bash
# 1. Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@gmail.com","password":"123456"}' \
  -c cookies.txt

# 2. Access protected endpoint
curl http://localhost:8081/api/v1/auth/account \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 3. Refresh token
curl http://localhost:8081/api/v1/auth/refresh \
  -b cookies.txt

# 4. Logout
curl -X POST http://localhost:8081/api/v1/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -b cookies.txt
```

## Next Steps

1. ✅ Auth-Service đã hoàn thành
2. ⏳ Cần tạo database `jobhunter_auth` và chạy service
3. ⏳ Test integration với API Gateway
4. ⏳ Migrate dữ liệu từ monolith sang microservice
5. ⏳ Tạo Company-Service tiếp theo

## Port

- Auth-Service: **8081**
- API Gateway: **8080**
- Eureka Server: **8761**
- MySQL: **3306**
- Redis: **6379**
- Zipkin: **9411**

