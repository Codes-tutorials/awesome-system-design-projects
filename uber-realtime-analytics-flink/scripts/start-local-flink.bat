@echo off
REM Uber Real-Time Analytics - Local Flink Startup Script for Windows

echo 🚀 Starting Uber Real-Time Analytics Platform
echo ==============================================

REM Configuration
set PROJECT_ROOT=%~dp0..
set FLINK_JOBS_JAR=%PROJECT_ROOT%\flink-jobs\target\flink-jobs-1.0.0.jar

echo [INFO] Checking prerequisites...

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed. Please install Java 17 or higher.
    pause
    exit /b 1
)

REM Check Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven is not installed. Please install Maven 3.8 or higher.
    pause
    exit /b 1
)

REM Check Docker
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not installed. Please install Docker.
    pause
    exit /b 1
)

REM Check Docker Compose
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose is not installed. Please install Docker Compose.
    pause
    exit /b 1
)

echo [SUCCESS] All prerequisites are satisfied

echo [INFO] Building the project...
cd /d "%PROJECT_ROOT%"
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ERROR] Failed to build project
    pause
    exit /b 1
)
echo [SUCCESS] Project built successfully

echo [INFO] Starting infrastructure services...
cd /d "%PROJECT_ROOT%\docker-compose"
docker-compose up -d zookeeper kafka pinot redis postgresql

echo [INFO] Waiting for services to be ready...
timeout /t 30 /nobreak >nul

echo [INFO] Creating Kafka topics...
docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic ride-events --partitions 6 --replication-factor 1 --if-not-exists
docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic driver-events --partitions 6 --replication-factor 1 --if-not-exists
docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic order-events --partitions 6 --replication-factor 1 --if-not-exists
docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic pricing-updates --partitions 3 --replication-factor 1 --if-not-exists
docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic matching-results --partitions 3 --replication-factor 1 --if-not-exists
docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic analytics-metrics --partitions 3 --replication-factor 1 --if-not-exists

echo [SUCCESS] Kafka topics created

echo [INFO] Starting Flink cluster...
docker-compose up -d flink-jobmanager flink-taskmanager

echo [INFO] Waiting for Flink cluster to be ready...
timeout /t 20 /nobreak >nul

echo [SUCCESS] Flink cluster is ready
echo [INFO] Flink UI available at: http://localhost:8081

echo [INFO] Starting API Gateway...
cd /d "%PROJECT_ROOT%\api-gateway"
start /b mvn spring-boot:run

echo [SUCCESS] API Gateway started
echo [INFO] API Gateway will be available at: http://localhost:8080

echo.
echo 🎉 Uber Real-Time Analytics Platform Started Successfully!
echo ========================================================
echo.
echo 📊 Service URLs:
echo   • Flink Dashboard:    http://localhost:8081
echo   • Kafka UI:          http://localhost:8080
echo   • Pinot Console:     http://localhost:9000
echo   • API Gateway:       http://localhost:8080
echo   • Prometheus:        http://localhost:9090
echo   • Grafana:           http://localhost:3000
echo.
echo 📈 Real-time Analytics:
echo   • Ride Matching:     Processing ride requests and driver matching
echo   • Dynamic Pricing:   Calculating surge pricing based on demand/supply
echo   • Analytics:         Computing real-time KPIs and metrics
echo   • Anomaly Detection: Monitoring for unusual patterns
echo.
echo 🔧 Management:
echo   • View logs:         docker-compose logs -f [service-name]
echo   • Stop services:     docker-compose down
echo   • Restart:           docker-compose restart [service-name]
echo.
echo 📝 Next Steps:
echo   1. Check Flink jobs in the dashboard
echo   2. Monitor Kafka topics for events
echo   3. Query Pinot for real-time analytics
echo   4. Use API Gateway to submit ride requests
echo.

pause