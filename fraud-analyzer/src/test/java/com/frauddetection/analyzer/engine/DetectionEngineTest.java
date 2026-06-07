package com.frauddetection.analyzer.engine;

import com.frauddetection.common.event.TransactionEvent;
import com.frauddetection.common.model.Action;
import com.frauddetection.common.model.RuleResult;
import com.frauddetection.analyzer.store.RedisStore;
import com.frauddetection.analyzer.rules.VelocityRule;
import com.frauddetection.analyzer.rules.BlacklistRule;
import com.frauddetection.analyzer.rules.DetectionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DetectionEngineTest {

    @Mock
    private RedisStore redisStore;

    private VelocityRule velocityRule;
    private BlacklistRule blacklistRule;
    private DetectionEngine detectionEngine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        velocityRule = new VelocityRule(redisStore);
        blacklistRule = new BlacklistRule(redisStore);
        detectionEngine = new DetectionEngine(List.of(velocityRule, blacklistRule));
    }

    @Test
    void shouldReturnAllowWhenNoRulesTrigger() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .amount(BigDecimal.valueOf(100))
            .ipAddress("192.168.1.1")
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.isInSet(anyString(), anyString())).thenReturn(false);
        when(redisStore.incrementWindowCount(anyString(), anyLong(), anyInt())).thenReturn(1L);

        var decision = detectionEngine.evaluate(event);

        assertEquals(Action.ALLOW, decision.getAction());
    }

    @Test
    void shouldReturnBlockWhenHighRiskScore() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .amount(BigDecimal.valueOf(5000))
            .ipAddress("192.168.1.1")
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.isInSet(anyString(), anyString())).thenReturn(false);
        when(redisStore.incrementWindowCount(anyString(), anyLong(), anyInt())).thenReturn(20L);

        var decision = detectionEngine.evaluate(event);

        assertEquals(Action.BLOCK, decision.getAction());
    }

    @Test
    void shouldTriggerOverrideWhenRuleScoreExceedsThreshold() {
        DetectionRule ruleWithOverride = new DetectionRule() {
            @Override
            public String getName() {
                return "test-override";
            }
            
            @Override
            public double getWeight() {
                return 0.1;
            }
            
            @Override
            public RuleResult evaluate(TransactionEvent event) {
                return RuleResult.of("test-override", 0.95, 0.1, "Test override");
            }
        };
        
        DetectionEngine engine = new DetectionEngine(List.of(ruleWithOverride));

        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .transactionId(UUID.randomUUID().toString())
            .build();

        var decision = engine.evaluate(event);

        assertEquals(Action.BLOCK, decision.getAction());
        assertTrue(decision.getReason().contains("HIGH_RISK_OVERRIDE"));
    }

    @Test
    void shouldCalculateNormalizedScoreCorrectly() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .amount(BigDecimal.valueOf(100))
            .ipAddress("10.0.0.1")
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.isInSet(anyString(), eq("1234"))).thenReturn(false);
        when(redisStore.incrementWindowCount(anyString(), anyLong(), anyInt())).thenReturn(5L);

        var decision = detectionEngine.evaluate(event);

        assertNotNull(decision.getScore());
        assertTrue(decision.getScore() >= 0 && decision.getScore() <= 1);
    }
}