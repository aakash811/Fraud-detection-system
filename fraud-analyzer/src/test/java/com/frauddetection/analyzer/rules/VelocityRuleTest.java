package com.frauddetection.analyzer.rules;

import com.frauddetection.analyzer.store.RedisStore;
import com.frauddetection.common.event.TransactionEvent;
import com.frauddetection.common.model.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VelocityRuleTest {

    @Mock
    private RedisStore redisStore;

    private VelocityRule velocityRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        velocityRule = new VelocityRule(redisStore);
    }

    @Test
    void shouldReturnZeroScoreWhenWithinThreshold() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.incrementWindowCount(anyString(), anyLong(), anyInt())).thenReturn(5L);

        RuleResult result = velocityRule.evaluate(event);

        assertEquals(0.0, result.score());
        assertEquals("velocity", result.ruleName());
    }

    @Test
    void shouldReturnPositiveScoreWhenExceedsThreshold() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.incrementWindowCount(anyString(), anyLong(), anyInt())).thenReturn(15L);

        RuleResult result = velocityRule.evaluate(event);

        assertTrue(result.score() > 0);
        assertEquals("velocity", result.ruleName());
    }
}