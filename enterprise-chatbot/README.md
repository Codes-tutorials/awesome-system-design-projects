# Enterprise Chatbot - AI-Powered Q&A System

A production-ready enterprise chatbot built with Spring Boot and Spring AI that provides intelligent document-based question answering capabilities.

## 🚀 Features

### Core Functionality
- **Document Processing**: Upload and process PDF, Word, and text documents
- **AI-Powered Chat**: Intelligent responses using OpenAI GPT models
- **Vector Search**: Semantic search through document content using embeddings
- **Real-time Chat**: WebSocket support for instant messaging
- **Session Management**: Persistent chat sessions with history
- **Document Management**: Track upload status and processing progress

### Technical Features
- **Spring AI Integration**: Latest Spring AI framework with OpenAI
- **Vector Database**: PostgreSQL with pgvector for embeddings
- **Caching**: Redis for session and response caching
- **Async Processing**: Non-blocking document processing
- **REST API**: Comprehensive RESTful endpoints
- **Web UI**: Modern, responsive chat interface
- **Production Ready**: Comprehensive configuration and monitoring

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.2.1, Spring AI 0.8.1
- **Database**: PostgreSQL with pgvector extension
- **Cache**: Redis
- **AI**: OpenAI GPT-4 and text-embedding-ada-002
- **Frontend**: Vanilla JavaScript with modern UI
- **Build**: Maven

### Key Components
1. **Document Processing Service**: Handles file uploads and text extraction
2. **Chat Service**: Manages conversations and AI interactions
3. **Vector Store**: Stores and searches document embeddings
4. **WebSocket Support**: Real-time chat capabilities
5. **REST Controllers**: API endpoints for all functionality

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ with pgvector extension
- Redis 6.0+
- OpenAI API key

## 🛠️ Installation & Setup

### 1. Database Setup

**PostgreSQL with pgvector:**
```sql
-- Create database
CREATE DATABASE chatbot_db;

-- Create user
CREATE USER chatbot_user WITH PASSWORD 'chatbot_password';
GRANT ALL PRIVILEGES ON DATABASE chatbot_db TO chatbot_user;

-- Connect to chatbot_db and enable pgvector
\c chatbot_db
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Redis Setup
```bash
# Install and start Redis
sudo apt-get install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

### 3. Environment Configuration

Create environment variables or update `application.yml`:
```bash
export OPENAI_API_KEY=your-openai-api-key-here
export DATABASE_URL=jdbc:postgresql://localhost:5432/chatbot_db
export DATABASE_USERNAME=chatbot_user
export DATABASE_PASSWORD=chatbot_password
```

### 4. Build and Run

```bash
# Clone the repository
git clone <repository-url>
cd enterprise-chatbot

# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run

# Or run with production profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🌐 Usage

### Web Interface
1. Open your browser to `http://localhost:8080`
2. Upload documents using the sidebar
3. Start chatting once documents are processed
4. Ask questions about your uploaded content

### API Endpoints

**Chat Operations:**
```bash
# Create new session
POST /api/chat/session?userId=user123

# Send message
POST /api/chat/message
{
  "sessionId": "session-id",
  "message": "What is the main topic of the uploaded document?"
}

# Get chat history
GET /api/chat/history/{sessionId}
```

**Document Operations:**
```bash
# Upload document
POST /api/documents/upload
Content-Type: multipart/form-data
file: [your-document.pdf]

# Get all documents
GET /api/documents

# Get documents by status
GET /api/documents/status/COMPLETED
```

### WebSocket Chat
Connect to `/ws` endpoint for real-time chat:
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    stompClient.subscribe('/topic/public', function(message) {
        // Handle incoming messages
    });
});
```

## 📊 API Documentation

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

## 🔧 Configuration

### Application Properties
Key configuration options in `application.yml`:

```yaml
chatbot:
  document:
    max-size: 10485760  # 10MB
    allowed-types: application/pdf,text/plain,application/msword
  chat:
    session-timeout: 86400  # 24 hours
    max-history: 50
  vector:
    similarity-threshold: 0.7
    max-results: 5
```

### Production Configuration
For production deployment, use `application-prod.yml` with:
- External database connections
- Redis clustering
- Enhanced security settings
- Performance optimizations

## 🧪 Testing

Run the test suite:
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ChatControllerTest

# Run with coverage
mvn test jacoco:report
```

## 📈 Monitoring & Health Checks

Access monitoring endpoints:
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Info: `http://localhost:8080/actuator/info`

## 🚀 Deployment

### Docker Deployment
```dockerfile
FROM openjdk:17-jre-slim
COPY target/chatbot-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables for Production
```bash
SPRING_PROFILES_ACTIVE=prod
OPENAI_API_KEY=your-production-key
DATABASE_URL=jdbc:postgresql://prod-db:5432/chatbot_db
REDIS_HOST=prod-redis
```

## 🔒 Security Features

- Input validation and sanitization
- File type and size restrictions
- Session management with Redis
- CORS configuration
- SQL injection prevention
- XSS protection

## 📝 Supported File Types

- **PDF**: `.pdf` files
- **Microsoft Word**: `.doc`, `.docx` files
- **Text**: `.txt` files
- **Maximum Size**: 10MB per file

## 🎯 Use Cases

1. **Enterprise Knowledge Base**: Upload company documents and policies
2. **Technical Documentation**: Query API docs, manuals, and guides
3. **Research Assistant**: Analyze research papers and reports
4. **Customer Support**: Quick access to product information
5. **Training Materials**: Interactive learning from training documents

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Troubleshooting

### Common Issues

**Database Connection Issues:**
- Verify PostgreSQL is running and pgvector is installed
- Check connection credentials in application.yml

**OpenAI API Issues:**
- Verify API key is valid and has sufficient credits
- Check rate limits and quotas

**Document Processing Failures:**
- Ensure file types are supported
- Check file size limits
- Verify Tika dependencies are included

**Redis Connection Issues:**
- Verify Redis server is running
- Check Redis configuration in application.yml

### Logs
Check application logs in:
- Development: Console output
- Production: `/var/log/chatbot/application.log`

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the API documentation

---

**Built with ❤️ using Spring Boot and Spring AI**