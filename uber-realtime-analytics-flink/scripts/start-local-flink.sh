#!/bin/bash

# Uber Real-Time Analytics - Local Flink Startup Script

set -e

echo "🚀 Starting Uber Real-Time Analytics Platform"
echo "=============================================="

# Configuration
FLINK_VERSION="1.18.0"
FLINK_HOME="/opt/flink"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FLINK_JOBS_JAR="$PROJECT_ROOT/flink-jobs/target/flink-jobs-1.0.0.jar"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17 or higher."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven 3.8 or higher."
        exit 1
    fi
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker."
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose."
        exit 1
    fi
    
    print_success "All prerequisites are satisfied"
}

# Build the project
build_project() {
    print_status "Building the project..."
    
    cd "$PROJECT_ROOT"
    
    # Clean and build
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_success "Project built successfully"
    else
        print_error "Failed to build project"
        exit 1
    fi
}

# Start infrastructure services
start_infrastructure() {
    print_status "Starting infrastructure services..."
    
    cd "$PROJECT_ROOT/docker-compose"
    
    # Start services
    docker-compose up -d zookeeper kafka pinot redis postgresql
    
    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    sleep 30
    
    # Check if Kafka is ready
    print_status "Checking Kafka connectivity..."
    docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        print_success "Kafka is ready"
    else
        print_warning "Kafka might not be fully ready yet"
    fi
    
    print_success "Infrastructure services started"
}

# Create Kafka topics
create_kafka_topics() {
    print_status "Creating Kafka topics..."
    
    cd "$PROJECT_ROOT/docker-compose"
    
    # Create topics
    docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic ride-events --partitions 6 --replication-factor 1 --if-not-exists
    docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic driver-events --partitions 6 --replication-factor 1 --if-not-exists
    docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic order-events --partitions 6 --replication-factor 1 --if-not-exists
    docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic pricing-updates --partitions 3 --replication-factor 1 --if-not-exists
    docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic matching-results --partitions 3 --replication-factor 1 --if-not-exists
    docker-compose exec kafka kafka-topics --create --bootstrap-server localhost:9092 --topic analytics-metrics --partitions 3 --replication-factor 1 --if-not-exists
    
    print_success "Kafka topics created"
}

# Start Flink cluster
start_flink_cluster() {
    print_status "Starting Flink cluster..."
    
    cd "$PROJECT_ROOT/docker-compose"
    
    # Start Flink services
    docker-compose up -d flink-jobmanager flink-taskmanager
    
    # Wait for Flink to be ready
    print_status "Waiting for Flink cluster to be ready..."
    sleep 20
    
    # Check Flink UI
    if curl -s http://localhost:8081 > /dev/null; then
        print_success "Flink cluster is ready"
        print_status "Flink UI available at: http://localhost:8081"
    else
        print_warning "Flink UI might not be ready yet"
    fi
}

# Submit Flink jobs
submit_flink_jobs() {
    print_status "Submitting Flink jobs..."
    
    if [ ! -f "$FLINK_JOBS_JAR" ]; then
        print_error "Flink jobs JAR not found: $FLINK_JOBS_JAR"
        print_status "Please build the project first: mvn clean package"
        exit 1
    fi
    
    cd "$PROJECT_ROOT/docker-compose"
    
    # Submit jobs
    print_status "Submitting Real-Time Matching Job..."
    docker-compose exec flink-jobmanager flink run -c com.uber.analytics.flink.RealTimeAnalyticsJob /opt/flink/jobs/flink-jobs-1.0.0.jar matching
    
    sleep 5
    
    print_status "Submitting Dynamic Pricing Job..."
    docker-compose exec flink-jobmanager flink run -c com.uber.analytics.flink.RealTimeAnalyticsJob /opt/flink/jobs/flink-jobs-1.0.0.jar pricing
    
    sleep 5
    
    print_status "Submitting Analytics Aggregation Job..."
    docker-compose exec flink-jobmanager flink run -c com.uber.analytics.flink.RealTimeAnalyticsJob /opt/flink/jobs/flink-jobs-1.0.0.jar analytics
    
    sleep 5
    
    print_status "Submitting Anomaly Detection Job..."
    docker-compose exec flink-jobmanager flink run -c com.uber.analytics.flink.RealTimeAnalyticsJob /opt/flink/jobs/flink-jobs-1.0.0.jar anomaly
    
    print_success "All Flink jobs submitted"
}

# Start API Gateway
start_api_gateway() {
    print_status "Starting API Gateway..."
    
    cd "$PROJECT_ROOT/api-gateway"
    
    # Start in background
    nohup mvn spring-boot:run > api-gateway.log 2>&1 &
    API_GATEWAY_PID=$!
    
    echo $API_GATEWAY_PID > api-gateway.pid
    
    print_success "API Gateway started (PID: $API_GATEWAY_PID)"
    print_status "API Gateway will be available at: http://localhost:8080"
}

# Display service URLs
display_service_urls() {
    echo ""
    echo "🎉 Uber Real-Time Analytics Platform Started Successfully!"
    echo "========================================================"
    echo ""
    echo "📊 Service URLs:"
    echo "  • Flink Dashboard:    http://localhost:8081"
    echo "  • Kafka UI:          http://localhost:8080"
    echo "  • Pinot Console:     http://localhost:9000"
    echo "  • API Gateway:       http://localhost:8080"
    echo "  • Prometheus:        http://localhost:9090"
    echo "  • Grafana:           http://localhost:3000"
    echo ""
    echo "📈 Real-time Analytics:"
    echo "  • Ride Matching:     Processing ride requests and driver matching"
    echo "  • Dynamic Pricing:   Calculating surge pricing based on demand/supply"
    echo "  • Analytics:         Computing real-time KPIs and metrics"
    echo "  • Anomaly Detection: Monitoring for unusual patterns"
    echo ""
    echo "🔧 Management:"
    echo "  • View logs:         docker-compose logs -f [service-name]"
    echo "  • Stop services:     ./scripts/stop-local-flink.sh"
    echo "  • Restart:           docker-compose restart [service-name]"
    echo ""
    echo "📝 Next Steps:"
    echo "  1. Check Flink jobs in the dashboard"
    echo "  2. Monitor Kafka topics for events"
    echo "  3. Query Pinot for real-time analytics"
    echo "  4. Use API Gateway to submit ride requests"
    echo ""
}

# Main execution
main() {
    echo "Starting Uber Real-Time Analytics Platform..."
    
    check_prerequisites
    build_project
    start_infrastructure
    create_kafka_topics
    start_flink_cluster
    submit_flink_jobs
    start_api_gateway
    
    # Wait a bit for everything to settle
    sleep 10
    
    display_service_urls
}

# Handle script interruption
cleanup() {
    print_warning "Script interrupted. Cleaning up..."
    # Kill API Gateway if running
    if [ -f "$PROJECT_ROOT/api-gateway/api-gateway.pid" ]; then
        kill $(cat "$PROJECT_ROOT/api-gateway/api-gateway.pid") 2>/dev/null || true
        rm -f "$PROJECT_ROOT/api-gateway/api-gateway.pid"
    fi
    exit 1
}

trap cleanup INT TERM

# Run main function
main "$@"