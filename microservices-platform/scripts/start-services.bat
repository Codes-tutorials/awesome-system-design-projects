@echo off
echo Starting Microservices Platform...
echo =====================================

echo Starting infrastructure services...
docker-compose up -d

echo Waiting for services to be ready...
timeout /t 30 /nobreak >nul

echo Building all services...
call mvn clean install -DskipTests

echo Starting Service Registry...
start /b cmd /c "cd service-registry && mvn spring-boot:run"
timeout /t 20 /nobreak >nul

echo Starting API Gateway...
start /b cmd /c "cd api-gateway && mvn spring-boot:run"
timeout /t 15 /nobreak >nul

echo Starting User Service...
start /b cmd /c "cd user-service && mvn spring-boot:run"
timeout /t 15 /nobreak >nul

echo Starting Order Service...
start /b cmd /c "cd order-service && mvn spring-boot:run"
timeout /t 15 /nobreak >nul

echo.
echo ✅ All services started successfully!
echo.
echo 📊 Service URLs:
echo   • Service Registry: http://localhost:8761
echo   • API Gateway:      http://localhost:8080
echo   • User Service:     http://localhost:8081
echo   • Order Service:    http://localhost:8082
echo   • Kafka UI:         http://localhost:8090
echo   • Prometheus:       http://localhost:9090
echo   • Grafana:          http://localhost:3000
echo.
echo 🔧 Infrastructure:
echo   • MySQL (User):     localhost:3306
echo   • MySQL (Inventory): localhost:3307
echo   • PostgreSQL:       localhost:5432
echo   • MongoDB:          localhost:27017
echo   • Kafka:            localhost:9092
echo   • Redis:            localhost:6379
echo.
echo Press any key to exit...
pause >nul