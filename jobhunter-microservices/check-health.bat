@echo off
echo Checking service health...
echo.

echo Eureka Server:
curl -s http://localhost:8761/actuator/health
echo.
echo.

echo API Gateway:
curl -s http://localhost:8080/actuator/health
echo.
echo.

echo Auth Service:
curl -s http://localhost:8081/actuator/health
echo.
echo.

echo Company Service:
curl -s http://localhost:8082/actuator/health
echo.
echo.

echo Job Service:
curl -s http://localhost:8083/actuator/health
echo.
echo.

echo Resume Service:
curl -s http://localhost:8084/actuator/health
echo.
echo.

echo File Service:
curl -s http://localhost:8085/actuator/health
echo.
echo.

echo Notification Service:
curl -s http://localhost:8086/actuator/health
echo.
echo.

pause

