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

public class BlacklistRuleTest {

    @Mock
    private RedisStore redisStore;

    private BlacklistRule blacklistRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        blacklistRule = new BlacklistRule(redisStore);
    }

    @Test
    void shouldBlacklistKnownBadCard() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("9999")
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.isInSet(anyString(), eq("9999"))).thenReturn(true);

        RuleResult result = blacklistRule.evaluate(event);

        assertEquals(0.95, result.score());
        assertEquals("blacklist", result.ruleName());
    }

    @Test
    void shouldAllowCleanCard() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.isInSet(anyString(), eq("1234"))).thenReturn(false);

        RuleResult result = blacklistRule.evaluate(event);

        assertEquals(0.0, result.score());
    }
}