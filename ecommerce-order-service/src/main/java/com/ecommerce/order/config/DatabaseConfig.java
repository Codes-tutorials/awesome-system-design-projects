package com.ecommerce.order.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.ecommerce.order.repository")
public class DatabaseConfig {
    
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    
    @Value("${spring.datasource.hikari.maximum-pool-size:50}")
    private int maximumPoolSize;
    
    @Value("${spring.datasource.hikari.minimum-idle:10}")
    private int minimumIdle;
    
    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;
    
    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;
    
    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;
    
    @Value("${spring.datasource.hikari.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;
    
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic connection properties
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Pool configuration for high performance
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        
        // Performance optimizations
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        config.setInitializationFailTimeout(10000);
        
        // Connection properties for PostgreSQL optimization
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // Pool name for monitoring
        config.setPoolName("OrderServiceHikariPool");
        
        return new HikariDataSource(config);
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.ecommerce.order.model");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        
        return em;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
    
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        
        // Basic Hibernate properties
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "true");
        
        // Performance optimizations
        properties.put("hibernate.jdbc.batch_size", "50");
        properties.put("hibernate.jdbc.fetch_size", "100");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        properties.put("hibernate.batch_versioned_data", "true");
        properties.put("hibernate.jdbc.batch_versioned_data", "true");
        
        // Second-level cache configuration
        properties.put("hibernate.cache.use_second_level_cache", "true");
        properties.put("hibernate.cache.use_query_cache", "true");
        properties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
        properties.put("hibernate.javax.cache.provider", "com.hazelcast.cache.impl.HazelcastServerCachingProvider");
        
        // Statistics and monitoring
        properties.put("hibernate.generate_statistics", "false");
        properties.put("hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS", "100");
        
        // Connection handling
        properties.put("hibernate.connection.provider_disables_autocommit", "true");
        properties.put("hibernate.query.fail_on_pagination_over_collection_fetch", "true");
        properties.put("hibernate.query.in_clause_parameter_padding", "true");
        
        return properties;
    }
    
    /**
     * Configuration for read-only operations (can be used for reporting queries)
     */
    @Bean
    public DataSource readOnlyDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Use read replica URL if available, otherwise use main database
        String readOnlyUrl = jdbcUrl.replace("localhost:5432", "localhost:5433"); // Example read replica
        config.setJdbcUrl(readOnlyUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Smaller pool for read-only operations
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        
        // Read-only optimizations
        config.setReadOnly(true);
        config.addDataSourceProperty("readOnly", "true");
        config.addDataSourceProperty("defaultTransactionIsolation", "TRANSACTION_READ_UNCOMMITTED");
        
        config.setPoolName("OrderServiceReadOnlyPool");
        
        return new HikariDataSource(config);
    }
}