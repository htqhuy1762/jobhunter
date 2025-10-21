@echo off
echo ========================================
echo Building JobHunter Microservices
echo ========================================

cd eureka-server
echo Building Eureka Server...
call gradlew clean build -x test
cd ..

cd auth-service
echo Building Auth Service...
call gradlew clean build -x test
cd ..

cd api-gateway
echo Building API Gateway...
call gradlew clean build -x test
cd ..

cd company-service
echo Building Company Service...
call gradlew clean build -x test
cd ..

cd job-service
echo Building Job Service...
call gradlew clean build -x test
cd ..

cd resume-service
echo Building Resume Service...
call gradlew clean build -x test
cd ..

cd file-service
echo Building File Service...
call gradlew clean build -x test
cd ..

cd notification-service
echo Building Notification Service...
call gradlew clean build -x test
cd ..

echo ========================================
echo Build Complete!
echo ========================================
echo You can now run: docker-compose up -d
pause