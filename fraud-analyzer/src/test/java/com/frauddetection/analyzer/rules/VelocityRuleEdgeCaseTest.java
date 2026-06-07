package com.frauddetection.analyzer.rules;

import com.frauddetection.analyzer.store.RedisStore;
import com.frauddetection.common.event.TransactionEvent;
import com.frauddetection.common.model.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VelocityRuleEdgeCaseTest {

    @Mock
    private RedisStore redisStore;

    private VelocityRule velocityRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        velocityRule = new VelocityRule(redisStore);
    }

    @Test
    void shouldHandleNullCardLast4() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        RuleResult result = velocityRule.evaluate(event);
        assertNotNull(result);
    }

    @Test
    void shouldHandleNegativeAmount() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .amount(BigDecimal.valueOf(-100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        RuleResult result = velocityRule.evaluate(event);
        assertNotNull(result);
    }

    @Test
    void shouldHandleZeroAmount() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .amount(BigDecimal.ZERO)
            .transactionId(UUID.randomUUID().toString())
            .build();

        RuleResult result = velocityRule.evaluate(event);
        assertNotNull(result);
    }

    @Test
    void shouldHandleFutureTimestamp() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now().plusSeconds(86400))
            .cardLast4("1234")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        RuleResult result = velocityRule.evaluate(event);
        assertNotNull(result);
    }

    @Test
    void shouldHandlePastTimestamp() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now().minusSeconds(86400))
            .cardLast4("1234")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        RuleResult result = velocityRule.evaluate(event);
        assertNotNull(result);
    }
}