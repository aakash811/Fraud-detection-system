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

public class IpVelocityRuleTest {

    @Mock
    private RedisStore redisStore;

    private IpVelocityRule ipVelocityRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ipVelocityRule = new IpVelocityRule(redisStore);
    }

    @Test
    void shouldReturnZeroScoreWhenBelowThreshold() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .ipAddress("192.168.1.1")
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.incrementWindowCount(anyString(), anyLong(), anyInt())).thenReturn(5L);

        RuleResult result = ipVelocityRule.evaluate(event);

        assertEquals(0.0, result.score());
        assertEquals("ip-velocity", result.ruleName());
    }

    @Test
    void shouldReturnPositiveScoreWhenExceedsThreshold() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .ipAddress("192.168.1.1")
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.incrementWindowCount(anyString(), anyLong(), anyInt())).thenReturn(25L);

        RuleResult result = ipVelocityRule.evaluate(event);

        assertTrue(result.score() > 0);
        assertEquals("ip-velocity", result.ruleName());
    }

    @Test
    void shouldSkipWhenNoIpProvided() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .transactionId(UUID.randomUUID().toString())
            .build();

        RuleResult result = ipVelocityRule.evaluate(event);

        assertEquals(0.0, result.score());
        assertEquals("No IP provided", result.details());
    }
}