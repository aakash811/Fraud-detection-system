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

public class BlacklistRuleEdgeCaseTest {

    @Mock
    private RedisStore redisStore;

    private BlacklistRule blacklistRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        blacklistRule = new BlacklistRule(redisStore);
    }

    @Test
    void shouldHandleNullCardLast4() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        RuleResult result = blacklistRule.evaluate(event);
        assertEquals(0.0, result.score());
        assertEquals("No card provided", result.details());
    }

    @Test
    void shouldHandleEmptyCardLast4() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.isInSet(anyString(), anyString())).thenReturn(false);
        RuleResult result = blacklistRule.evaluate(event);
        assertNotNull(result);
    }

    @Test
    void shouldHandleRedisDown() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.isInSet(anyString(), anyString())).thenReturn(null);
        RuleResult result = blacklistRule.evaluate(event);
        assertEquals(0.0, result.score());
    }

    @Test
    void shouldHandleShortCardLast4() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("12")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.isInSet(anyString(), anyString())).thenReturn(false);
        RuleResult result = blacklistRule.evaluate(event);
        assertNotNull(result);
    }
}