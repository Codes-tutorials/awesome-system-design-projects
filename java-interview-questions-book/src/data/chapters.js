export const chapters = [
  {
    id: 'java-fundamentals',
    title: 'Java Fundamentals & Core Concepts',
    description: 'Deep dive into Java fundamentals, language features, and core concepts for senior developers.',
    questions: [
      {
        id: 'jf-001',
        question: 'Explain the difference between `==` and `equals()` method in Java. Provide a scenario where this distinction becomes critical in a production application.',
        difficulty: 'intermediate',
        category: 'Core Java',
        scenario: 'String comparison in user authentication system',
        answer: `The \`==\` operator compares references (memory addresses) while \`equals()\` method compares the actual content of objects.

**Critical Production Scenario:**
In a user authentication system, comparing passwords or usernames incorrectly can lead to security vulnerabilities.

\`\`\`java
public class AuthenticationService {
    private static final String ADMIN_USERNAME = "admin";
    
    // WRONG - Security vulnerability
    public boolean authenticateWrong(String username) {
        return username == ADMIN_USERNAME; // Compares references
    }
    
    // CORRECT - Secure comparison
    public boolean authenticateCorrect(String username) {
        return ADMIN_USERNAME.equals(username); // Compares content
    }
    
    // BEST PRACTICE - Null-safe comparison
    public boolean authenticateBest(String username) {
        return Objects.equals(ADMIN_USERNAME, username);
    }
}
\`\`\`

**Why this matters in production:**
- String literals are interned, but user input strings are not
- \`new String("admin")\` creates a new object, \`==\` will return false
- Can lead to authentication bypass or denial of service
- Always use \`.equals()\` for content comparison`,
        tags: ['string-comparison', 'security', 'authentication', 'best-practices'],
        followUp: [
          'How does string interning affect this comparison?',
          'What are the performance implications of using equals() vs ==?',
          'How would you implement a custom equals() method?'
        ]
      },
      {
        id: 'jf-004',
        question: 'Explain the difference between `final`, `finally`, and `finalize()` in Java. Provide scenarios where each would be critical in enterprise applications.',
        difficulty: 'intermediate',
        category: 'Core Java',
        scenario: 'Resource management in enterprise application with database connections and file handling',
        answer: `These three keywords serve completely different purposes in Java and are often confused in interviews.

**1. \`final\` Keyword:**
Used for creating constants, preventing inheritance, and method overriding.

\`\`\`java
public class DatabaseConfig {
    // Constant - cannot be changed
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/app";
    
    // Final variable - must be initialized
    private final String instanceId;
    
    public DatabaseConfig(String instanceId) {
        this.instanceId = instanceId; // Can only be set once
    }
    
    // Final method - cannot be overridden
    public final void validateConnection() {
        // Critical validation logic that subclasses cannot change
    }
}

// Final class - cannot be extended
public final class SecurityUtils {
    public static String encrypt(String data) {
        // Security implementation that must not be modified
        return "encrypted_" + data;
    }
}
\`\`\`

**2. \`finally\` Block:**
Always executes, used for cleanup operations.

\`\`\`java
public class FileProcessor {
    public void processLargeFile(String filename) {
        FileInputStream fis = null;
        BufferedReader reader = null;
        
        try {
            fis = new FileInputStream(filename);
            reader = new BufferedReader(new InputStreamReader(fis));
            
            // Process file - might throw IOException
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
            
        } catch (IOException e) {
            logger.error("File processing failed: " + filename, e);
            throw new ProcessingException("Failed to process file", e);
            
        } finally {
            // ALWAYS executes - even if exception occurs or return statement
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("Failed to close reader", e);
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.warn("Failed to close file stream", e);
                }
            }
        }
    }
}
\`\`\`

**3. \`finalize()\` Method:**
Called by garbage collector before object destruction (deprecated in Java 9+).

\`\`\`java
public class LegacyResourceManager {
    private long nativeHandle;
    
    public LegacyResourceManager() {
        this.nativeHandle = allocateNativeResource();
    }
    
    // DEPRECATED - Don't use in modern Java
    @Override
    protected void finalize() throws Throwable {
        try {
            if (nativeHandle != 0) {
                freeNativeResource(nativeHandle);
                nativeHandle = 0;
            }
        } finally {
            super.finalize();
        }
    }
    
    // BETTER APPROACH - Explicit cleanup
    public void close() {
        if (nativeHandle != 0) {
            freeNativeResource(nativeHandle);
            nativeHandle = 0;
        }
    }
    
    private native long allocateNativeResource();
    private native void freeNativeResource(long handle);
}

// MODERN APPROACH - try-with-resources
public class ModernResourceManager implements AutoCloseable {
    private Connection connection;
    
    public ModernResourceManager(String url) throws SQLException {
        this.connection = DriverManager.getConnection(url);
    }
    
    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    // Usage with automatic cleanup
    public void processData() {
        try (ModernResourceManager manager = new ModernResourceManager(DB_URL)) {
            // Use manager - automatically closed even if exception occurs
            manager.executeQuery("SELECT * FROM users");
        } catch (SQLException e) {
            logger.error("Database operation failed", e);
        }
    }
}
\`\`\`

**Enterprise Application Scenarios:**

**\`final\` in Enterprise:**
- Configuration constants that must not change
- Security methods that cannot be overridden
- Immutable data transfer objects

**\`finally\` in Enterprise:**
- Database connection cleanup
- File handle closure
- Releasing locks and semaphores
- Logging completion status

**\`finalize()\` Issues:**
- Unpredictable execution timing
- Performance overhead
- Can prevent garbage collection
- Deprecated since Java 9

**Best Practices:**
1. Use \`final\` for constants and preventing inheritance
2. Use \`finally\` for guaranteed cleanup
3. Avoid \`finalize()\` - use try-with-resources instead
4. Implement \`AutoCloseable\` for resource management`,
        tags: ['final', 'finally', 'finalize', 'resource-management', 'cleanup', 'enterprise'],
        followUp: [
          'What happens if an exception is thrown in the finally block?',
          'How does try-with-resources compare to finally blocks?',
          'Why was finalize() deprecated and what are the alternatives?'
        ]
      },
      {
        id: 'jf-005',
        question: 'Design a custom exception hierarchy for a banking application. Explain when to use checked vs unchecked exceptions and how to handle them properly in a microservices architecture.',
        difficulty: 'expert',
        category: 'Exception Handling',
        scenario: 'Banking microservices with transaction processing, account management, and fraud detection',
        answer: `Exception design is critical in banking systems where reliability and proper error handling can prevent financial losses.

**Custom Exception Hierarchy:**

\`\`\`java
// Base exception for all banking operations
public abstract class BankingException extends Exception {
    private final String errorCode;
    private final String userMessage;
    private final Map<String, Object> context;
    
    protected BankingException(String errorCode, String message, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.context = new HashMap<>();
    }
    
    protected BankingException(String errorCode, String message, String userMessage, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.context = new HashMap<>();
    }
    
    public String getErrorCode() { return errorCode; }
    public String getUserMessage() { return userMessage; }
    public Map<String, Object> getContext() { return context; }
    
    public void addContext(String key, Object value) {
        context.put(key, value);
    }
}

// Checked Exceptions - Recoverable business errors
public class InsufficientFundsException extends BankingException {
    private final BigDecimal availableBalance;
    private final BigDecimal requestedAmount;
    
    public InsufficientFundsException(BigDecimal available, BigDecimal requested) {
        super("INSUFFICIENT_FUNDS", 
              String.format("Insufficient funds: available=%.2f, requested=%.2f", available, requested),
              "Insufficient funds in your account");
        this.availableBalance = available;
        this.requestedAmount = requested;
        addContext("availableBalance", available);
        addContext("requestedAmount", requested);
    }
    
    public BigDecimal getAvailableBalance() { return availableBalance; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
}

public class AccountNotFoundException extends BankingException {
    private final String accountNumber;
    
    public AccountNotFoundException(String accountNumber) {
        super("ACCOUNT_NOT_FOUND",
              "Account not found: " + accountNumber,
              "The specified account could not be found");
        this.accountNumber = accountNumber;
        addContext("accountNumber", accountNumber);
    }
    
    public String getAccountNumber() { return accountNumber; }
}

public class TransactionLimitExceededException extends BankingException {
    private final BigDecimal limit;
    private final BigDecimal attemptedAmount;
    
    public TransactionLimitExceededException(BigDecimal limit, BigDecimal attempted) {
        super("TRANSACTION_LIMIT_EXCEEDED",
              String.format("Transaction limit exceeded: limit=%.2f, attempted=%.2f", limit, attempted),
              "Transaction amount exceeds your daily limit");
        this.limit = limit;
        this.attemptedAmount = attempted;
        addContext("limit", limit);
        addContext("attemptedAmount", attempted);
    }
}

// Unchecked Exceptions - Programming errors or system failures
public class BankingSystemException extends RuntimeException {
    private final String errorCode;
    
    public BankingSystemException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BankingSystemException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() { return errorCode; }
}

public class DatabaseConnectionException extends BankingSystemException {
    public DatabaseConnectionException(String message, Throwable cause) {
        super("DB_CONNECTION_FAILED", message, cause);
    }
}

public class FraudDetectionException extends BankingSystemException {
    private final String riskScore;
    
    public FraudDetectionException(String riskScore, String reason) {
        super("FRAUD_DETECTED", "Fraudulent activity detected: " + reason);
        this.riskScore = riskScore;
    }
    
    public String getRiskScore() { return riskScore; }
}
\`\`\`

**Service Layer Implementation:**

\`\`\`java
@Service
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final FraudDetectionService fraudService;
    
    public TransactionResult transfer(String fromAccount, String toAccount, BigDecimal amount) 
            throws InsufficientFundsException, AccountNotFoundException, TransactionLimitExceededException {
        
        try {
            // Validate accounts exist
            Account from = accountRepository.findByNumber(fromAccount)
                .orElseThrow(() -> new AccountNotFoundException(fromAccount));
            Account to = accountRepository.findByNumber(toAccount)
                .orElseThrow(() -> new AccountNotFoundException(toAccount));
            
            // Check fraud detection
            FraudResult fraudResult = fraudService.checkTransaction(from, to, amount);
            if (fraudResult.isHighRisk()) {
                throw new FraudDetectionException(fraudResult.getRiskScore(), fraudResult.getReason());
            }
            
            // Check balance
            if (from.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException(from.getBalance(), amount);
            }
            
            // Check transaction limits
            BigDecimal dailyLimit = from.getDailyTransactionLimit();
            BigDecimal todayTotal = transactionService.getTodayTotal(fromAccount);
            if (todayTotal.add(amount).compareTo(dailyLimit) > 0) {
                throw new TransactionLimitExceededException(dailyLimit, amount);
            }
            
            // Execute transaction
            return transactionService.executeTransfer(from, to, amount);
            
        } catch (DataAccessException e) {
            // Convert unchecked database exceptions to system exceptions
            throw new DatabaseConnectionException("Database error during transfer", e);
        }
    }
}
\`\`\`

**Microservices Exception Handling:**

\`\`\`java
@RestControllerAdvice
public class BankingExceptionHandler {
    
    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInsufficientFunds(InsufficientFundsException e) {
        return ErrorResponse.builder()
            .errorCode(e.getErrorCode())
            .message(e.getUserMessage())
            .details(e.getContext())
            .timestamp(Instant.now())
            .build();
    }
    
    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleAccountNotFound(AccountNotFoundException e) {
        return ErrorResponse.builder()
            .errorCode(e.getErrorCode())
            .message(e.getUserMessage())
            .timestamp(Instant.now())
            .build();
    }
    
    @ExceptionHandler(FraudDetectionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleFraudDetection(FraudDetectionException e) {
        // Log security incident
        securityLogger.warn("Fraud detected: {}", e.getMessage());
        
        return ErrorResponse.builder()
            .errorCode(e.getErrorCode())
            .message("Transaction blocked for security reasons")
            .timestamp(Instant.now())
            .build();
    }
    
    @ExceptionHandler(BankingSystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleSystemException(BankingSystemException e) {
        // Log system error
        logger.error("System error: {}", e.getMessage(), e);
        
        return ErrorResponse.builder()
            .errorCode(e.getErrorCode())
            .message("A system error occurred. Please try again later.")
            .timestamp(Instant.now())
            .build();
    }
}

@Data
@Builder
public class ErrorResponse {
    private String errorCode;
    private String message;
    private Map<String, Object> details;
    private Instant timestamp;
}
\`\`\`

**Circuit Breaker Integration:**

\`\`\`java
@Component
public class ExternalServiceClient {
    
    @CircuitBreaker(name = "fraud-service", fallbackMethod = "fallbackFraudCheck")
    @Retry(name = "fraud-service")
    public FraudResult checkFraud(TransactionRequest request) {
        try {
            return fraudServiceClient.checkTransaction(request);
        } catch (FeignException e) {
            throw new BankingSystemException("FRAUD_SERVICE_ERROR", 
                "Fraud detection service unavailable", e);
        }
    }
    
    public FraudResult fallbackFraudCheck(TransactionRequest request, Exception e) {
        logger.warn("Fraud service unavailable, using fallback", e);
        // Return conservative result
        return FraudResult.builder()
            .riskScore("MEDIUM")
            .approved(request.getAmount().compareTo(new BigDecimal("1000")) <= 0)
            .reason("Service unavailable - conservative approval")
            .build();
    }
}
\`\`\`

**Best Practices:**

**Checked vs Unchecked:**
- **Checked**: Business logic errors that caller can handle
- **Unchecked**: Programming errors or system failures

**Microservices Considerations:**
- Include correlation IDs for tracing
- Don't expose internal details in error messages
- Use circuit breakers for external service failures
- Implement proper logging and monitoring
- Consider retry mechanisms for transient failures`,
        tags: ['exceptions', 'banking', 'microservices', 'error-handling', 'checked-exceptions', 'circuit-breaker'],
        followUp: [
          'How would you handle distributed transaction failures across microservices?',
          'What metrics would you track for exception monitoring?',
          'How do you ensure exception handling doesn\'t leak sensitive information?'
        ]
      },
      {
        id: 'jf-006',
        question: 'Implement a type-safe generic cache with TTL (Time To Live) support. Explain the challenges with generic type erasure and how to work around them.',
        difficulty: 'expert',
        category: 'Generics',
        scenario: 'High-performance caching layer for e-commerce application with different data types',
        answer: `Generic type erasure is one of Java's most challenging aspects. Here's a production-ready cache implementation that addresses these challenges.

**Type-Safe Generic Cache with TTL:**

\`\`\`java
public class TTLCache<K, V> {
    private final ConcurrentHashMap<K, CacheEntry<V>> cache;
    private final ScheduledExecutorService cleanupExecutor;
    private final long defaultTtlMillis;
    private final int maxSize;
    
    // For type safety with generics
    private final Class<V> valueType;
    
    private static class CacheEntry<V> {
        private final V value;
        private final long expirationTime;
        private volatile long lastAccessed;
        
        public CacheEntry(V value, long ttlMillis) {
            this.value = value;
            this.expirationTime = System.currentTimeMillis() + ttlMillis;
            this.lastAccessed = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
        
        public V getValue() {
            this.lastAccessed = System.currentTimeMillis();
            return value;
        }
        
        public long getLastAccessed() {
            return lastAccessed;
        }
    }
    
    // Constructor with type token to work around type erasure
    public TTLCache(Class<V> valueType, long defaultTtlMillis, int maxSize) {
        this.valueType = valueType;
        this.defaultTtlMillis = defaultTtlMillis;
        this.maxSize = maxSize;
        this.cache = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TTLCache-Cleanup");
            t.setDaemon(true);
            return t;
        });
        
        // Schedule cleanup every 30 seconds
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, 30, 30, TimeUnit.SECONDS);
    }
    
    public void put(K key, V value) {
        put(key, value, defaultTtlMillis);
    }
    
    public void put(K key, V value, long ttlMillis) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");
        
        // Type safety check at runtime
        if (!valueType.isInstance(value)) {
            throw new ClassCastException("Value must be of type " + valueType.getName());
        }
        
        // Evict if cache is full
        if (cache.size() >= maxSize) {
            evictLRU();
        }
        
        cache.put(key, new CacheEntry<>(value, ttlMillis));
    }
    
    public Optional<V> get(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            return Optional.empty();
        }
        
        return Optional.of(entry.getValue());
    }
    
    // Type-safe bulk operations
    public Map<K, V> getAll(Collection<K> keys) {
        Map<K, V> result = new HashMap<>();
        for (K key : keys) {
            get(key).ifPresent(value -> result.put(key, value));
        }
        return result;
    }
    
    public void putAll(Map<K, V> entries) {
        entries.forEach(this::put);
    }
    
    // Generic method with bounded type parameters
    public <T extends V> void putTyped(K key, T value, long ttlMillis) {
        put(key, value, ttlMillis);
    }
    
    // Wildcard usage for flexible retrieval
    public Optional<? extends V> getWildcard(K key) {
        return get(key);
    }
    
    private void evictLRU() {
        K lruKey = cache.entrySet().stream()
            .min(Comparator.comparing(entry -> entry.getValue().getLastAccessed()))
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (lruKey != null) {
            cache.remove(lruKey);
        }
    }
    
    private void cleanup() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // Statistics and monitoring
    public CacheStats getStats() {
        long expired = cache.values().stream()
            .mapToLong(entry -> entry.isExpired() ? 1 : 0)
            .sum();
        
        return new CacheStats(cache.size(), expired, maxSize);
    }
    
    public static class CacheStats {
        private final int currentSize;
        private final long expiredEntries;
        private final int maxSize;
        
        public CacheStats(int currentSize, long expiredEntries, int maxSize) {
            this.currentSize = currentSize;
            this.expiredEntries = expiredEntries;
            this.maxSize = maxSize;
        }
        
        // Getters...
    }
}
\`\`\`

**Working Around Type Erasure:**

\`\`\`java
// 1. Type Token Pattern
public abstract class TypeReference<T> {
    private final Type type;
    
    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }
    
    public Type getType() {
        return type;
    }
    
    @SuppressWarnings("unchecked")
    public Class<T> getRawType() {
        if (type instanceof Class) {
            return (Class<T>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) type).getRawType();
        }
        throw new IllegalArgumentException("Cannot determine raw type for " + type);
    }
}

// Enhanced cache with type reference support
public class TypeSafeTTLCache<K, V> extends TTLCache<K, V> {
    private final TypeReference<V> typeReference;
    
    public TypeSafeTTLCache(TypeReference<V> typeReference, long defaultTtlMillis, int maxSize) {
        super(typeReference.getRawType(), defaultTtlMillis, maxSize);
        this.typeReference = typeReference;
    }
    
    // JSON serialization with type safety
    public void putJson(K key, String json, ObjectMapper mapper) throws JsonProcessingException {
        V value = mapper.readValue(json, typeReference.getRawType());
        put(key, value);
    }
    
    public String getAsJson(K key, ObjectMapper mapper) throws JsonProcessingException {
        return get(key)
            .map(value -> {
                try {
                    return mapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            })
            .orElse(null);
    }
}
\`\`\`

**E-commerce Usage Examples:**

\`\`\`java
@Component
public class EcommerceCacheManager {
    private final TTLCache<String, Product> productCache;
    private final TTLCache<Long, User> userCache;
    private final TTLCache<String, List<Category>> categoryCache;
    private final ObjectMapper objectMapper;
    
    public EcommerceCacheManager() {
        // Different TTL for different data types
        this.productCache = new TTLCache<>(Product.class, TimeUnit.HOURS.toMillis(2), 10000);
        this.userCache = new TTLCache<>(User.class, TimeUnit.MINUTES.toMillis(30), 5000);
        
        // Complex generic types using TypeReference
        this.categoryCache = new TypeSafeTTLCache<>(
            new TypeReference<List<Category>>() {}, 
            TimeUnit.HOURS.toMillis(6), 
            1000
        );
        
        this.objectMapper = new ObjectMapper();
    }
    
    // Type-safe product caching
    public void cacheProduct(Product product) {
        productCache.put(product.getSku(), product);
    }
    
    public Optional<Product> getProduct(String sku) {
        return productCache.get(sku);
    }
    
    // Bulk operations with type safety
    public Map<String, Product> getProducts(List<String> skus) {
        return productCache.getAll(skus);
    }
    
    // Generic method for flexible caching
    public <T extends Serializable> void cacheSerializable(String key, T value, long ttlMillis) {
        // This would require a separate cache or more complex type handling
        // Demonstrates the challenges with generic type erasure
    }
    
    // Cache warming with type safety
    public void warmProductCache(List<Product> products) {
        Map<String, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getSku, Function.identity()));
        productCache.putAll(productMap);
    }
}

// Usage in service layer
@Service
public class ProductService {
    private final EcommerceCacheManager cacheManager;
    private final ProductRepository productRepository;
    
    public Product getProduct(String sku) {
        return cacheManager.getProduct(sku)
            .orElseGet(() -> {
                Product product = productRepository.findBySku(sku);
                if (product != null) {
                    cacheManager.cacheProduct(product);
                }
                return product;
            });
    }
}
\`\`\`

**Challenges and Solutions:**

**1. Type Erasure Issues:**
- Runtime type information is lost
- Cannot create generic arrays
- Cannot use instanceof with generic types

**2. Solutions:**
- Type tokens (Class<T> parameters)
- TypeReference pattern for complex types
- Bounded type parameters
- Wildcard types for flexibility

**3. Performance Considerations:**
- ConcurrentHashMap for thread safety
- Scheduled cleanup to prevent memory leaks
- LRU eviction for size management
- Lazy expiration checking

**4. Production Features:**
- Monitoring and statistics
- Graceful shutdown
- Configurable TTL per entry
- Bulk operations for efficiency`,
        tags: ['generics', 'type-erasure', 'caching', 'ttl', 'concurrent', 'type-safety'],
        followUp: [
          'How would you implement cache invalidation strategies?',
          'What are the trade-offs between different eviction policies?',
          'How would you handle cache coherence in a distributed system?'
        ]
      },
      {
        id: 'jf-002',
        question: 'Design a thread-safe Singleton pattern that handles high-concurrency scenarios. Explain why double-checked locking is problematic and provide a better alternative.',
        difficulty: 'expert',
        category: 'Design Patterns',
        scenario: 'Database connection pool manager in high-traffic application',
        answer: `Traditional double-checked locking has issues due to instruction reordering and memory visibility problems.

**Problematic Double-Checked Locking:**
\`\`\`java
public class ProblematicSingleton {
    private static volatile ProblematicSingleton instance;
    
    public static ProblematicSingleton getInstance() {
        if (instance == null) { // First check
            synchronized (ProblematicSingleton.class) {
                if (instance == null) { // Second check
                    instance = new ProblematicSingleton(); // Problem here!
                }
            }
        }
        return instance;
    }
}
\`\`\`

**Issues:**
1. Constructor execution can be reordered
2. Partial object initialization visibility
3. Complex and error-prone

**Better Alternatives:**

**1. Enum Singleton (Recommended):**
\`\`\`java
public enum DatabaseConnectionPool {
    INSTANCE;
    
    private final HikariDataSource dataSource;
    
    DatabaseConnectionPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost/db");
        config.setMaximumPoolSize(20);
        this.dataSource = new HikariDataSource(config);
    }
    
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
\`\`\`

**2. Initialization-on-demand holder:**
\`\`\`java
public class DatabaseConnectionPool {
    private DatabaseConnectionPool() {}
    
    private static class Holder {
        private static final DatabaseConnectionPool INSTANCE = 
            new DatabaseConnectionPool();
    }
    
    public static DatabaseConnectionPool getInstance() {
        return Holder.INSTANCE;
    }
}
\`\`\`

**Why Enum is best:**
- Thread-safe by JVM guarantee
- Serialization-safe
- Reflection-proof
- Simple and clean
- No performance overhead`,
        tags: ['singleton', 'thread-safety', 'concurrency', 'design-patterns', 'enum'],
        followUp: [
          'How does the JVM guarantee thread safety for enums?',
          'What happens during serialization/deserialization of singletons?',
          'How would you test a singleton in a multithreaded environment?'
        ]
      },
      {
        id: 'jf-003',
        question: 'Explain Java Memory Model (JMM) and how it affects visibility of variables across threads. Provide a real-world example where understanding JMM prevents a production bug.',
        difficulty: 'expert',
        category: 'Concurrency',
        scenario: 'Cache invalidation in distributed system',
        answer: `The Java Memory Model defines how threads interact through memory and what behaviors are allowed in concurrent execution.

**Key JMM Concepts:**
1. **Happens-before relationship**
2. **Memory visibility**
3. **Instruction reordering**
4. **Volatile semantics**

**Production Bug Scenario - Cache Invalidation:**
\`\`\`java
public class DistributedCache {
    private boolean cacheValid = true;
    private Map<String, Object> cache = new ConcurrentHashMap<>();
    
    // BUG: Without volatile, other threads might not see the change
    public void invalidateCache() {
        cache.clear();
        cacheValid = false; // This change might not be visible!
    }
    
    public Object get(String key) {
        if (cacheValid) { // Might read stale value
            return cache.get(key);
        }
        return fetchFromDatabase(key);
    }
}
\`\`\`

**The Problem:**
- Thread A calls \`invalidateCache()\`
- Thread B might still see \`cacheValid = true\`
- Results in serving stale data from cleared cache
- Can cause data inconsistency in production

**Solution with proper JMM understanding:**
\`\`\`java
public class DistributedCache {
    private volatile boolean cacheValid = true; // Ensures visibility
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public void invalidateCache() {
        lock.writeLock().lock();
        try {
            cache.clear();
            cacheValid = false; // Happens-before guarantee
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public Object get(String key) {
        lock.readLock().lock();
        try {
            if (cacheValid) {
                return cache.get(key);
            }
            return fetchFromDatabase(key);
        } finally {
            lock.readLock().unlock();
        }
    }
}
\`\`\`

**Better Solution using AtomicReference:**
\`\`\`java
public class DistributedCache {
    private final AtomicReference<Map<String, Object>> cacheRef = 
        new AtomicReference<>(new ConcurrentHashMap<>());
    
    public void invalidateCache() {
        cacheRef.set(new ConcurrentHashMap<>());
    }
    
    public Object get(String key) {
        Map<String, Object> currentCache = cacheRef.get();
        Object value = currentCache.get(key);
        return value != null ? value : fetchFromDatabase(key);
    }
}
\`\`\`

**JMM Guarantees:**
- Volatile reads/writes create happens-before edges
- Synchronized blocks provide mutual exclusion and visibility
- AtomicReference operations are atomic and visible`,
        tags: ['jmm', 'memory-model', 'volatile', 'concurrency', 'cache', 'atomic'],
        followUp: [
          'What is the difference between volatile and synchronized?',
          'How does happens-before relationship work?',
          'What are the performance implications of different synchronization mechanisms?'
        ]
      }
    ]
  },
  {
    id: 'oop-concepts',
    title: 'Object-Oriented Programming',
    description: 'Advanced OOP concepts, design principles, and real-world applications.',
    questions: [
      {
        id: 'oop-001',
        question: 'Design a payment processing system that demonstrates SOLID principles. Show how violating these principles leads to maintenance nightmares.',
        difficulty: 'expert',
        category: 'Design Principles',
        scenario: 'E-commerce payment gateway integration',
        answer: `Let's design a payment system that follows SOLID principles and show the consequences of violations.

**VIOLATION Example (Anti-pattern):**
\`\`\`java
// Violates SRP, OCP, DIP
public class PaymentProcessor {
    public void processPayment(String type, double amount, String details) {
        if ("CREDIT_CARD".equals(type)) {
            // Credit card processing logic
            validateCreditCard(details);
            chargeCreditCard(amount, details);
            sendCreditCardReceipt(details);
        } else if ("PAYPAL".equals(type)) {
            // PayPal processing logic
            validatePayPal(details);
            chargePayPal(amount, details);
            sendPayPalReceipt(details);
        } else if ("BITCOIN".equals(type)) {
            // Bitcoin processing logic - NEW REQUIREMENT
            validateBitcoin(details);
            chargeBitcoin(amount, details);
            sendBitcoinReceipt(details);
        }
        // Adding new payment method requires modifying this class!
    }
    
    // All validation, charging, and receipt methods here
    // This class has too many responsibilities!
}
\`\`\`

**SOLID-Compliant Design:**

**1. Single Responsibility Principle:**
\`\`\`java
public interface PaymentMethod {
    PaymentResult process(PaymentRequest request);
    boolean supports(PaymentType type);
}

public class CreditCardPayment implements PaymentMethod {
    private final CreditCardValidator validator;
    private final CreditCardGateway gateway;
    private final ReceiptService receiptService;
    
    @Override
    public PaymentResult process(PaymentRequest request) {
        validator.validate(request);
        PaymentResult result = gateway.charge(request);
        receiptService.send(result);
        return result;
    }
    
    @Override
    public boolean supports(PaymentType type) {
        return PaymentType.CREDIT_CARD.equals(type);
    }
}
\`\`\`

**2. Open/Closed Principle:**
\`\`\`java
public class PaymentProcessor {
    private final List<PaymentMethod> paymentMethods;
    
    public PaymentProcessor(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }
    
    public PaymentResult processPayment(PaymentRequest request) {
        PaymentMethod method = paymentMethods.stream()
            .filter(pm -> pm.supports(request.getType()))
            .findFirst()
            .orElseThrow(() -> new UnsupportedPaymentException(request.getType()));
            
        return method.process(request);
    }
    
    // Adding new payment method doesn't require changing this class!
}
\`\`\`

**3. Liskov Substitution Principle:**
\`\`\`java
public abstract class PaymentMethod {
    public final PaymentResult process(PaymentRequest request) {
        validateRequest(request);
        PaymentResult result = doProcess(request);
        postProcess(result);
        return result;
    }
    
    protected abstract PaymentResult doProcess(PaymentRequest request);
    
    protected void validateRequest(PaymentRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
    }
    
    protected void postProcess(PaymentResult result) {
        // Common post-processing logic
    }
}

// All implementations can be substituted without breaking functionality
public class BitcoinPayment extends PaymentMethod {
    @Override
    protected PaymentResult doProcess(PaymentRequest request) {
        // Bitcoin-specific processing
        return bitcoinGateway.process(request);
    }
}
\`\`\`

**4. Interface Segregation Principle:**
\`\`\`java
public interface PaymentValidator {
    void validate(PaymentRequest request);
}

public interface PaymentGateway {
    PaymentResult charge(PaymentRequest request);
}

public interface RefundCapable {
    RefundResult refund(RefundRequest request);
}

public interface RecurringPaymentCapable {
    SubscriptionResult setupRecurring(RecurringRequest request);
}

// Clients only depend on interfaces they use
public class CreditCardPayment implements PaymentMethod, RefundCapable, RecurringPaymentCapable {
    // Implementation
}
\`\`\`

**5. Dependency Inversion Principle:**
\`\`\`java
@Component
public class PaymentService {
    private final PaymentProcessor processor;
    private final PaymentRepository repository;
    private final NotificationService notificationService;
    
    // Depends on abstractions, not concretions
    public PaymentService(PaymentProcessor processor,
                         PaymentRepository repository,
                         NotificationService notificationService) {
        this.processor = processor;
        this.repository = repository;
        this.notificationService = notificationService;
    }
}
\`\`\`

**Benefits of SOLID Design:**
- Easy to add new payment methods
- Each class has single responsibility
- Testable in isolation
- Maintainable and extensible
- Reduced coupling`,
        tags: ['solid', 'design-principles', 'payment-processing', 'architecture'],
        followUp: [
          'How would you handle payment method discovery and registration?',
          'What design patterns complement SOLID principles?',
          'How do you balance SOLID principles with performance requirements?'
        ]
      }
    ]
  },
  {
    id: 'collections-framework',
    title: 'Collections Framework & Data Structures',
    description: 'Advanced collections usage, custom implementations, and performance considerations.',
    questions: [
      {
        id: 'cf-001',
        question: 'Implement a thread-safe LRU Cache that can handle high-concurrency scenarios. Compare different approaches and their trade-offs.',
        difficulty: 'expert',
        category: 'Data Structures',
        scenario: 'Caching layer for microservices architecture',
        answer: `Let's implement a thread-safe LRU Cache with different approaches and analyze their trade-offs.

**Approach 1: Synchronized LinkedHashMap (Simple but Limited)**
\`\`\`java
public class SynchronizedLRUCache<K, V> {
    private final int capacity;
    private final Map<K, V> cache;
    
    public SynchronizedLRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = Collections.synchronizedMap(
            new LinkedHashMap<K, V>(capacity + 1, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    return size() > capacity;
                }
            }
        );
    }
    
    public V get(K key) {
        return cache.get(key); // Synchronized but blocks all operations
    }
    
    public void put(K key, V value) {
        cache.put(key, value);
    }
}
\`\`\`

**Problems with Approach 1:**
- Coarse-grained locking blocks all operations
- Poor scalability under high concurrency
- No control over eviction callbacks

**Approach 2: Custom Implementation with ReadWriteLock**
\`\`\`java
public class ReadWriteLRUCache<K, V> {
    private final int capacity;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<K, Node<K, V>> map = new HashMap<>();
    private final Node<K, V> head = new Node<>(null, null);
    private final Node<K, V> tail = new Node<>(null, null);
    
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev, next;
        
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    public ReadWriteLRUCache(int capacity) {
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }
    
    public V get(K key) {
        lock.readLock().lock();
        try {
            Node<K, V> node = map.get(key);
            if (node == null) return null;
            
            // Need to upgrade to write lock for LRU update
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                // Double-check after lock upgrade
                node = map.get(key);
                if (node != null) {
                    moveToHead(node);
                    return node.value;
                }
                return null;
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            Node<K, V> existing = map.get(key);
            if (existing != null) {
                existing.value = value;
                moveToHead(existing);
                return;
            }
            
            Node<K, V> newNode = new Node<>(key, value);
            map.put(key, newNode);
            addToHead(newNode);
            
            if (map.size() > capacity) {
                Node<K, V> tail = removeTail();
                map.remove(tail.key);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }
    
    private void addToHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }
    
    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    private Node<K, V> removeTail() {
        Node<K, V> lastNode = tail.prev;
        removeNode(lastNode);
        return lastNode;
    }
}
\`\`\`

**Approach 3: Lock-Free with ConcurrentHashMap + Custom LRU**
\`\`\`java
public class ConcurrentLRUCache<K, V> {
    private final int capacity;
    private final ConcurrentHashMap<K, Node<K, V>> map;
    private final AtomicReference<Node<K, V>> head = new AtomicReference<>();
    private final AtomicReference<Node<K, V>> tail = new AtomicReference<>();
    private final AtomicInteger size = new AtomicInteger(0);
    
    private static class Node<K, V> {
        final K key;
        volatile V value;
        volatile Node<K, V> prev;
        volatile Node<K, V> next;
        volatile boolean deleted = false;
        
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    public ConcurrentLRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new ConcurrentHashMap<>(capacity);
        
        Node<K, V> headNode = new Node<>(null, null);
        Node<K, V> tailNode = new Node<>(null, null);
        headNode.next = tailNode;
        tailNode.prev = headNode;
        head.set(headNode);
        tail.set(tailNode);
    }
    
    public V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null || node.deleted) {
            return null;
        }
        
        // Move to head (lock-free approach)
        moveToHead(node);
        return node.value;
    }
    
    public V put(K key, V value) {
        Node<K, V> newNode = new Node<>(key, value);
        Node<K, V> existing = map.putIfAbsent(key, newNode);
        
        if (existing != null) {
            // Update existing
            V oldValue = existing.value;
            existing.value = value;
            moveToHead(existing);
            return oldValue;
        }
        
        // New node
        addToHead(newNode);
        
        if (size.incrementAndGet() > capacity) {
            evictLRU();
        }
        
        return null;
    }
    
    private void moveToHead(Node<K, V> node) {
        if (node.deleted) return;
        
        // Remove from current position
        Node<K, V> prevNode = node.prev;
        Node<K, V> nextNode = node.next;
        
        if (prevNode != null) prevNode.next = nextNode;
        if (nextNode != null) nextNode.prev = prevNode;
        
        // Add to head
        addToHead(node);
    }
    
    private void addToHead(Node<K, V> node) {
        Node<K, V> headNode = head.get();
        Node<K, V> firstNode = headNode.next;
        
        node.prev = headNode;
        node.next = firstNode;
        headNode.next = node;
        if (firstNode != null) {
            firstNode.prev = node;
        }
    }
    
    private void evictLRU() {
        Node<K, V> tailNode = tail.get();
        Node<K, V> lastNode = tailNode.prev;
        
        if (lastNode != null && lastNode != head.get()) {
            lastNode.deleted = true;
            map.remove(lastNode.key);
            
            Node<K, V> prevNode = lastNode.prev;
            if (prevNode != null) {
                prevNode.next = tailNode;
                tailNode.prev = prevNode;
            }
            
            size.decrementAndGet();
        }
    }
}
\`\`\`

**Approach 4: Production-Ready with Caffeine-style Implementation**
\`\`\`java
public class ProductionLRUCache<K, V> {
    private final int capacity;
    private final ConcurrentHashMap<K, V> data;
    private final ConcurrentLinkedDeque<K> accessOrder;
    private final ReentrantLock evictionLock = new ReentrantLock();
    
    public ProductionLRUCache(int capacity) {
        this.capacity = capacity;
        this.data = new ConcurrentHashMap<>(capacity);
        this.accessOrder = new ConcurrentLinkedDeque<>();
    }
    
    public V get(K key) {
        V value = data.get(key);
        if (value != null) {
            // Record access
            accessOrder.remove(key);
            accessOrder.offer(key);
        }
        return value;
    }
    
    public V put(K key, V value) {
        V existing = data.put(key, value);
        
        if (existing == null) {
            accessOrder.offer(key);
            
            if (data.size() > capacity) {
                evictIfNeeded();
            }
        } else {
            // Update access order
            accessOrder.remove(key);
            accessOrder.offer(key);
        }
        
        return existing;
    }
    
    private void evictIfNeeded() {
        if (evictionLock.tryLock()) {
            try {
                while (data.size() > capacity) {
                    K lru = accessOrder.poll();
                    if (lru != null) {
                        data.remove(lru);
                    }
                }
            } finally {
                evictionLock.unlock();
            }
        }
    }
}
\`\`\`

**Performance Comparison:**

| Approach | Throughput | Latency | Memory | Complexity |
|----------|------------|---------|---------|------------|
| Synchronized | Low | High | Low | Low |
| ReadWrite Lock | Medium | Medium | Medium | Medium |
| Lock-Free | High | Low | High | High |
| Production | High | Low | Medium | Medium |

**Recommendation:**
Use approach 4 for production systems - it provides good balance of performance, correctness, and maintainability.`,
        tags: ['lru-cache', 'concurrency', 'data-structures', 'performance', 'thread-safety'],
        followUp: [
          'How would you implement cache warming strategies?',
          'What metrics would you track for cache performance?',
          'How would you handle cache coherence in distributed systems?'
        ]
      }
    ]
  },
  {
    id: 'multithreading',
    title: 'Multithreading & Concurrency',
    description: 'Advanced concurrency patterns, thread safety, and performance optimization.',
    questions: []
  },
  {
    id: 'jvm-internals',
    title: 'JVM Internals & Memory Management',
    description: 'Deep dive into JVM architecture, garbage collection, and memory optimization.',
    questions: []
  },
  {
    id: 'performance-optimization',
    title: 'Performance Optimization',
    description: 'Profiling, optimization techniques, and performance best practices.',
    questions: []
  },
  {
    id: 'security',
    title: 'Security & Best Practices',
    description: 'Security vulnerabilities, secure coding practices, and compliance.',
    questions: []
  },
  {
    id: 'web-technologies',
    title: 'Web Technologies & Frameworks',
    description: 'Spring Framework, REST APIs, microservices, and web security.',
    questions: []
  }
]

