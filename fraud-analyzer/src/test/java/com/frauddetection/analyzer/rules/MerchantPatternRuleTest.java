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

public class MerchantPatternRuleTest {

    @Mock
    private RedisStore redisStore;

    private MerchantPatternRule merchantPatternRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        merchantPatternRule = new MerchantPatternRule(redisStore);
    }

    @Test
    void shouldReturnZeroScoreWhenBelowThreshold() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .merchantId("MERCH001")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.countUniqueMerchantsForCard(anyString(), anyLong(), anyLong())).thenReturn(5L);

        RuleResult result = merchantPatternRule.evaluate(event);

        assertEquals(0.0, result.score());
    }

    @Test
    void shouldFlagHighMerchantSwitching() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .merchantId("MERCH001")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        when(redisStore.countUniqueMerchantsForCard(anyString(), anyLong(), anyLong())).thenReturn(20L);

        RuleResult result = merchantPatternRule.evaluate(event);

        assertTrue(result.score() > 0);
        assertTrue(result.details().contains("Rapid merchant switching"));
    }

    @Test
    void shouldSkipWhenNoMerchantProvided() {
        TransactionEvent event = TransactionEvent.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .cardLast4("1234")
            .amount(BigDecimal.valueOf(100))
            .transactionId(UUID.randomUUID().toString())
            .build();

        RuleResult result = merchantPatternRule.evaluate(event);

        assertEquals(0.0, result.score());
        assertEquals("No merchant info", result.details());
    }
}