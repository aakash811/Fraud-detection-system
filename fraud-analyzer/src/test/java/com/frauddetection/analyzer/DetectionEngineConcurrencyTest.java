package com.frauddetection.analyzer;

import com.frauddetection.analyzer.store.RedisStore;
import com.frauddetection.analyzer.rules.DetectionRule;
import com.frauddetection.analyzer.engine.DetectionEngine;
import com.frauddetection.common.event.TransactionEvent;
import com.frauddetection.common.model.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DetectionEngineConcurrencyTest {

    @Mock
    private RedisStore redisStore;

    @Mock
    private DetectionRule mockRule;

    private DetectionEngine detectionEngine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockRule.getName()).thenReturn("test-rule");
        when(mockRule.getWeight()).thenReturn(1.0);
        when(mockRule.evaluate(any())).thenReturn(RuleResult.of("test-rule", 0.0, 1.0, "No match"));
        
        detectionEngine = new DetectionEngine(List.of(mockRule));
    }

    @Test
    void shouldHandleConcurrentTransactions() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    TransactionEvent event = TransactionEvent.builder()
                        .eventId(UUID.randomUUID())
                        .occurredAt(Instant.now())
                        .cardLast4("1234")
                        .amount(BigDecimal.valueOf(100 + index))
                        .ipAddress("192.168.1." + (index % 255))
                        .transactionId(UUID.randomUUID().toString())
                        .build();
                    
                    detectionEngine.evaluate(event);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        verify(mockRule, times(threadCount)).evaluate(any());
    }
}