// Helper function to get all questions
export const getAllQuestions = () => {
  return chapters.reduce((acc, chapter) => {
    return acc.concat(chapter.questions.map(q => ({ ...q, chapterId: chapter.id, chapterTitle: chapter.title })))
  }, [])
}

// Helper function to get question by ID
export const getQuestionById = (questionId) => {
  for (const chapter of chapters) {
    const question = chapter.questions.find(q => q.id === questionId)
    if (question) {
      return { ...question, chapterId: chapter.id, chapterTitle: chapter.title }
    }
  }
  return null
}

// Helper function to get chapter by ID
export const getChapterById = (chapterId) => {
  return chapters.find(chapter => chapter.id === chapterId)
}
      {
        id: 'jf-007',
        question: 'Explain the difference between `String`, `StringBuilder`, and `StringBuffer`. When would you use each in a high-performance application?',
        difficulty: 'intermediate',
        category: 'String Handling',
        scenario: 'Log processing system handling millions of log entries per minute',
        answer: `String manipulation is critical for performance in high-throughput applications. Understanding the differences can prevent major performance bottlenecks.

**Key Differences:**

| Feature | String | StringBuilder | StringBuffer |
|---------|--------|---------------|--------------|
| Mutability | Immutable | Mutable | Mutable |
| Thread Safety | Thread-safe | Not thread-safe | Thread-safe |
| Performance | Slow for concatenation | Fast | Moderate |
| Memory | Creates new objects | Reuses buffer | Reuses buffer |

**1. String - Immutable and Thread-Safe:**

\`\`\`java
public class StringPerformanceDemo {
    // POOR PERFORMANCE - Creates many objects
    public String processLogsBadly(List<String> logEntries) {
        String result = "";
        for (String entry : logEntries) {
            result += entry + "\\n"; // Creates new String object each time
        }
        return result;
    }
    
    // This creates O(n²) objects and has O(n²) time complexity
    // For 10,000 entries, this creates ~50 million objects!
}
\`\`\`

**2. StringBuilder - Mutable and Fast:**

\`\`\`java
public class LogProcessor {
    // GOOD PERFORMANCE - Single thread
    public String processLogsEfficiently(List<String> logEntries) {
        StringBuilder sb = new StringBuilder(logEntries.size() * 100); // Pre-size buffer
        
        for (String entry : logEntries) {
            sb.append(entry).append("\\n");
        }
        
        return sb.toString();
    }
    
    // Advanced usage with capacity management
    public String processLargeDataset(Stream<String> dataStream) {
        StringBuilder sb = new StringBuilder(8192); // 8KB initial capacity
        
        dataStream.forEach(data -> {
            // Check if we need to expand capacity
            if (sb.length() + data.length() > sb.capacity() * 0.75) {
                sb.ensureCapacity(sb.capacity() * 2);
            }
            sb.append(data).append("\\n");
        });
        
        return sb.toString();
    }
}
\`\`\`

**3. StringBuffer - Thread-Safe but Slower:**

\`\`\`java
public class ConcurrentLogProcessor {
    private final StringBuffer sharedBuffer = new StringBuffer();
    
    // Thread-safe but slower due to synchronization
    public void appendLogEntry(String entry) {
        synchronized(sharedBuffer) { // Actually redundant - StringBuffer is already synchronized
            sharedBuffer.append(entry).append("\\n");
        }
    }
    
    // Better approach for concurrent scenarios
    public String processLogsConcurrently(List<String> logEntries) {
        // Use ThreadLocal StringBuilder for better performance
        ThreadLocal<StringBuilder> localBuilder = ThreadLocal.withInitial(
            () -> new StringBuilder(1024)
        );
        
        return logEntries.parallelStream()
            .collect(
                () -> localBuilder.get().setLength(0), // Reset for reuse
                (sb, entry) -> sb.append(entry).append("\\n"),
                (sb1, sb2) -> sb1.append(sb2)
            ).toString();
    }
}
\`\`\`

**High-Performance Log Processing System:**

\`\`\`java
@Component
public class HighPerformanceLogProcessor {
    private static final int BUFFER_SIZE = 8192;
    private static final int BATCH_SIZE = 1000;
    
    // Pool of StringBuilder objects to avoid allocation overhead
    private final ObjectPool<StringBuilder> stringBuilderPool = 
        new GenericObjectPool<>(new StringBuilderPooledObjectFactory());
    
    public String processLogBatch(List<LogEntry> entries) {
        StringBuilder sb = null;
        try {
            sb = stringBuilderPool.borrowObject();
            sb.setLength(0); // Reset for reuse
            
            for (LogEntry entry : entries) {
                formatLogEntry(sb, entry);
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to process log batch", e);
        } finally {
            if (sb != null) {
                try {
                    stringBuilderPool.returnObject(sb);
                } catch (Exception e) {
                    // Log error but don't fail the operation
                    logger.warn("Failed to return StringBuilder to pool", e);
                }
            }
        }
    }
    
    private void formatLogEntry(StringBuilder sb, LogEntry entry) {
        sb.append(entry.getTimestamp())
          .append(" [").append(entry.getLevel()).append("] ")
          .append(entry.getLogger())
          .append(" - ").append(entry.getMessage());
        
        if (entry.getException() != null) {
            sb.append("\\n").append(getStackTrace(entry.getException()));
        }
        
        sb.append("\\n");
    }
    
    // Custom pooled object factory
    private static class StringBuilderPooledObjectFactory 
            extends BasePooledObjectFactory<StringBuilder> {
        
        @Override
        public StringBuilder create() {
            return new StringBuilder(BUFFER_SIZE);
        }
        
        @Override
        public PooledObject<StringBuilder> wrap(StringBuilder sb) {
            return new DefaultPooledObject<>(sb);
        }
        
        @Override
        public void passivateObject(PooledObject<StringBuilder> pooledObject) {
            // Reset but keep capacity for reuse
            pooledObject.getObject().setLength(0);
        }
    }
}
\`\`\`

**Performance Comparison:**

\`\`\`java
@Component
public class StringPerformanceBenchmark {
    
    @Benchmark
    public String stringConcatenation(int iterations) {
        String result = "";
        for (int i = 0; i < iterations; i++) {
            result += "item" + i + "\\n";
        }
        return result;
    }
    
    @Benchmark
    public String stringBuilderAppend(int iterations) {
        StringBuilder sb = new StringBuilder(iterations * 10);
        for (int i = 0; i < iterations; i++) {
            sb.append("item").append(i).append("\\n");
        }
        return sb.toString();
    }
    
    @Benchmark
    public String stringBufferAppend(int iterations) {
        StringBuffer sb = new StringBuffer(iterations * 10);
        for (int i = 0; i < iterations; i++) {
            sb.append("item").append(i).append("\\n");
        }
        return sb.toString();
    }
}

/*
Benchmark Results (1000 iterations):
String concatenation:     ~50ms, ~500MB memory
StringBuilder:           ~0.1ms, ~10KB memory  
StringBuffer:            ~0.2ms, ~10KB memory
*/
\`\`\`

**Best Practices for High-Performance Applications:**

**1. Choose the Right Tool:**
- **String**: For immutable text, small concatenations
- **StringBuilder**: Single-threaded, high-performance concatenation
- **StringBuffer**: Multi-threaded scenarios (rare)

**2. Optimization Techniques:**
- Pre-size buffers when possible
- Use object pooling for frequent operations
- Consider ThreadLocal for concurrent scenarios
- Use streaming APIs for large datasets

**3. Memory Management:**
- Monitor string pool usage
- Avoid creating unnecessary intermediate strings
- Use \`intern()\` carefully - can cause memory leaks
- Consider off-heap storage for very large strings

**4. Production Monitoring:**
- Track string allocation rates
- Monitor GC pressure from string operations
- Use profilers to identify string bottlenecks
- Implement metrics for string processing performance`,
        tags: ['string', 'stringbuilder', 'stringbuffer', 'performance', 'memory', 'concatenation'],
        followUp: [
          'How does string interning affect memory usage in long-running applications?',
          'What are the trade-offs of using object pooling for StringBuilder?',
          'How would you handle very large string operations that don\'t fit in memory?'
        ]
      },
      {
        id: 'jf-008',
        question: 'Design a robust equals() and hashCode() implementation for a complex business object. Explain the contract between these methods and the implications of violating it.',
        difficulty: 'expert',
        category: 'Object Methods',
        scenario: 'Financial trading system with complex order objects used in HashMaps and Sets',
        answer: `The equals() and hashCode() contract is fundamental to Java collections. Violating it can cause subtle bugs that are extremely difficult to debug in production systems.

**The Contract:**
1. If \`a.equals(b)\` returns true, then \`a.hashCode() == b.hashCode()\`
2. If \`a.hashCode() != b.hashCode()\`, then \`a.equals(b)\` must return false
3. hashCode() must be consistent during object lifetime
4. equals() must be reflexive, symmetric, transitive, and consistent

**Complex Business Object - Trading Order:**

\`\`\`java
public class TradingOrder {
    private final String orderId;
    private final String symbol;
    private final OrderType orderType;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final Instant timestamp;
    private final String traderId;
    
    // Mutable fields that don't affect equality
    private OrderStatus status;
    private BigDecimal filledQuantity;
    private List<Execution> executions;
    
    public TradingOrder(String orderId, String symbol, OrderType orderType, 
                       BigDecimal quantity, BigDecimal price, String traderId) {
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.symbol = Objects.requireNonNull(symbol, "Symbol cannot be null");
        this.orderType = Objects.requireNonNull(orderType, "Order type cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
        this.price = price; // Can be null for market orders
        this.traderId = Objects.requireNonNull(traderId, "Trader ID cannot be null");
        this.timestamp = Instant.now();
        this.status = OrderStatus.PENDING;
        this.filledQuantity = BigDecimal.ZERO;
        this.executions = new ArrayList<>();
    }
    
    @Override
    public boolean equals(Object obj) {
        // 1. Reference equality check (performance optimization)
        if (this == obj) {
            return true;
        }
        
        // 2. Null check
        if (obj == null) {
            return false;
        }
        
        // 3. Type check (handles inheritance correctly)
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        TradingOrder other = (TradingOrder) obj;
        
        // 4. Field comparison - only immutable business-significant fields
        return Objects.equals(orderId, other.orderId) &&
               Objects.equals(symbol, other.symbol) &&
               Objects.equals(orderType, other.orderType) &&
               Objects.equals(quantity, other.quantity) &&
               Objects.equals(price, other.price) &&
               Objects.equals(traderId, other.traderId);
        
        // Note: timestamp, status, filledQuantity, executions are NOT included
        // because they don't define business equality
    }
    
    @Override
    public int hashCode() {
        // Use the same fields as equals()
        return Objects.hash(orderId, symbol, orderType, quantity, price, traderId);
    }
    
    // Additional methods for debugging
    @Override
    public String toString() {
        return String.format("TradingOrder{orderId='%s', symbol='%s', type=%s, qty=%s, price=%s, trader='%s', status=%s}",
            orderId, symbol, orderType, quantity, price, traderId, status);
    }
}
\`\`\`

**Advanced Scenarios and Edge Cases:**

\`\`\`java
// 1. Handling BigDecimal precision issues
public class PriceAwareOrder extends TradingOrder {
    private static final int PRICE_SCALE = 4; // 4 decimal places
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        
        PriceAwareOrder other = (PriceAwareOrder) obj;
        
        // Handle BigDecimal comparison with scale normalization
        BigDecimal thisPrice = normalizePrice(this.getPrice());
        BigDecimal otherPrice = normalizePrice(other.getPrice());
        
        return Objects.equals(thisPrice, otherPrice);
    }
    
    @Override
    public int hashCode() {
        // Normalize price for consistent hashing
        BigDecimal normalizedPrice = normalizePrice(getPrice());
        return Objects.hash(getOrderId(), getSymbol(), getOrderType(), 
                          getQuantity(), normalizedPrice, getTraderId());
    }
    
    private BigDecimal normalizePrice(BigDecimal price) {
        return price == null ? null : price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }
}

// 2. Immutable order with builder pattern
public final class ImmutableTradingOrder {
    private final String orderId;
    private final String symbol;
    private final OrderType orderType;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final String traderId;
    private final Instant timestamp;
    
    // Cached hash code for performance
    private final int hashCode;
    
    private ImmutableTradingOrder(Builder builder) {
        this.orderId = builder.orderId;
        this.symbol = builder.symbol;
        this.orderType = builder.orderType;
        this.quantity = builder.quantity;
        this.price = builder.price;
        this.traderId = builder.traderId;
        this.timestamp = builder.timestamp;
        
        // Pre-compute hash code since object is immutable
        this.hashCode = Objects.hash(orderId, symbol, orderType, quantity, price, traderId);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ImmutableTradingOrder other = (ImmutableTradingOrder) obj;
        
        // Quick hash code check for performance
        if (this.hashCode != other.hashCode) {
            return false;
        }
        
        return Objects.equals(orderId, other.orderId) &&
               Objects.equals(symbol, other.symbol) &&
               Objects.equals(orderType, other.orderType) &&
               Objects.equals(quantity, other.quantity) &&
               Objects.equals(price, other.price) &&
               Objects.equals(traderId, other.traderId);
    }
    
    @Override
    public int hashCode() {
        return hashCode; // Return pre-computed value
    }
    
    public static class Builder {
        private String orderId;
        private String symbol;
        private OrderType orderType;
        private BigDecimal quantity;
        private BigDecimal price;
        private String traderId;
        private Instant timestamp = Instant.now();
        
        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }
        
        // Other builder methods...
        
        public ImmutableTradingOrder build() {
            validateRequiredFields();
            return new ImmutableTradingOrder(this);
        }
        
        private void validateRequiredFields() {
            Objects.requireNonNull(orderId, "Order ID is required");
            Objects.requireNonNull(symbol, "Symbol is required");
            Objects.requireNonNull(orderType, "Order type is required");
            Objects.requireNonNull(quantity, "Quantity is required");
            Objects.requireNonNull(traderId, "Trader ID is required");
        }
    }
}
\`\`\`

**Production Issues from Contract Violations:**

\`\`\`java
@Component
public class OrderManagementService {
    private final Map<TradingOrder, OrderMetadata> orderMetadata = new HashMap<>();
    private final Set<TradingOrder> activeOrders = new HashSet<>();
    
    public void demonstrateContractViolations() {
        // VIOLATION EXAMPLE 1: Mutable fields in equals/hashCode
        TradingOrder order = new TradingOrder("ORD001", "AAPL", OrderType.BUY, 
                                            new BigDecimal("100"), new BigDecimal("150.00"), "TRADER1");
        
        // Add to collections
        activeOrders.add(order);
        orderMetadata.put(order, new OrderMetadata("metadata"));
        
        // Modify mutable field that affects hashCode (BAD DESIGN)
        // order.setStatus(OrderStatus.FILLED); // This would break collections!
        
        // Now the order might not be found in collections
        boolean found = activeOrders.contains(order); // Might return false!
        OrderMetadata metadata = orderMetadata.get(order); // Might return null!
        
        // VIOLATION EXAMPLE 2: Inconsistent equals/hashCode
        BadOrder bad1 = new BadOrder("ID1");
        BadOrder bad2 = new BadOrder("ID1");
        
        System.out.println(bad1.equals(bad2)); // true
        System.out.println(bad1.hashCode() == bad2.hashCode()); // false - VIOLATION!
        
        Set<BadOrder> badSet = new HashSet<>();
        badSet.add(bad1);
        System.out.println(badSet.contains(bad2)); // false - even though equals() returns true!
    }
    
    // Example of BAD implementation
    static class BadOrder {
        private String id;
        
        BadOrder(String id) { this.id = id; }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BadOrder) {
                return Objects.equals(id, ((BadOrder) obj).id);
            }
            return false;
        }
        
        // MISSING hashCode() - uses Object.hashCode() which violates contract!
    }
}
\`\`\`

**Performance Optimizations:**

\`\`\`java
public class OptimizedTradingOrder {
    // Lazy-computed hash code for expensive calculations
    private transient volatile int hashCode = 0;
    
    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = computeHashCode();
            hashCode = result;
        }
        return result;
    }
    
    private int computeHashCode() {
        // Expensive computation here
        return Objects.hash(orderId, symbol, orderType, quantity, price, traderId);
    }
    
    // Fast path for equals when hash codes differ
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        OptimizedTradingOrder other = (OptimizedTradingOrder) obj;
        
        // Quick rejection if hash codes are different
        if (this.hashCode() != other.hashCode()) {
            return false;
        }
        
        // Expensive field comparison only if hash codes match
        return Objects.equals(orderId, other.orderId) &&
               Objects.equals(symbol, other.symbol) &&
               Objects.equals(orderType, other.orderType) &&
               Objects.equals(quantity, other.quantity) &&
               Objects.equals(price, other.price) &&
               Objects.equals(traderId, other.traderId);
    }
}
\`\`\`

**Testing the Contract:**

\`\`\`java
@Test
public class TradingOrderContractTest {
    
    @Test
    public void testEqualsHashCodeContract() {
        TradingOrder order1 = createOrder("ORD001");
        TradingOrder order2 = createOrder("ORD001");
        TradingOrder order3 = createOrder("ORD002");
        
        // Reflexive: x.equals(x) must be true
        assertTrue(order1.equals(order1));
        
        // Symmetric: x.equals(y) == y.equals(x)
        assertEquals(order1.equals(order2), order2.equals(order1));
        
        // Transitive: if x.equals(y) and y.equals(z), then x.equals(z)
        TradingOrder order2Copy = createOrder("ORD001");
        assertTrue(order1.equals(order2));
        assertTrue(order2.equals(order2Copy));
        assertTrue(order1.equals(order2Copy));
        
        // Consistent: multiple calls return same result
        assertEquals(order1.equals(order2), order1.equals(order2));
        
        // Null: x.equals(null) must be false
        assertFalse(order1.equals(null));
        
        // Hash code contract: if equals, then same hash code
        if (order1.equals(order2)) {
            assertEquals(order1.hashCode(), order2.hashCode());
        }
        
        // Hash code consistency
        int hash1 = order1.hashCode();
        int hash2 = order1.hashCode();
        assertEquals(hash1, hash2);
    }
    
    @Test
    public void testCollectionBehavior() {
        TradingOrder order1 = createOrder("ORD001");
        TradingOrder order2 = createOrder("ORD001"); // Equal to order1
        
        Set<TradingOrder> orders = new HashSet<>();
        orders.add(order1);
        
        // Should find equal object
        assertTrue(orders.contains(order2));
        
        // Should not add duplicate
        orders.add(order2);
        assertEquals(1, orders.size());
        
        // Map behavior
        Map<TradingOrder, String> orderMap = new HashMap<>();
        orderMap.put(order1, "metadata1");
        
        // Should retrieve with equal key
        assertEquals("metadata1", orderMap.get(order2));
    }
}
\`\`\`

**Key Takeaways:**

1. **Only use immutable, business-significant fields** in equals/hashCode
2. **Always implement both methods together**
3. **Pre-compute hash codes for immutable objects**
4. **Use quick rejection techniques for performance**
5. **Test the contract thoroughly**
6. **Consider using IDE-generated or library implementations** (Lombok, AutoValue)`,
        tags: ['equals', 'hashcode', 'contract', 'collections', 'performance', 'immutable'],
        followUp: [
          'How would you handle equals/hashCode in inheritance hierarchies?',
          'What are the performance implications of different hash code algorithms?',
          'How do you test equals/hashCode implementations comprehensively?'
        ]
      },
      {
        id: 'jf-009',
        question: 'Implement a custom ClassLoader for a plugin system. Explain class loading delegation model and how to avoid common pitfalls like memory leaks and ClassCastExceptions.',
        difficulty: 'expert',
        category: 'ClassLoading',
        scenario: 'Enterprise application server with hot-deployable plugins and dynamic class reloading',
        answer: `ClassLoaders are fundamental to Java's runtime behavior. Understanding them is crucial for building plugin systems, application servers, and avoiding subtle runtime issues.

**Class Loading Delegation Model:**
1. **Bootstrap ClassLoader** - loads core Java classes
2. **Extension ClassLoader** - loads extension classes  
3. **Application ClassLoader** - loads application classes
4. **Custom ClassLoaders** - load specific classes

**Custom Plugin ClassLoader Implementation:**

\`\`\`java
public class PluginClassLoader extends URLClassLoader {
    private final String pluginName;
    private final Set<String> loadedClasses = ConcurrentHashMap.newKeySet();
    private final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    private volatile boolean closed = false;
    
    // Parent-first or child-first delegation
    private final boolean parentFirst;
    
    public PluginClassLoader(String pluginName, URL[] urls, ClassLoader parent, boolean parentFirst) {
        super(urls, parent);
        this.pluginName = pluginName;
        this.parentFirst = parentFirst;
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            if (closed) {
                throw new ClassNotFoundException("ClassLoader is closed: " + name);
            }
            
            // Check if already loaded
            Class<?> clazz = findLoadedClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
            
            // Check cache
            clazz = classCache.get(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
            
            // Delegation strategy
            if (parentFirst) {
                return loadClassParentFirst(name, resolve);
            } else {
                return loadClassChildFirst(name, resolve);
            }
        }
    }
    
    private Class<?> loadClassParentFirst(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;
        
        // 1. Try parent first (standard delegation)
        try {
            clazz = getParent().loadClass(name);
        } catch (ClassNotFoundException e) {
            // Parent couldn't load, try ourselves
        }
        
        // 2. Try to load from our URLs
        if (clazz == null) {
            try {
                clazz = findClass(name);
                loadedClasses.add(name);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Class not found: " + name);
            }
        }
        
        classCache.put(name, clazz);
        
        if (resolve) {
            resolveClass(clazz);
        }
        
        return clazz;
    }
    
    private Class<?> loadClassChildFirst(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;
        
        // Skip system classes - always delegate to parent
        if (isSystemClass(name)) {
            return super.loadClass(name, resolve);
        }
        
        // 1. Try to load from our URLs first
        try {
            clazz = findClass(name);
            loadedClasses.add(name);
        } catch (ClassNotFoundException e) {
            // We couldn't load, try parent
            try {
                clazz = getParent().loadClass(name);
            } catch (ClassNotFoundException e2) {
                throw new ClassNotFoundException("Class not found: " + name);
            }
        }
        
        classCache.put(name, clazz);
        
        if (resolve) {
            resolveClass(clazz);
        }
        
        return clazz;
    }
    
    private boolean isSystemClass(String name) {
        return name.startsWith("java.") || 
               name.startsWith("javax.") || 
               name.startsWith("sun.") ||
               name.startsWith("com.sun.");
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            // Log for debugging
            logger.debug("Class not found in plugin {}: {}", pluginName, name);
            throw e;
        }
    }
    
    // Resource loading with proper isolation
    @Override
    public URL getResource(String name) {
        if (parentFirst) {
            URL resource = getParent().getResource(name);
            if (resource != null) {
                return resource;
            }
            return findResource(name);
        } else {
            URL resource = findResource(name);
            if (resource != null) {
                return resource;
            }
            return getParent().getResource(name);
        }
    }
    
    // Proper cleanup to prevent memory leaks
    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        
        closed = true;
        
        try {
            // Clear caches
            classCache.clear();
            loadedClasses.clear();
            
            // Close underlying resources
            super.close();
            
            logger.info("Plugin ClassLoader closed: {}", pluginName);
            
        } catch (IOException e) {
            logger.error("Error closing plugin ClassLoader: " + pluginName, e);
            throw e;
        }
    }
    
    // Debugging and monitoring
    public Set<String> getLoadedClasses() {
        return new HashSet<>(loadedClasses);
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public boolean isClosed() {
        return closed;
    }
}
\`\`\`

**Plugin Management System:**

\`\`\`java
@Component
public class PluginManager {
    private final Map<String, PluginContext> plugins = new ConcurrentHashMap<>();
    private final ExecutorService pluginExecutor = Executors.newCachedThreadPool();
    
    private static class PluginContext {
        final PluginClassLoader classLoader;
        final Plugin pluginInstance;
        final PluginMetadata metadata;
        volatile boolean active;
        
        PluginContext(PluginClassLoader classLoader, Plugin pluginInstance, PluginMetadata metadata) {
            this.classLoader = classLoader;
            this.pluginInstance = pluginInstance;
            this.metadata = metadata;
            this.active = true;
        }
    }
    
    public void loadPlugin(String pluginName, Path pluginJar) throws PluginException {
        try {
            // Create isolated ClassLoader
            URL[] urls = {pluginJar.toUri().toURL()};
            PluginClassLoader classLoader = new PluginClassLoader(
                pluginName, 
                urls, 
                getClass().getClassLoader(), 
                false // child-first for plugin isolation
            );
            
            // Load plugin metadata
            PluginMetadata metadata = loadPluginMetadata(classLoader);
            
            // Load main plugin class
            Class<?> pluginClass = classLoader.loadClass(metadata.getMainClass());
            
            // Verify plugin interface
            if (!Plugin.class.isAssignableFrom(pluginClass)) {
                throw new PluginException("Plugin class must implement Plugin interface: " + metadata.getMainClass());
            }
            
            // Create plugin instance
            Plugin pluginInstance = (Plugin) pluginClass.getDeclaredConstructor().newInstance();
            
            // Initialize plugin in separate thread to avoid blocking
            CompletableFuture<Void> initFuture = CompletableFuture.runAsync(() -> {
                try {
                    pluginInstance.initialize();
                } catch (Exception e) {
                    throw new RuntimeException("Plugin initialization failed", e);
                }
            }, pluginExecutor);
            
            // Wait for initialization with timeout
            initFuture.get(30, TimeUnit.SECONDS);
            
            // Register plugin
            PluginContext context = new PluginContext(classLoader, pluginInstance, metadata);
            plugins.put(pluginName, context);
            
            logger.info("Plugin loaded successfully: {}", pluginName);
            
        } catch (Exception e) {
            throw new PluginException("Failed to load plugin: " + pluginName, e);
        }
    }
    
    public void unloadPlugin(String pluginName) throws PluginException {
        PluginContext context = plugins.remove(pluginName);
        if (context == null) {
            throw new PluginException("Plugin not found: " + pluginName);
        }
        
        try {
            context.active = false;
            
            // Shutdown plugin
            context.pluginInstance.shutdown();
            
            // Close ClassLoader to release resources
            context.classLoader.close();
            
            // Force garbage collection to clean up classes
            System.gc();
            
            logger.info("Plugin unloaded: {}", pluginName);
            
        } catch (Exception e) {
            throw new PluginException("Failed to unload plugin: " + pluginName, e);
        }
    }
    
    // Hot reload functionality
    public void reloadPlugin(String pluginName, Path newPluginJar) throws PluginException {
        unloadPlugin(pluginName);
        loadPlugin(pluginName, newPluginJar);
    }
    
    // Safe plugin execution with proper context
    public <T> T executeInPluginContext(String pluginName, Callable<T> task) throws Exception {
        PluginContext context = plugins.get(pluginName);
        if (context == null || !context.active) {
            throw new PluginException("Plugin not available: " + pluginName);
        }
        
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Set plugin ClassLoader as context
            Thread.currentThread().setContextClassLoader(context.classLoader);
            return task.call();
        } finally {
            // Restore original ClassLoader
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    private PluginMetadata loadPluginMetadata(PluginClassLoader classLoader) throws IOException {
        try (InputStream is = classLoader.getResourceAsStream("plugin.properties")) {
            if (is == null) {
                throw new IOException("plugin.properties not found");
            }
            
            Properties props = new Properties();
            props.load(is);
            
            return new PluginMetadata(
                props.getProperty("name"),
                props.getProperty("version"),
                props.getProperty("mainClass"),
                props.getProperty("description")
            );
        }
    }
}
\`\`\`

**Avoiding Common Pitfalls:**

\`\`\`java
// 1. Memory Leak Prevention
public class MemoryLeakPreventionExample {
    
    // BAD: Static references prevent ClassLoader GC
    private static final Map<String, Object> staticCache = new HashMap<>();
    
    // GOOD: Use WeakReferences for caches
    private static final Map<String, WeakReference<Object>> weakCache = new ConcurrentHashMap<>();
    
    // BAD: ThreadLocal not cleaned up
    private static final ThreadLocal<PluginContext> pluginContext = new ThreadLocal<>();
    
    // GOOD: Proper ThreadLocal cleanup
    private static final ThreadLocal<PluginContext> safePluginContext = new ThreadLocal<PluginContext>() {
        @Override
        protected void finalize() throws Throwable {
            remove(); // Clean up on GC
            super.finalize();
        }
    };
    
    public void demonstrateProperCleanup() {
        try {
            safePluginContext.set(getCurrentPluginContext());
            // Use plugin context
        } finally {
            safePluginContext.remove(); // Always clean up
        }
    }
}

// 2. ClassCastException Prevention
public class ClassCastPreventionExample {
    
    // BAD: Direct casting across ClassLoaders
    public void badCasting(Object pluginObject) {
        // This will fail if pluginObject was loaded by different ClassLoader
        MyInterface obj = (MyInterface) pluginObject; // ClassCastException!
    }
    
    // GOOD: Use reflection or common interfaces
    public void safeCasting(Object pluginObject) {
        if (isInstanceOf(pluginObject, "com.example.MyInterface")) {
            // Use reflection to invoke methods
            Method method = pluginObject.getClass().getMethod("doSomething");
            method.invoke(pluginObject);
        }
    }
    
    private boolean isInstanceOf(Object obj, String className) {
        try {
            Class<?> clazz = obj.getClass().getClassLoader().loadClass(className);
            return clazz.isInstance(obj);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    // BETTER: Use common parent ClassLoader for shared interfaces
    public interface PluginInterface {
        void execute();
    }
    
    // Load shared interfaces in parent ClassLoader
    public void properInterfaceUsage(PluginInterface plugin) {
        plugin.execute(); // Safe - interface loaded by parent
    }
}
\`\`\`

**Testing and Monitoring:**

\`\`\`java
@Component
public class ClassLoaderMonitor {
    
    @EventListener
    @Async
    public void onPluginLoaded(PluginLoadedEvent event) {
        monitorClassLoaderMemory(event.getPluginName());
    }
    
    private void monitorClassLoaderMemory(String pluginName) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage metaspaceUsage = memoryBean.getNonHeapMemoryUsage();
        
        logger.info("Metaspace usage after loading {}: {} MB", 
                   pluginName, 
                   metaspaceUsage.getUsed() / (1024 * 1024));
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void checkForClassLoaderLeaks() {
        // Monitor for ClassLoader instances that should have been GC'd
        int classLoaderCount = 0;
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            // Check for metaspace pressure
            if (gcBean.getName().contains("Metaspace")) {
                long collections = gcBean.getCollectionCount();
                long time = gcBean.getCollectionTime();
                
                if (time > 1000) { // More than 1 second spent in metaspace GC
                    logger.warn("High metaspace GC time: {} ms", time);
                }
            }
        }
    }
}
\`\`\`

**Key Takeaways:**

1. **Understand delegation models** - parent-first vs child-first
2. **Proper resource cleanup** - always close ClassLoaders
3. **Avoid static references** - use WeakReferences for caches
4. **Handle ThreadLocal cleanup** - prevent memory leaks
5. **Use common parent ClassLoaders** - for shared interfaces
6. **Monitor metaspace usage** - detect ClassLoader leaks
7. **Test hot reloading thoroughly** - ensure proper cleanup`,
        tags: ['classloader', 'plugins', 'memory-leaks', 'delegation', 'hot-reload', 'metaspace'],
        followUp: [
          'How would you implement plugin sandboxing and security?',
          'What are the performance implications of custom ClassLoaders?',
          'How do you handle plugin dependencies and version conflicts?'
        ]
      },
      {
        id: 'jf-010',
        question: 'Explain Java Reflection API and its performance implications. Implement a high-performance object mapper that uses reflection efficiently.',
        difficulty: 'expert',
        category: 'Reflection',
        scenario: 'High-throughput data processing system that converts between different object formats',
        answer: `Reflection is powerful but expensive. Understanding its performance characteristics and optimization techniques is crucial for building efficient systems.

**Reflection Performance Issues:**
1. **Method lookup overhead** - finding methods by name
2. **Security checks** - access control verification
3. **Argument boxing/unboxing** - primitive type conversions
4. **No JIT optimization** - reflective calls can't be inlined

**High-Performance Object Mapper Implementation:**

\`\`\`java
@Component
public class HighPerformanceObjectMapper {
    
    // Cache for expensive reflection operations
    private final ConcurrentHashMap<Class<?>, ClassMetadata> classCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Method> methodCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Field> fieldCache = new ConcurrentHashMap<>();
    
    // Method handles for better performance (Java 7+)
    private final ConcurrentHashMap<String, MethodHandle> methodHandleCache = new ConcurrentHashMap<>();
    
    private static class ClassMetadata {
        final Map<String, Field> fields;
        final Map<String, Method> getters;
        final Map<String, Method> setters;
        final Constructor<?> defaultConstructor;
        
        ClassMetadata(Class<?> clazz) throws Exception {
            this.fields = new HashMap<>();
            this.getters = new HashMap<>();
            this.setters = new HashMap<>();
            
            // Cache all fields
            for (Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true); // Do this once
                    fields.put(field.getName(), field);
                }
            }
            
            // Cache getter/setter methods
            for (Method method : clazz.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    method.setAccessible(true); // Do this once
                    
                    String methodName = method.getName();
                    if (methodName.startsWith("get") && method.getParameterCount() == 0) {
                        String fieldName = decapitalize(methodName.substring(3));
                        getters.put(fieldName, method);
                    } else if (methodName.startsWith("set") && method.getParameterCount() == 1) {
                        String fieldName = decapitalize(methodName.substring(3));
                        setters.put(fieldName, method);
                    }
                }
            }
            
            // Cache default constructor
            this.defaultConstructor = clazz.getDeclaredConstructor();
            this.defaultConstructor.setAccessible(true);
        }
        
        private String decapitalize(String name) {
            if (name.length() == 0) return name;
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
    }
    
    // High-performance mapping using cached metadata
    public <T> T mapObject(Object source, Class<T> targetClass) throws Exception {
        if (source == null) return null;
        
        ClassMetadata sourceMetadata = getClassMetadata(source.getClass());
        ClassMetadata targetMetadata = getClassMetadata(targetClass);
        
        // Create target instance
        T target = (T) targetMetadata.defaultConstructor.newInstance();
        
        // Map fields efficiently
        for (Map.Entry<String, Field> entry : sourceMetadata.fields.entrySet()) {
            String fieldName = entry.getKey();
            Field sourceField = entry.getValue();
            
            // Check if target has corresponding field or setter
            Field targetField = targetMetadata.fields.get(fieldName);
            Method targetSetter = targetMetadata.setters.get(fieldName);
            
            if (targetField != null || targetSetter != null) {
                Object value = sourceField.get(source);
                
                if (value != null) {
                    if (targetField != null) {
                        // Direct field access (fastest)
                        setFieldValue(targetField, target, value);
                    } else {
                        // Use setter method
                        targetSetter.invoke(target, value);
                    }
                }
            }
        }
        
        return target;
    }
    
    // Optimized field value setting with type conversion
    private void setFieldValue(Field field, Object target, Object value) throws Exception {
        Class<?> fieldType = field.getType();
        Class<?> valueType = value.getClass();
        
        if (fieldType.isAssignableFrom(valueType)) {
            // Direct assignment
            field.set(target, value);
        } else if (fieldType.isPrimitive()) {
            // Handle primitive type conversions
            setPrimitiveValue(field, target, value);
        } else {
            // Type conversion needed
            Object convertedValue = convertValue(value, fieldType);
            field.set(target, convertedValue);
        }
    }
    
    private void setPrimitiveValue(Field field, Object target, Object value) throws Exception {
        Class<?> fieldType = field.getType();
        
        if (fieldType == int.class) {
            field.setInt(target, ((Number) value).intValue());
        } else if (fieldType == long.class) {
            field.setLong(target, ((Number) value).longValue());
        } else if (fieldType == double.class) {
            field.setDouble(target, ((Number) value).doubleValue());
        } else if (fieldType == float.class) {
            field.setFloat(target, ((Number) value).floatValue());
        } else if (fieldType == boolean.class) {
            field.setBoolean(target, (Boolean) value);
        } else {
            field.set(target, value);
        }
    }
    
    private Object convertValue(Object value, Class<?> targetType) {
        if (targetType == String.class) {
            return value.toString();
        } else if (targetType == Integer.class && value instanceof Number) {
            return ((Number) value).intValue();
        } else if (targetType == Long.class && value instanceof Number) {
            return ((Number) value).longValue();
        } else if (targetType == BigDecimal.class) {
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                return new BigDecimal(value.toString());
            }
        }
        // Add more conversions as needed
        return value;
    }
    
    private ClassMetadata getClassMetadata(Class<?> clazz) throws Exception {
        return classCache.computeIfAbsent(clazz, k -> {
            try {
                return new ClassMetadata(k);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create metadata for " + k, e);
            }
        });
    }
}
\`\`\`

**Method Handles for Better Performance:**

\`\`\`java
public class MethodHandleObjectMapper {
    private final ConcurrentHashMap<String, MethodHandle> getterHandles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MethodHandle> setterHandles = new ConcurrentHashMap<>();
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    
    public <T> T mapWithMethodHandles(Object source, Class<T> targetClass) throws Throwable {
        T target = targetClass.getDeclaredConstructor().newInstance();
        
        Field[] sourceFields = source.getClass().getDeclaredFields();
        
        for (Field sourceField : sourceFields) {
            String fieldName = sourceField.getName();
            
            // Get value using MethodHandle (faster than reflection)
            MethodHandle getter = getGetterHandle(source.getClass(), fieldName);
            Object value = getter.invoke(source);
            
            if (value != null) {
                // Set value using MethodHandle
                MethodHandle setter = getSetterHandle(targetClass, fieldName, sourceField.getType());
                if (setter != null) {
                    setter.invoke(target, value);
                }
            }
        }
        
        return target;
    }
    
    private MethodHandle getGetterHandle(Class<?> clazz, String fieldName) throws Throwable {
        String key = clazz.getName() + "." + fieldName + ".get";
        
        return getterHandles.computeIfAbsent(key, k -> {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return lookup.unreflectGetter(field);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create getter handle", e);
            }
        });
    }
    
    private MethodHandle getSetterHandle(Class<?> clazz, String fieldName, Class<?> fieldType) throws Throwable {
        String key = clazz.getName() + "." + fieldName + ".set";
        
        return setterHandles.computeIfAbsent(key, k -> {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return lookup.unreflectSetter(field);
            } catch (NoSuchFieldException e) {
                // Field doesn't exist in target class
                return null;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create setter handle", e);
            }
        });
    }
}
\`\`\`

**Code Generation for Maximum Performance:**

\`\`\`java
public class CodeGeneratingMapper {
    private final ConcurrentHashMap<String, Function<Object, Object>> mapperCache = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public <T> T mapWithCodeGeneration(Object source, Class<T> targetClass) {
        String mapperKey = source.getClass().getName() + "->" + targetClass.getName();
        
        Function<Object, Object> mapper = mapperCache.computeIfAbsent(mapperKey, k -> {
            return generateMapper(source.getClass(), targetClass);
        });
        
        return (T) mapper.apply(source);
    }
    
    private <S, T> Function<Object, Object> generateMapper(Class<S> sourceClass, Class<T> targetClass) {
        // In a real implementation, you would use bytecode generation libraries
        // like ASM, Javassist, or ByteBuddy to generate optimized mapping code
        
        // For demonstration, we'll use a lambda that compiles to efficient bytecode
        return source -> {
            try {
                T target = targetClass.getDeclaredConstructor().newInstance();
                
                // This would be generated code specific to the source/target pair
                if (source instanceof User && target instanceof UserDTO) {
                    User user = (User) source;
                    UserDTO dto = (UserDTO) target;
                    
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());
                    // ... other fields
                }
                
                return target;
            } catch (Exception e) {
                throw new RuntimeException("Mapping failed", e);
            }
        };
    }
}
\`\`\`

**Performance Benchmarking:**

\`\`\`java
@Component
public class ReflectionPerformanceBenchmark {
    
    @Benchmark
    public void directAccess(Blackhole bh) {
        User user = new User("John", "john@example.com");
        UserDTO dto = new UserDTO();
        
        // Direct access (baseline)
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        
        bh.consume(dto);
    }
    
    @Benchmark
    public void reflectionAccess(Blackhole bh) throws Exception {
        User user = new User("John", "john@example.com");
        UserDTO dto = new UserDTO();
        
        // Reflection access (slow)
        Method getName = User.class.getMethod("getName");
        Method setName = UserDTO.class.getMethod("setName", String.class);
        
        String name = (String) getName.invoke(user);
        setName.invoke(dto, name);
        
        bh.consume(dto);
    }
    
    @Benchmark
    public void cachedReflectionAccess(Blackhole bh) throws Exception {
        User user = new User("John", "john@example.com");
        UserDTO dto = new UserDTO();
        
        // Cached reflection (better)
        Method getName = methodCache.get("User.getName");
        Method setName = methodCache.get("UserDTO.setName");
        
        String name = (String) getName.invoke(user);
        setName.invoke(dto, name);
        
        bh.consume(dto);
    }
    
    @Benchmark
    public void methodHandleAccess(Blackhole bh) throws Throwable {
        User user = new User("John", "john@example.com");
        UserDTO dto = new UserDTO();
        
        // MethodHandle access (fastest reflective approach)
        MethodHandle getName = methodHandleCache.get("User.getName");
        MethodHandle setName = methodHandleCache.get("UserDTO.setName");
        
        String name = (String) getName.invoke(user);
        setName.invoke(dto, name);
        
        bh.consume(dto);
    }
}
\`\`\`

**Real-World Usage Example:**

\`\`\`java
@Service
public class DataTransformationService {
    private final HighPerformanceObjectMapper mapper;
    
    public DataTransformationService(HighPerformanceObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Async
    public CompletableFuture<List<OrderDTO>> transformOrders(List<Order> orders) {
        return CompletableFuture.supplyAsync(() -> {
            return orders.parallelStream()
                .map(order -> {
                    try {
                        return mapper.mapObject(order, OrderDTO.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Mapping failed for order: " + order.getId(), e);
                    }
                })
                .collect(Collectors.toList());
        });
    }
    
    // Batch processing with performance monitoring
    public void processBatch(List<Object> sourceObjects, Class<?> targetClass) {
        long startTime = System.nanoTime();
        
        try {
            List<Object> results = sourceObjects.parallelStream()
                .map(source -> {
                    try {
                        return mapper.mapObject(source, targetClass);
                    } catch (Exception e) {
                        logger.error("Failed to map object: " + source, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            long duration = System.nanoTime() - startTime;
            double throughput = (sourceObjects.size() * 1_000_000_000.0) / duration;
            
            logger.info("Processed {} objects in {} ms, throughput: {:.2f} objects/second",
                       sourceObjects.size(),
                       duration / 1_000_000,
                       throughput);
                       
        } catch (Exception e) {
            logger.error("Batch processing failed", e);
        }
    }
}
\`\`\`

**Performance Tips:**

1. **Cache reflection metadata** - Method, Field, Constructor objects
2. **Use MethodHandles** - faster than reflection for repeated calls
3. **Avoid security checks** - call setAccessible() once during caching
4. **Consider code generation** - for maximum performance
5. **Use primitive-specific methods** - avoid boxing/unboxing
6. **Batch operations** - amortize reflection overhead
7. **Profile and measure** - reflection performance varies by JVM

**Performance Comparison:**
- Direct access: 1x (baseline)
- Cached reflection: 10-50x slower
- MethodHandles: 3-10x slower
- Code generation: 1-2x slower`,
        tags: ['reflection', 'performance', 'object-mapping', 'method-handles', 'caching', 'optimization'],
        followUp: [
          'How would you implement annotation-based mapping configuration?',
          'What are the security implications of using reflection?',
          'How do you handle circular references in object mapping?'
        ]
      },
      {
        id: 'jf-011',
        question: 'Implement a comprehensive annotation processing system for compile-time code generation. Explain the annotation processing lifecycle and common use cases.',
        difficulty: 'expert',
        category: 'Annotations',
        scenario: 'Framework development requiring compile-time validation and code generation for REST API endpoints',
        answer: `Annotation processing enables compile-time code generation and validation, which is crucial for frameworks like Spring, Hibernate, and custom tooling.

**Annotation Processing Lifecycle:**
1. **Compilation starts** - javac begins processing
2. **Annotations discovered** - processor claims annotations
3. **Processing rounds** - multiple rounds until no new files generated
4. **Code generation** - create new source files
5. **Validation** - report errors/warnings
6. **Compilation completes** - generated code compiled

**Custom Annotation Definitions:**

\`\`\`java
// REST endpoint annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE) // Only needed at compile time
public @interface RestEndpoint {
    String path();
    HttpMethod method() default HttpMethod.GET;
    String[] produces() default {"application/json"};
    String[] consumes() default {"application/json"};
    boolean requiresAuth() default true;
}

// Entity validation annotation
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ValidatedEntity {
    String tableName() default "";
    boolean generateBuilder() default false;
    boolean generateEquals() default true;
}

// Field validation annotation
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Validate {
    String[] rules();
    String message() default "";
}

// Supporting enums
public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH
}
\`\`\`

**Annotation Processor Implementation:**

\`\`\`java
@SupportedAnnotationTypes({
    "com.example.annotations.RestEndpoint",
    "com.example.annotations.ValidatedEntity",
    "com.example.annotations.Validate"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class RestApiProcessor extends AbstractProcessor {
    
    private Messager messager;
    private Filer filer;
    private Elements elementUtils;
    private Types typeUtils;
    
    // Code generation utilities
    private final Map<String, ControllerMetadata> controllers = new HashMap<>();
    private final Set<String> processedClasses = new HashSet<>();
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            // Process REST endpoints
            processRestEndpoints(roundEnv);
            
            // Process validated entities
            processValidatedEntities(roundEnv);
            
            // Generate code in final round
            if (roundEnv.processingOver()) {
                generateControllerImplementations();
                generateValidationCode();
                generateDocumentation();
            }
            
            return true; // Claim all annotations
            
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, 
                "Annotation processing failed: " + e.getMessage());
            return false;
        }
    }
    
    private void processRestEndpoints(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(RestEndpoint.class)) {
            if (element.getKind() != ElementKind.METHOD) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                    "@RestEndpoint can only be applied to methods", element);
                continue;
            }
            
            ExecutableElement method = (ExecutableElement) element;
            TypeElement controller = (TypeElement) method.getEnclosingElement();
            
            // Validate method signature
            validateRestEndpointMethod(method);
            
            // Collect metadata
            RestEndpoint annotation = method.getAnnotation(RestEndpoint.class);
            EndpointMetadata endpoint = new EndpointMetadata(
                method.getSimpleName().toString(),
                annotation.path(),
                annotation.method(),
                Arrays.asList(annotation.produces()),
                Arrays.asList(annotation.consumes()),
                annotation.requiresAuth(),
                extractParameters(method),
                extractReturnType(method)
            );
            
            String controllerName = controller.getQualifiedName().toString();
            controllers.computeIfAbsent(controllerName, k -> new ControllerMetadata(controller))
                      .addEndpoint(endpoint);
        }
    }
    
    private void validateRestEndpointMethod(ExecutableElement method) {
        // Check return type
        TypeMirror returnType = method.getReturnType();
        if (returnType.getKind() == TypeKind.VOID) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                "REST endpoint should return a value", method);
        }
        
        // Check parameters
        for (VariableElement param : method.getParameters()) {
            if (!isValidParameterType(param.asType())) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                    "Invalid parameter type: " + param.asType(), param);
            }
        }
        
        // Check modifiers
        if (!method.getModifiers().contains(Modifier.PUBLIC)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                "REST endpoint must be public", method);
        }
    }
    
    private boolean isValidParameterType(TypeMirror type) {
        // Check if type is serializable or has proper annotations
        TypeElement typeElement = (TypeElement) typeUtils.asElement(type);
        if (typeElement == null) return true; // Primitive types are OK
        
        // Check for common parameter types
        String typeName = typeElement.getQualifiedName().toString();
        return typeName.startsWith("java.lang") ||
               typeName.startsWith("java.util") ||
               typeName.startsWith("java.math") ||
               typeElement.getAnnotation(ValidatedEntity.class) != null;
    }
    
    private void processValidatedEntities(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(ValidatedEntity.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                    "@ValidatedEntity can only be applied to classes", element);
                continue;
            }
            
            TypeElement classElement = (TypeElement) element;
            ValidatedEntity annotation = classElement.getAnnotation(ValidatedEntity.class);
            
            // Process validation rules
            EntityMetadata entity = new EntityMetadata(
                classElement.getQualifiedName().toString(),
                annotation.tableName(),
                annotation.generateBuilder(),
                annotation.generateEquals()
            );
            
            // Process fields with validation
            for (Element enclosedElement : classElement.getEnclosedElements()) {
                if (enclosedElement.getKind() == ElementKind.FIELD) {
                    VariableElement field = (VariableElement) enclosedElement;
                    Validate validateAnnotation = field.getAnnotation(Validate.class);
                    
                    if (validateAnnotation != null) {
                        FieldMetadata fieldMeta = new FieldMetadata(
                            field.getSimpleName().toString(),
                            field.asType().toString(),
                            Arrays.asList(validateAnnotation.rules()),
                            validateAnnotation.message()
                        );
                        entity.addField(fieldMeta);
                    }
                }
            }
            
            generateEntityCode(entity);
        }
    }
    
    private void generateControllerImplementations() throws IOException {
        for (ControllerMetadata controller : controllers.values()) {
            generateControllerProxy(controller);
            generateOpenApiSpec(controller);
        }
    }
    
    private void generateControllerProxy(ControllerMetadata controller) throws IOException {
        String packageName = getPackageName(controller.getClassName());
        String className = getSimpleName(controller.getClassName()) + "Proxy";
        
        JavaFileObject file = filer.createSourceFile(packageName + "." + className);
        
        try (PrintWriter writer = new PrintWriter(file.openWriter())) {
            writer.println("package " + packageName + ";");
            writer.println();
            writer.println("import org.springframework.web.bind.annotation.*;");
            writer.println("import org.springframework.http.ResponseEntity;");
            writer.println("import javax.validation.Valid;");
            writer.println();
            writer.println("@RestController");
            writer.println("@RequestMapping(\"/api\")");
            writer.println("public class " + className + " {");
            writer.println();
            writer.println("    private final " + getSimpleName(controller.getClassName()) + " delegate;");
            writer.println();
            writer.println("    public " + className + "(" + getSimpleName(controller.getClassName()) + " delegate) {");
            writer.println("        this.delegate = delegate;");
            writer.println("    }");
            writer.println();
            
            // Generate endpoint methods
            for (EndpointMetadata endpoint : controller.getEndpoints()) {
                generateEndpointMethod(writer, endpoint);
            }
            
            writer.println("}");
        }
    }
    
    private void generateEndpointMethod(PrintWriter writer, EndpointMetadata endpoint) {
        // Generate Spring annotations
        writer.println("    @" + endpoint.getHttpMethod() + "Mapping(");
        writer.println("        path = \"" + endpoint.getPath() + "\",");
        writer.println("        produces = {" + String.join(", ", 
            endpoint.getProduces().stream().map(s -> "\"" + s + "\"").collect(Collectors.toList())) + "},");
        writer.println("        consumes = {" + String.join(", ", 
            endpoint.getConsumes().stream().map(s -> "\"" + s + "\"").collect(Collectors.toList())) + "}");
        writer.println("    )");
        
        // Generate method signature
        writer.print("    public ResponseEntity<" + endpoint.getReturnType() + "> " + endpoint.getMethodName() + "(");
        
        // Generate parameters
        List<String> paramStrings = new ArrayList<>();
        for (ParameterMetadata param : endpoint.getParameters()) {
            String paramAnnotation = determineParameterAnnotation(param);
            paramStrings.add(paramAnnotation + " " + param.getType() + " " + param.getName());
        }
        writer.print(String.join(", ", paramStrings));
        writer.println(") {");
        
        // Generate method body
        writer.println("        try {");
        
        if (endpoint.isRequiresAuth()) {
            writer.println("            // Authentication check would be here");
        }
        
        writer.print("            " + endpoint.getReturnType() + " result = delegate." + endpoint.getMethodName() + "(");
        writer.print(endpoint.getParameters().stream()
            .map(ParameterMetadata::getName)
            .collect(Collectors.joining(", ")));
        writer.println(");");
        
        writer.println("            return ResponseEntity.ok(result);");
        writer.println("        } catch (Exception e) {");
        writer.println("            return ResponseEntity.internalServerError().build();");
        writer.println("        }");
        writer.println("    }");
        writer.println();
    }
    
    private String determineParameterAnnotation(ParameterMetadata param) {
        // Simple heuristic - in real implementation, this would be more sophisticated
        if (param.getType().startsWith("java.lang.String") && param.getName().contains("id")) {
            return "@PathVariable";
        } else if (isPrimitiveOrWrapper(param.getType())) {
            return "@RequestParam";
        } else {
            return "@RequestBody @Valid";
        }
    }
    
    private boolean isPrimitiveOrWrapper(String type) {
        return type.equals("int") || type.equals("Integer") ||
               type.equals("long") || type.equals("Long") ||
               type.equals("String") || type.equals("boolean") || type.equals("Boolean");
    }
    
    private void generateValidationCode(EntityMetadata entity) throws IOException {
        String packageName = getPackageName(entity.getClassName());
        String className = getSimpleName(entity.getClassName()) + "Validator";
        
        JavaFileObject file = filer.createSourceFile(packageName + "." + className);
        
        try (PrintWriter writer = new PrintWriter(file.openWriter())) {
            writer.println("package " + packageName + ";");
            writer.println();
            writer.println("import java.util.*;");
            writer.println("import java.util.regex.Pattern;");
            writer.println();
            writer.println("public class " + className + " {");
            writer.println();
            
            // Generate validation methods for each field
            for (FieldMetadata field : entity.getFields()) {
                generateFieldValidation(writer, field);
            }
            
            // Generate main validation method
            writer.println("    public List<String> validate(" + getSimpleName(entity.getClassName()) + " entity) {");
            writer.println("        List<String> errors = new ArrayList<>();");
            
            for (FieldMetadata field : entity.getFields()) {
                String getterName = "get" + capitalize(field.getName());
                writer.println("        errors.addAll(validate" + capitalize(field.getName()) + "(entity." + getterName + "()));");
            }
            
            writer.println("        return errors;");
            writer.println("    }");
            writer.println("}");
        }
    }
    
    private void generateFieldValidation(PrintWriter writer, FieldMetadata field) {
        String methodName = "validate" + capitalize(field.getName());
        writer.println("    private List<String> " + methodName + "(" + field.getType() + " value) {");
        writer.println("        List<String> errors = new ArrayList<>();");
        
        for (String rule : field.getValidationRules()) {
            generateValidationRule(writer, field, rule);
        }
        
        writer.println("        return errors;");
        writer.println("    }");
        writer.println();
    }
    
    private void generateValidationRule(PrintWriter writer, FieldMetadata field, String rule) {
        if (rule.equals("required")) {
            writer.println("        if (value == null) {");
            writer.println("            errors.add(\"" + field.getName() + " is required\");");
            writer.println("        }");
        } else if (rule.startsWith("minLength:")) {
            int minLength = Integer.parseInt(rule.substring(10));
            writer.println("        if (value != null && value.toString().length() < " + minLength + ") {");
            writer.println("            errors.add(\"" + field.getName() + " must be at least " + minLength + " characters\");");
            writer.println("        }");
        } else if (rule.equals("email")) {
            writer.println("        if (value != null && !isValidEmail(value.toString())) {");
            writer.println("            errors.add(\"" + field.getName() + " must be a valid email\");");
            writer.println("        }");
        }
    }
    
    // Utility methods
    private String getPackageName(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot > 0 ? fullyQualifiedName.substring(0, lastDot) : "";
    }
    
    private String getSimpleName(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot > 0 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
    }
    
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
\`\`\`

**Metadata Classes:**

\`\`\`java
public class ControllerMetadata {
    private final String className;
    private final List<EndpointMetadata> endpoints = new ArrayList<>();
    
    public ControllerMetadata(TypeElement element) {
        this.className = element.getQualifiedName().toString();
    }
    
    public void addEndpoint(EndpointMetadata endpoint) {
        endpoints.add(endpoint);
    }
    
    // Getters...
}

public class EndpointMetadata {
    private final String methodName;
    private final String path;
    private final HttpMethod httpMethod;
    private final List<String> produces;
    private final List<String> consumes;
    private final boolean requiresAuth;
    private final List<ParameterMetadata> parameters;
    private final String returnType;
    
    // Constructor and getters...
}

public class EntityMetadata {
    private final String className;
    private final String tableName;
    private final boolean generateBuilder;
    private final boolean generateEquals;
    private final List<FieldMetadata> fields = new ArrayList<>();
    
    // Constructor and methods...
}
\`\`\`

**Usage Example:**

\`\`\`java
// Original controller class
public class UserController {
    
    @RestEndpoint(path = "/users/{id}", method = HttpMethod.GET)
    public User getUser(String id) {
        return userService.findById(id);
    }
    
    @RestEndpoint(path = "/users", method = HttpMethod.POST, requiresAuth = true)
    public User createUser(CreateUserRequest request) {
        return userService.create(request);
    }
}

// Entity with validation
@ValidatedEntity(tableName = "users", generateBuilder = true)
public class User {
    
    @Validate(rules = {"required", "minLength:2"})
    private String name;
    
    @Validate(rules = {"required", "email"})
    private String email;
    
    // Getters and setters...
}
\`\`\`

**Generated Code Example:**

\`\`\`java
// Generated UserControllerProxy.java
@RestController
@RequestMapping("/api")
public class UserControllerProxy {
    
    private final UserController delegate;
    
    public UserControllerProxy(UserController delegate) {
        this.delegate = delegate;
    }
    
    @GetMapping(
        path = "/users/{id}",
        produces = {"application/json"}
    )
    public ResponseEntity<User> getUser(@PathVariable String id) {
        try {
            User result = delegate.getUser(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
\`\`\`

**Build Configuration:**

\`\`\`xml
<!-- Maven configuration -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <annotationProcessorPaths>
            <path>
                <groupId>com.example</groupId>
                <artifactId>rest-api-processor</artifactId>
                <version>1.0.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
\`\`\`

**Common Use Cases:**
1. **Code generation** - REST controllers, builders, mappers
2. **Compile-time validation** - business rules, constraints
3. **Documentation generation** - OpenAPI specs, javadocs
4. **Performance optimization** - eliminate reflection
5. **Framework integration** - dependency injection, AOP

**Best Practices:**
1. **Validate early** - catch errors at compile time
2. **Generate readable code** - for debugging
3. **Handle incremental compilation** - support IDE integration
4. **Provide clear error messages** - help developers
5. **Cache metadata** - improve performance`,
        tags: ['annotations', 'code-generation', 'compile-time', 'processor', 'validation', 'framework'],
        followUp: [
          'How would you handle annotation inheritance and composition?',
          'What are the limitations of annotation processing?',
          'How do you test annotation processors effectively?'
        ]
      }space'],
        followUp: [
          'How would you implement class versioning in a plugin system?',
          'What are the security implications of custom ClassLoaders?',
          'How do you handle dependency conflicts between plugins?'
        ]
      },