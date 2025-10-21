@echo off
echo Starting JobHunter Microservices with Docker Compose...
docker-compose up -d
echo.
echo Services are starting up...
echo.
echo Eureka Server: http://localhost:8761
echo API Gateway: http://localhost:8080
echo Zipkin Tracing: http://localhost:9411
echo.
echo Wait 2-3 minutes for all services to register with Eureka
pause

