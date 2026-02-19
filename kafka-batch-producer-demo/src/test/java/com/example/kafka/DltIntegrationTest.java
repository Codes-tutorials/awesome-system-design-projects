package com.example.kafka;

import com.example.kafka.config.KafkaConfig;
import com.example.kafka.service.KafkaProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class DltIntegrationTest {

    @Autowired
    private KafkaProducerService producerService;

    private CountDownLatch dltLatch = new CountDownLatch(1);

    @Test
    void testRetryAndDlt() throws InterruptedException {
        // Send a message that triggers failure
        producerService.sendMessage("user1", "fail");

        // Wait for the message to arrive in DLT
        boolean messageReceived = dltLatch.await(10, TimeUnit.SECONDS);
        assertTrue(messageReceived, "Message should be received in DLT after retries");
    }

    // Listener for DLT to verify redirection
    @KafkaListener(topics = KafkaConfig.DLT_TOPIC_NAME, groupId = "dlt-group")
    public void listenDlt(String message) {
        System.out.println("Received message in DLT: " + message);
        dltLatch.countDown();
    }
}
