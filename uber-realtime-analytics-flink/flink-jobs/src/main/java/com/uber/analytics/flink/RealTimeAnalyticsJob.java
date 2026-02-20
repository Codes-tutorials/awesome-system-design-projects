package com.uber.analytics.flink;

import com.uber.analytics.flink.jobs.DynamicPricingJob;
import com.uber.analytics.flink.jobs.RealTimeMatchingJob;
import com.uber.analytics.flink.jobs.AnalyticsAggregationJob;
import com.uber.analytics.flink.jobs.AnomalyDetectionJob;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Main entry point for Uber-style real-time analytics Flink jobs
 */
public class RealTimeAnalyticsJob {
    
    private static final Logger logger = LoggerFactory.getLogger(RealTimeAnalyticsJob.class);
    
    public static void main(String[] args) throws Exception {
        logger.info("Starting Uber Real-Time Analytics Platform");
        
        // Parse command line arguments
        String jobType = args.length > 0 ? args[0] : "all";
        
        // Create Flink execution environment
        StreamExecutionEnvironment env = createExecutionEnvironment();
        
        // Execute the specified job type
        switch (jobType.toLowerCase()) {
            case "matching":
                logger.info("Starting Real-Time Matching Job");
                new RealTimeMatchingJob().execute(env);
                break;
                
            case "pricing":
                logger.info("Starting Dynamic Pricing Job");
                new DynamicPricingJob().execute(env);
                break;
                
            case "analytics":
                logger.info("Starting Analytics Aggregation Job");
                new AnalyticsAggregationJob().execute(env);
                break;
                
            case "anomaly":
                logger.info("Starting Anomaly Detection Job");
                new AnomalyDetectionJob().execute(env);
                break;
                
            case "all":
            default:
                logger.info("Starting All Jobs");
                executeAllJobs(env);
                break;
        }
        
        // Execute the job
        env.execute("Uber Real-Time Analytics - " + jobType);
        logger.info("Job execution completed");
    }
    
    private static StreamExecutionEnvironment createExecutionEnvironment() {
        Configuration config = new Configuration();
        
        // Enable checkpointing
        config.setString("execution.checkpointing.interval", "60s");
        config.setString("execution.checkpointing.mode", "EXACTLY_ONCE");
        config.setString("execution.checkpointing.timeout", "10min");
        config.setString("execution.checkpointing.max-concurrent-checkpoints", "1");
        
        // State backend configuration
        config.setString("state.backend", "rocksdb");
        config.setString("state.checkpoints.dir", "file:///tmp/flink-checkpoints");
        config.setString("state.savepoints.dir", "file:///tmp/flink-savepoints");
        
        // Parallelism and resource configuration
        config.setInteger("parallelism.default", 4);
        config.setString("taskmanager.memory.process.size", "2gb");
        config.setString("jobmanager.memory.process.size", "1gb");
        
        // Network configuration
        config.setString("taskmanager.network.memory.fraction", "0.1");
        config.setString("taskmanager.network.memory.min", "64mb");
        config.setString("taskmanager.network.memory.max", "1gb");
        
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(config);
        
        // Configure restart strategy
        env.setRestartStrategy(RestartStrategies.failureRateRestart(
                3, // max failures per interval
                Time.of(5, TimeUnit.MINUTES), // failure rate interval
                Time.of(10, TimeUnit.SECONDS) // delay between restarts
        ));
        
        // Enable event time processing
        env.getConfig().setAutoWatermarkInterval(1000);
        
        logger.info("Flink execution environment configured successfully");
        return env;
    }
    
    private static void executeAllJobs(StreamExecutionEnvironment env) {
        logger.info("Configuring all streaming jobs");
        
        try {
            // Real-time matching job
            new RealTimeMatchingJob().execute(env);
            logger.info("Real-time matching job configured");
            
            // Dynamic pricing job
            new DynamicPricingJob().execute(env);
            logger.info("Dynamic pricing job configured");
            
            // Analytics aggregation job
            new AnalyticsAggregationJob().execute(env);
            logger.info("Analytics aggregation job configured");
            
            // Anomaly detection job
            new AnomalyDetectionJob().execute(env);
            logger.info("Anomaly detection job configured");
            
        } catch (Exception e) {
            logger.error("Failed to configure jobs", e);
            throw new RuntimeException("Job configuration failed", e);
        }
    }
}