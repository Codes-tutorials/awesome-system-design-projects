# Enterprise Chatbot - Quick Start Guide

Get your enterprise chatbot up and running in minutes!

## 🚀 Quick Setup (5 minutes)

### Prerequisites Check
```bash
java -version    # Should be 17+
mvn -version     # Should be 3.6+
psql --version   # PostgreSQL 12+
redis-cli ping   # Should return PONG
```

### 1. Database Setup (2 minutes)
```bash
# Start PostgreSQL and create database
sudo -u postgres psql
```
```sql
CREATE DATABASE chatbot_db;
CREATE USER chatbot_user WITH PASSWORD 'chatbot_password';
GRANT ALL PRIVILEGES ON DATABASE chatbot_db TO chatbot_user;
\c chatbot_db
CREATE EXTENSION IF NOT EXISTS vector;
\q
```

### 2. Start Redis
```bash
redis-server --daemonize yes
```

### 3. Set OpenAI API Key
```bash
export OPENAI_API_KEY=your-openai-api-key-here
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

### 5. Access the Application
Open your browser to: **http://localhost:8080**

## 🎯 First Steps

### Upload Your First Document
1. Click the upload area in the sidebar
2. Select a PDF, Word, or text file (max 10MB)
3. Wait for processing to complete (status will show "COMPLETED")

### Start Chatting
1. Type a question about your uploaded document
2. Press Enter or click the send button
3. Get AI-powered responses based on your content!

## 📋 Example Interactions

**Upload a company policy document, then ask:**
- "What is the vacation policy?"
- "How many sick days do employees get?"
- "What are the remote work guidelines?"

**Upload a technical manual, then ask:**
- "How do I configure the database?"
- "What are the system requirements?"
- "Explain the installation process"

## 🔧 Configuration (Optional)

### Custom OpenAI Model
Edit `application.yml`:
```yaml
spring:
  ai:
    openai:
      chat:
        options:
          model: gpt-3.5-turbo  # or gpt-4
          temperature: 0.7
```

### File Upload Limits
```yaml
chatbot:
  document:
    max-size: 20971520  # 20MB
```

## 🐳 Docker Quick Start

### Using Docker Compose
```yaml
version: '3.8'
services:
  chatbot:
    build: .
    ports:
      - "8080:8080"
    environment:
      - OPENAI_API_KEY=your-key
      - DATABASE_URL=jdbc:postgresql://db:5432/chatbot_db
      - REDIS_HOST=redis
    depends_on:
      - db
      - redis
  
  db:
    image: pgvector/pgvector:pg15
    environment:
      - POSTGRES_DB=chatbot_db
      - POSTGRES_USER=chatbot_user
      - POSTGRES_PASSWORD=chatbot_password
  
  redis:
    image: redis:7-alpine
```

Run with:
```bash
docker-compose up -d
```

## 🧪 Test the Setup

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### API Test
```bash
# Create session
curl -X POST http://localhost:8080/api/chat/session

# Send message (replace SESSION_ID)
curl -X POST http://localhost:8080/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"SESSION_ID","message":"Hello!"}'
```

## 🚨 Troubleshooting

### Common Issues

**"Connection refused" errors:**
```bash
# Check if services are running
sudo systemctl status postgresql
sudo systemctl status redis
```

**"Invalid API key" errors:**
```bash
# Verify your OpenAI API key
echo $OPENAI_API_KEY
```

**Database connection issues:**
```bash
# Test database connection
psql -h localhost -U chatbot_user -d chatbot_db
```

**Port already in use:**
```bash
# Change port in application.yml
server:
  port: 8081
```

### Reset Everything
```bash
# Stop application (Ctrl+C)
# Drop and recreate database
sudo -u postgres psql -c "DROP DATABASE IF EXISTS chatbot_db;"
sudo -u postgres psql -c "CREATE DATABASE chatbot_db;"
# Restart application
mvn spring-boot:run
```

## 📚 Next Steps

1. **Read the full README.md** for detailed configuration
2. **Check API documentation** at http://localhost:8080/swagger-ui.html
3. **Upload multiple documents** to build your knowledge base
4. **Customize the UI** by modifying files in `src/main/resources/static/`
5. **Set up production deployment** using the production configuration guide

## 💡 Pro Tips

- **Upload related documents together** for better context
- **Use specific questions** for more accurate responses
- **Check document processing status** before asking questions
- **Start new sessions** for different topics or contexts
- **Monitor logs** for debugging: `tail -f logs/chatbot.log`

## 🎉 You're Ready!

Your enterprise chatbot is now running and ready to help with document-based Q&A. Upload your documents and start asking questions!

---

**Need help?** Check the full README.md or create an issue in the repository.