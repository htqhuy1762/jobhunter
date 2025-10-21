@echo off
echo ========================================
echo QUICK START - JobHunter Microservices
echo ========================================
echo.

echo Step 1: Checking prerequisites...
echo.

where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker is not installed or not in PATH
    echo Please install Docker Desktop from https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

where docker-compose >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker Compose is not installed
    pause
    exit /b 1
)

echo [OK] Docker and Docker Compose are installed
echo.

echo Step 2: Creating .env file if not exists...
if not exist .env (
    copy .env.example .env
    echo [INFO] Created .env file. Please edit it with your email credentials.
    notepad .env
) else (
    echo [OK] .env file already exists
)
echo.

echo Step 3: Building all services...
echo This may take 10-15 minutes for the first time...
echo.
call build-all.bat

echo.
echo Step 4: Starting all services with Docker Compose...
echo.
docker-compose up -d

echo.
echo ========================================
echo Services are starting up!
echo ========================================
echo.
echo Please wait 2-3 minutes for all services to start and register with Eureka
echo.
echo Available URLs:
echo - Eureka Dashboard: http://localhost:8761
echo - API Gateway: http://localhost:8080
echo - Zipkin Tracing: http://localhost:9411
echo.
echo Check service status: docker-compose ps
echo Check logs: docker-compose logs -f [service-name]
echo.
pause